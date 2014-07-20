(ns data-source.core
  (:require [langohr.core       :as rmq]
            [langohr.channel    :as lch]
            [langohr.queue      :as lq]
            [langohr.basic      :as lb]
            [ruiyun.tools.timer :as timer]
            [common.cli         :as cli]
            [data-source.gen    :as gen]))

(def default-exchange "")

(defn start-switch-events-generator [width height publish-fn]
  (let [state (atom (gen/initial-switch-events width height))
        upd-and-pub-state
          #(do 
            (swap! state gen/next-switch-events)
            (doseq [s @state] (publish-fn (pr-str s))))
        timer (timer/run-task! upd-and-pub-state :period 1000)]
    #(timer/cancel! timer)))

(defn run [width height queue]
  (let [conn (rmq/connect)
        ch (lch/open conn)
        _ (lq/declare ch queue)
        stop-gen (start-switch-events-generator width height 
                    (partial lb/publish ch default-exchange queue))]
    #(do
      (stop-gen)
      (rmq/close ch)
      (rmq/close conn))))

(def cli-options [])

(defn -main [& args]
  (let [{:keys [width height switch-events-queue]} (cli/parse-opts args cli-options)]
    (run width height switch-events-queue)))
