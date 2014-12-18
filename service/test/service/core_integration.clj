(ns service.core-integration
  (:require [clojure.test :refer :all]
            [service.core]
            [common.events    :as events]
            [common.service   :as service]
            [gniazdo.core     :as ws]))

(defn handler [atom]
  #(swap! atom conj (read-string %)))

(deftest ^:integration test-simulation
  (let [simulation (service.core/run-simulation "../simulation_grid/config.sumo.cfg" 3 2 40 :cli)
        query-result (atom [])
        query-ws (ws/connect "ws://localhost:3000/query"
                    :on-receive (handler query-result))]

    (ws/send-msg query-ws "select * from SwitchEvent.std:unique(x,y)")
    (Thread/sleep 1000)

    (ws/close query-ws)
    (service/stop simulation)

    (println @query-result)
    (is (= 6 (count @query-result)))))

(deftest ^:integration test-timer-service
  (let [event-service (events/build-esper-service "test-timer-service")
        timer-service (service.core/run-timer event-service 100)]
    (Thread/sleep 250)
    (let [current-time (events/current-time event-service)]
      (service/stop timer-service)
      (service/stop event-service)
      (is (= 200 current-time)))))
