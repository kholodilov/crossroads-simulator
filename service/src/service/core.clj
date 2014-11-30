(ns service.core
  (:require [org.httpkit.server :as http-kit]
            [common.cli         :as cli]
            [common.events      :as events]
            [common.service     :as service]
            [common.messaging   :as messaging]
            [ruiyun.tools.timer :as timer]
            [service.web        :as web]))

(defn run-timer [event-service period]
  (let [time (atom 0)
        timer (timer/timer)
        timer-fn
          (fn []
            (swap! time + period)
            (events/do-timestep event-service @time))]
      (timer/run-task! timer-fn :period period :by timer :delay period)
      (service/build-service :stop-fn #(timer/cancel! timer))
    ))

(defn run [width height queue]
  (let [event-service (events/build-esper-service "CrossroadsSimulator")
        timer-service (run-timer event-service 100)
        web-service (web/start-web-service event-service {:port 3000})
        messaging-conn (messaging/connect)]

    (messaging/subscribe messaging-conn queue
      (fn [event-attrs] (events/trigger-event event-service events/SwitchEvent event-attrs)))

    #(do
      (messaging/disconnect messaging-conn)
      (service/stop web-service)
      (service/stop timer-service)
      (service/stop event-service))
))

(def cli-options [])

(defn -main [& args]
  (let [{:keys [width height switch-events-queue]} (cli/parse-opts args cli-options)]
    (run width height switch-events-queue)))
