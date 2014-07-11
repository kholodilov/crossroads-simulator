(ns data-source.core
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.basic     :as lb]))

(def default-exchange "")
(def switch-events-queue "switch-events")

(defn -main [& args]
  (let [conn (rmq/connect)
        ch (lch/open conn)]
    (lq/declare ch switch-events-queue :exclusive false)
    (lb/publish ch default-exchange switch-events-queue "Hello!")
    (rmq/close ch)
    (rmq/close conn)
  ))
