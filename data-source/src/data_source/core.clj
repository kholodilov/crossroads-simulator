(ns data-source.core
  (:require [ruiyun.tools.timer :as timer]
            [common.cli         :as cli]
            [common.messaging   :as messaging]
            [data-source.gen    :as gen]))

(defn start-switch-events-generator [width height publish-fn]
  (let [switch-events (atom (gen/initial-switch-events width height))
        generate-and-publish-switch-events
          (fn [] 
            (swap! switch-events gen/next-switch-events)
            (dorun (map publish-fn @switch-events)))
        timer (timer/run-task! generate-and-publish-switch-events :period 1000)]
    #(timer/cancel! timer)))

(defn run [width height queue]
  (let [messaging-conn (messaging/connect)
        publish-fn (messaging/publish-fn messaging-conn queue)
        stop-gen (start-switch-events-generator width height publish-fn)]
    #(do
      (stop-gen)
      (messaging/disconnect messaging-conn))))

(def cli-options [])

(defn -main [& args]
  (let [{:keys [width height switch-events-queue]} (cli/parse-opts args cli-options)]
    (run width height switch-events-queue)))
