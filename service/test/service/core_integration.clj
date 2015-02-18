(ns service.core-integration
  (:require [clojure.test :refer :all]
            [service.core]
            [common.events    :as events]
            [common.service   :as service]
            [gniazdo.core     :as ws]))

(defn handler [atom]
  #(swap! atom conj (read-string %)))

(def width 3)
(def height 2)
(def max-phase-length 20)

(deftest ^:integration test-simulation
  (let [simulation (service.core/run-simulation "../simulation_grid/config.sumo.cfg" 
                                                width height max-phase-length
                                                {:phase-length-mode "static" :phase-length-update-mode "on-switch"}
                                                :cli)
        query-result (atom [])
        query-ws (ws/connect "ws://localhost:3000/query"
                    :on-receive (handler query-result))]

    (ws/send-msg query-ws "select * from SwitchEvent.std:unique(x,y)")
    (Thread/sleep 1000)

    (ws/close query-ws)
    (service/stop simulation)

    (println @query-result)
    (is (= 6 (count @query-result)))))
