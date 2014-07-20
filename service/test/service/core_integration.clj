(ns service.core-integration
  (:require [clojure.test :refer :all]
            [service.core :as service]
            [gniazdo.core :as ws]
            [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.basic     :as lb]
            [langohr.queue     :as lq]))

(def default-exchange "")

(defn handler [atom]
  #(swap! atom conj (read-string %)))

(deftest ^:integration test-query-websocket
  (let [queue "test-query-websocket"
        conn (rmq/connect)
        ch (lch/open conn)
        _ (lq/declare ch queue)
        _ (lq/purge ch queue)
        stop-service (service/run 2 2 queue)
        query-result (atom [])
        query-ws (ws/connect "ws://localhost:3000/query"
                    :on-receive (handler query-result))
        switch-events [{:x 0 :y 0 :t 1 :direction "ns"}
                       {:x 0 :y 1 :t 2 :direction "we"}
                       {:x 1 :y 0 :t 3 :direction "ns"}
                       {:x 1 :y 1 :t 4 :direction "we"}]]
    (ws/send-msg query-ws "select * from SwitchEvent")
    (doseq [event switch-events]
      (lb/publish ch default-exchange queue (pr-str event)))
    (Thread/sleep 1000)
    (ws/close query-ws)
    (stop-service)
    (rmq/close ch)
    (rmq/close conn)
    (is (= @query-result switch-events))))
