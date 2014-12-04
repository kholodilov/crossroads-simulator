(ns switchlights-control.core-integration
  (:require [clojure.test :refer :all]
            [common.test :refer :all]
            [switchlights-control.core :as SUT]
            [common.events    :as events]
            [common.service   :as service]))

(deftest ^:integration test-switchlights-control-service
  (let [event-service (events/build-esper-service "test-switchlights-control-service")
        switch-events-stmt (events/create-statement event-service "select * from SwitchEvent.win:keepall()")
        pull-switch-events (wait-and-pull-events-fn event-service switch-events-stmt)
        switchlights-service (SUT/run-switchlights event-service 2 2 1000)]

    (events/do-timestep event-service 1000)
    (is (= 4 (count (pull-switch-events))))

    (events/do-timestep event-service 2000)
    (is (= 8 (count (pull-switch-events))))

    (events/do-timestep event-service 3000)
    (is (= 12 (count (pull-switch-events))))

    (service/stop switchlights-service)
    (service/stop event-service)))
