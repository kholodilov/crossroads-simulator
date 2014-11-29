(ns service.core
  (:require [org.httpkit.server :as http-kit]
            [common.cli         :as cli]
            [common.events      :as events]
            [common.service     :as service]
            [common.messaging   :as messaging]
            [ruiyun.tools.timer :as timer]
            [service.web        :as web]))

(defn current-state-handler [event-service width height]
  (let [statement (events/create-statement event-service "select * from SwitchEvent.std:unique(x,y)")
        switch-times (events/pull-events event-service statement)]
    (events/destroy-statement event-service statement)
    {:width width :height height :switch-times switch-times}))

(defn query-results-to-channel [channel results]
  (doseq [result results]
    (http-kit/send! channel (pr-str result))))

(defn query-handler [event-service request]
  (http-kit/with-channel request channel

    (println "query: channel opened")

    (let [listener (partial query-results-to-channel channel)
          cleanup-fn (atom #())]
      (http-kit/on-receive channel
        (fn [query]
          (@cleanup-fn)
          (println (str "Starting query: " query))
          (let [statement (events/create-statement event-service query)]
            (events/subscribe event-service statement listener)
            (reset! cleanup-fn
              #(do
                (println (str "Stopping query: " query))
                (events/destroy-statement event-service statement)))
            )))

      (http-kit/on-close channel
        (fn [status]
          (@cleanup-fn)
          (println "query: channel closed")))
    )
))

(defn run-timer [event-service period]
  (let [time (atom 0)
        timer (timer/timer)
        timer-fn
          (fn []
            (events/trigger-event event-service events/TimerEvent {:time @time})
            (swap! time + period))]
      (timer/run-task! timer-fn :period period :by timer)
      (service/build-service :stop-fn #(timer/cancel! timer))
    ))

(defn run [width height queue]
  (let [event-service (events/build-esper-service "CrossroadsSimulator")
        timer-service (run-timer event-service 1000)
        stop-web-service
          (web/start-web-service {:port 3000}
            (partial current-state-handler event-service width height)
            (partial query-handler event-service))
        messaging-conn (messaging/connect)]

    (messaging/subscribe messaging-conn queue
      (fn [event-attrs] (events/trigger-event event-service events/SwitchEvent event-attrs)))

    #(do
      (messaging/disconnect messaging-conn)
      (stop-web-service)
      (service/stop timer-service)
      (service/stop event-service))
))

(def cli-options [])

(defn -main [& args]
  (let [{:keys [width height switch-events-queue]} (cli/parse-opts args cli-options)]
    (run width height switch-events-queue)))
