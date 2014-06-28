(ns service.core-integration
  (:require [clojure.test :refer :all]
            [service.core :as service]
            [gniazdo.core :as ws]))

(defn handler [atm]
  #(swap! atm conj (read-string %)))

(deftest test-events-websocket
  (let [stop-service (service/run 2 2)
        events (atom [])
        events-ws (ws/connect "ws://localhost:3000/events"
                    :on-receive (handler events))]
    (Thread/sleep 3000)
    (ws/close events-ws)
    (is (= (count @events) 12))
    (stop-service)))
