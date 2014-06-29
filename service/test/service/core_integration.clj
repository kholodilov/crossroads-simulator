(ns service.core-integration
  (:require [clojure.test :refer :all]
            [service.core :as service]
            [gniazdo.core :as ws]))

(defn handler [atom]
  #(swap! atom conj (read-string %)))

(deftest test-query-websocket
  (let [stop-service (service/run 2 2)
        query-result (atom [])
        query-ws (ws/connect "ws://localhost:3000/query"
                    :on-receive (handler query-result))]
    (ws/send-msg query-ws "select count(*) count from SwitchEvent output last every 1 seconds")
    (Thread/sleep 4500)
    (ws/close query-ws)
    (is (= @query-result [{:count 4} {:count 8} {:count 12}]))
    (stop-service)))
