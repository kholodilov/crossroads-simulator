(ns common.messaging
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.basic     :as lb]
            [langohr.consumers :as lc]))

(defn connect [] (rmq/connect))
(defn disconnect [conn] (rmq/close conn))

(defn- declare-queue [ch queue]
  (lq/declare ch queue))

(def default-exchange "")

(defn publish-fn [conn queue]
  (let [ch (lch/open conn)]
    (declare-queue ch queue)
    (fn [data]
      (lb/publish ch default-exchange queue (pr-str data)))))

(defn subscribe [conn queue handler]
  (let [ch (lch/open conn)]
    (declare-queue ch queue)
    (lc/subscribe ch queue
      (fn [_ _ ^bytes payload]
        (handler (read-string (String. payload "UTF-8"))))
      :auto-ack true)))