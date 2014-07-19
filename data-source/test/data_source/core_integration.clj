(ns data-source.core-integration
  (:require [clojure.test :refer :all]
            [data-source.core  :as ds]
            [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.consumers :as lc])
  (:use [clojure.java.shell :only (sh)]))

(defn handler [atom]
  (fn [ch _ ^bytes payload] 
    (swap! atom conj (read-string (String. payload "UTF-8")))))

(deftest ^:integration test-switch-events-source
  (let [queue "test-switch-events-source"
        result (atom [])
        conn (rmq/connect)
        ch (lch/open conn)
        _ (lq/declare ch queue)
        _ (lc/subscribe ch queue (handler result))
        stop-source (ds/run 2 1 queue)]
    (Thread/sleep 2500)
    (stop-source)
    (println @result)
    (is (= 6 (count @result)))))
