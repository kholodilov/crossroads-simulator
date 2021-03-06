(ns switchlights-control.core-integration
  (:require [clojure.test :refer :all]
            [common.test :refer :all]
            [switchlights-control.core :as SUT]
            [switchlights-control.control :as control]
            [common.events    :as events]
            [common.service   :as service]
            [common.crossroads :as crossroads]))

(def width 2)
(def height 2)
(def max-phase-length 20)

(deftest ^:integration test-switchlights-control-service
  (let [event-service (events/build-esper-service "test-switchlights-control-service")
        switch-events-stmt (events/create-statement event-service "select * from SwitchEvent.win:keepall()")
        pull-switch-events (wait-and-pull-events-fn event-service switch-events-stmt)
        switchlights-service (SUT/run-switchlights event-service width height max-phase-length {:phase-length-mode "static" :phase-length-update-mode "on-switch"})]

    (doseq [x (crossroads/coord-range 2)
            y (crossroads/coord-range 2)
            crossroads-direction (crossroads/list-directions x y)]
      (events/trigger-event event-service events/QueueEvent (events/queue-event crossroads-direction :queue 0)))

    (Thread/sleep 3000)
    (println "INFO: starting switchlights-control service test")

    (events/do-timestep event-service 1000)
    (is (= 4 (count (pull-switch-events))))

    (events/do-timestep event-service 2000)
    (is (= 8 (count (pull-switch-events))))

    (events/do-timestep event-service 3000)
    (is (= 12 (count (pull-switch-events))))

    (service/stop switchlights-service)
    (service/stop event-service)))

(deftest ^:integration phase-length-controlled-smoke-test
  (control/build-phase-length-controlled-fn max-phase-length))
