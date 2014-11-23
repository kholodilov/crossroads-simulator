(ns service.core
  (:require [org.httpkit.server :as http-kit]
            [common.cli         :as cli]
            [common.events      :as events]
            [common.service     :as service]
            [common.messaging   :as messaging]
            [service.web        :as web]))

(defn current-state-handler [esp-service width height]
  {:width width :height height
   :switch-times (events/pull-events esp-service "select * from SwitchEvent.std:unique(x,y)")})

(defn query-results-to-channel [channel results]
  (doseq [result results]
    (http-kit/send! channel (pr-str result))))

(defn query-handler [esp-service request]
  (http-kit/with-channel request channel

    (println "query: channel opened")

    (let [listener (partial query-results-to-channel channel)
          cleanup-fn (atom #())]
      (http-kit/on-receive channel
        (fn [query]
          (@cleanup-fn)
          (println (str "Starting query: " query))
          (let [subscription (events/subscribe esp-service query listener)]
            (reset! cleanup-fn
              #(do
                (println (str "Stopping query: " query))
                (events/unsubscribe esp-service subscription)))
            )))

      (http-kit/on-close channel
        (fn [status]
          (@cleanup-fn)
          (println "query: channel closed")))
    )
))

(defn run [width height queue]
  (let [esp-service (events/build-esper-service "CrossroadsSimulator")
        stop-web-service
          (web/start-web-service {:port 3000}
            (partial current-state-handler esp-service width height)
            (partial query-handler esp-service))
        messaging-conn (messaging/connect)]

    (messaging/subscribe messaging-conn queue
      (fn [event-attrs] (events/trigger-event esp-service events/SwitchEvent event-attrs)))

    #(do
      (messaging/disconnect messaging-conn)
      (stop-web-service)
      (service/stop esp-service))
))

(def cli-options [])

(defn -main [& args]
  (let [{:keys [width height switch-events-queue]} (cli/parse-opts args cli-options)]
    (run width height switch-events-queue)))
