(ns service.web-integration
  (:require [clojure.test :refer :all]
            [gniazdo.core     :as ws]
            [org.httpkit.client :as http-kit]
            [service.web      :as web]
            [common.events    :as events]
            [common.service   :as service]))

(defn handler [atom]
  #(swap! atom conj (read-string %)))

(defn http-get [url]
  (read-string (String. (.bytes (:body @(http-kit/get url))))))

(deftest ^:integration test-query-websocket
  (let [event-service (events/build-esper-service "test-query-websocket")
        web-service (web/start-web-service event-service {:port 3000})
        query-result (atom [])
        query-ws (ws/connect "ws://localhost:3000/query"
                    :on-receive (handler query-result))
        switch-events [{:x 0 :y 0 :phase-time 9 :phase-length 20 :direction "ns"}
                       {:x 0 :y 1 :phase-time 9 :phase-length 20 :direction "we"}
                       {:x 1 :y 0 :phase-time 9 :phase-length 20 :direction "ns"}
                       {:x 1 :y 1 :phase-time 9 :phase-length 20 :direction "we"}]]

    (ws/send-msg query-ws "select * from SwitchEvent")
    (Thread/sleep 1000)
    (doseq [event switch-events]
      (events/trigger-event event-service events/SwitchEvent event))
    (Thread/sleep 1000)

    (ws/close query-ws)
    (service/stop web-service)
    (service/stop event-service)

    (is (= (set switch-events) (set @query-result)))))

(deftest ^:integration test-current-state
  (let [event-service (events/build-esper-service "test-current-state")
        web-service (web/start-web-service event-service {:port 3000})
        switch-events [{:x 0 :y 0 :phase-time 9 :phase-length 20 :direction "ns"}
                       {:x 0 :y 1 :phase-time 9 :phase-length 20 :direction "we"}
                       {:x 1 :y 0 :phase-time 9 :phase-length 20 :direction "ns"}
                       {:x 1 :y 1 :phase-time 9 :phase-length 20 :direction "we"}]
        result (atom nil)]

    (doseq [event switch-events]
      (events/trigger-event event-service events/SwitchEvent event))
    (Thread/sleep 1000)

    (reset! result (http-get "http://localhost:3000/state"))

    (service/stop web-service)
    (service/stop event-service)

    (is (= 2 (:width @result)))
    (is (= 2 (:height @result)))
    (is (= (set switch-events) (set (:switch-times @result))))))

(deftest ^:integration test-current-state-initial
  (let [event-service (events/build-esper-service "test-current-state")
        web-service (web/start-web-service event-service {:port 3000})
        result (atom nil)]

    (reset! result (http-get "http://localhost:3000/state"))

    (service/stop web-service)
    (service/stop event-service)

    (is (= 1 (:width @result)))
    (is (= 1 (:height @result)))
    (is (empty (:switch-times @result)))))
