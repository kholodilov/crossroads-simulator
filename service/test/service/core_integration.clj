(ns service.core-integration
  (:require [clojure.test :refer :all]
            [service.core]
            [common.events    :as events]
            [common.service   :as service]
            [gniazdo.core     :as ws]
            [langohr.core     :as rmq]
            [langohr.channel  :as lch]
            [langohr.basic    :as lb]
            [langohr.queue    :as lq]))

(def default-exchange "")

(defn handler [atom]
  #(swap! atom conj (read-string %)))

(deftest ^:integration test-query-websocket
  (let [queue "test-query-websocket"
        conn (rmq/connect)
        ch (lch/open conn)
        _ (lq/declare ch queue)
        _ (lq/purge ch queue)
        stop-service (service.core/run 2 2 queue)
        query-result (atom [])
        query-ws (ws/connect "ws://localhost:3000/query"
                    :on-receive (handler query-result))
        switch-events [{:x 0 :y 0 :t 1 :direction "ns"}
                       {:x 0 :y 1 :t 2 :direction "we"}
                       {:x 1 :y 0 :t 3 :direction "ns"}
                       {:x 1 :y 1 :t 4 :direction "we"}]]
    (ws/send-msg query-ws "select * from SwitchEvent")
    (Thread/sleep 1000)
    (doseq [event switch-events]
      (lb/publish ch default-exchange queue (pr-str event)))
    (Thread/sleep 1000)
    (ws/close query-ws)
    (stop-service)
    (rmq/close ch)
    (rmq/close conn)
    (is (= @query-result switch-events))))

(deftest ^:integration test-timer-service
  (let [event-service (events/build-esper-service "test-timer-service")
        statement (events/create-statement event-service "select * from TimerEvent.win:keepall()")
        timer-service (service.core/run-timer event-service 100)]
    (Thread/sleep 250)
    (service/stop timer-service)
    (let [timer-events (events/pull-events event-service statement)]
        (service/stop event-service)
        (is (= timer-events [{:time 0} {:time 100} {:time 200}]))
        )))
