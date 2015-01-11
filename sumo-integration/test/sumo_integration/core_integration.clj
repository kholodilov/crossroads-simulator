(ns sumo-integration.core-integration
  (:require [clojure.test :refer :all]
            [common.test :refer :all]
            [common.events  :as events]
            [common.service :as service]
            [common.crossroads     :as crossroads]
            [sumo-integration.core :as sumo]))

(def width 3)
(def height 2)
(def step-length 1000)

(def incoming-vehicle-events (crossroads/incoming-directions width height))

(deftest ^:integration test-vehicles
  (let [event-service (events/build-esper-service "test-vehicles")
        vehicles-count-events-stmt (events/create-statement event-service "select * from TotalVehiclesCountEvent.std:lastevent()")
        pull-vehicles-count-events (wait-and-pull-events-fn event-service vehicles-count-events-stmt)
        vehicles-count #(get (first (pull-vehicles-count-events)) :count)
        sumo (sumo/run-sumo event-service "../simulation_grid/config.sumo.cfg" width height :cli step-length)]

    (is (= nil (vehicles-count)))
    (events/do-timestep event-service 1000)
    (is (= 0 (vehicles-count)))
    (doseq [event incoming-vehicle-events
            i (range 10)]
      (events/trigger-event event-service events/VehicleEvent event))
    (wait-a-moment)
    (events/do-timestep event-service 2000)
    (is (= (* (count incoming-vehicle-events) 10) (vehicles-count)))

    (service/stop sumo)
    (service/stop event-service)))

(deftest ^:integration test-queues
  (let [event-service (events/build-esper-service "test-queues")
        queue-events-stmt (events/create-statement event-service "select * from QueueEvent.win:keepall()")
        pull-queue-events (wait-and-pull-events-fn event-service queue-events-stmt)
        sumo (sumo/run-sumo event-service "../simulation_grid/config.sumo.cfg" width height :cli step-length)]

    (is (= 0 (count (pull-queue-events))))
    (events/do-timestep event-service 1000)
    (is (= 24 (count (pull-queue-events))))
    (events/do-timestep event-service 2000)
    (is (= 48 (count (pull-queue-events))))

    (service/stop sumo)
    (service/stop event-service)))

(deftest ^:integration test-switchlights
  (let [event-service (events/build-esper-service "test-switchlights")
        sumo (sumo/run-sumo event-service "../simulation_grid/config.sumo.cfg" width height :cli step-length)]

    (events/trigger-event event-service events/SwitchEvent {:x 1 :y 1 :phase-time 9 :phase-length 20 :direction "ns"})
    (Thread/sleep 100)
    (let [tl (sumo/retrieve-tl (:conn sumo) "1/1")]
      (is (= 11000 (:remaining-duration tl)))
      (is (= "GGgrrrGGgrrr" (:state tl))))

    (service/stop sumo)
    (service/stop event-service)))
