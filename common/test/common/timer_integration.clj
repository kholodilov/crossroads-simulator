(ns common.timer-integration
  (:require [clojure.test :refer :all]
            [common.timer     :as timer]
            [common.events    :as events]
            [common.service   :as service]))

(deftest ^:integration test-timer-service
  (let [event-service (events/build-esper-service "test-timer-service")
        timer-service (timer/run-timer event-service 100)]
    (Thread/sleep 250)
    (let [current-time (events/current-time event-service)]
      (service/stop timer-service)
      (service/stop event-service)
      (is (= 200 current-time))
      (is (= 200 (timer/get-time timer-service))))))
