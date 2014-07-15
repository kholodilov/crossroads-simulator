(ns data-source.core
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.basic     :as lb]))

(def default-exchange "")
(def switch-events-queue "switch-events")

(defn -main [& args]
  (let [queue (first args)
        conn (rmq/connect)
        ch (lch/open conn)]
    (lq/declare ch queue :exclusive false)
    (lb/publish ch default-exchange queue "Hello!")
    (rmq/close ch)
    (rmq/close conn)
  ))
