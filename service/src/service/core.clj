(ns service.core
  (:require [org.httpkit.server :as http-kit]
            [clj-esper.core :as esp]
            [service.web :refer (start-web-service)]
            [common.cli        :as cli]
            [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.consumers :as lc]))

(esp/defevent SwitchEvent [x :int y :int t :int direction :string])

(defn send-event [esp-service event attrs]
  (esp/trigger-event esp-service
    (apply esp/new-event (into [event] attrs))))

(defn current-state-handler [last-switch-events-stmt width height]
  {:width width :height height
   :switch-times (esp/pull-events last-switch-events-stmt)})

(defn query-results-to-channel [channel results]
  (doseq [result results]
    (http-kit/send! channel (pr-str result))))

(defn query-handler [esp-service request]
  (http-kit/with-channel request channel

    (println "query: channel opened")

    (let [listener (esp/create-listener (partial query-results-to-channel channel))
          cleanup-fn (atom #())]
      (http-kit/on-receive channel
        (fn [query]
          (@cleanup-fn)
          (println (str "Starting query: " query))
          (let [stmt (esp/create-statement esp-service query)]
            (esp/attach-listener stmt listener)
            (reset! cleanup-fn
              #(do
                (println (str "Stopping query: " query))
                (esp/detach-listener stmt listener)))
            )))

      (http-kit/on-close channel
        (fn [status]
          (@cleanup-fn)
          (println "query: channel closed")))
    )
))

(defn run [width height queue]
  (let [esp-conf (esp/create-configuration [SwitchEvent])
        esp-service (esp/create-service "CrossroadsSimulator" esp-conf)
        last-switch-events-stmt (esp/create-statement esp-service "select * from SwitchEvent.std:unique(x,y)")
        conn (rmq/connect)
        ch (lch/open conn)
        _ (lq/declare ch queue)
        _ (lc/subscribe ch queue
            (fn [_ _ ^bytes payload]
              (let [event (flatten (vec (read-string (String. payload "UTF-8"))))]
                (send-event esp-service SwitchEvent event)))
            :auto-ack true)
        stop-web-service
          (start-web-service {:port 3000}
            (partial current-state-handler last-switch-events-stmt width height)
            (partial query-handler esp-service))]
    #(do (stop-web-service) (rmq/close ch) (rmq/close conn) (.destroy esp-service))
))

(def cli-options [])

(defn -main [& args]
  (let [{:keys [width height switch-events-queue]} (cli/parse-opts args cli-options)]
    (run width height switch-events-queue)))
