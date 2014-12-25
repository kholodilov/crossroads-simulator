(ns sumo-integration.core-integration
  (:require [clojure.test :refer :all]
            [common.test :refer :all]
            [common.events  :as events]
            [common.service :as service]
            [sumo-integration.core :as sumo]))

(deftest ^:integration test-sumo-service
  (let [event-service (events/build-esper-service "test-sumo-service")
        vehicles-count-events-stmt (events/create-statement event-service "select * from TotalVehiclesCountEvent.std:lastevent()")
        pull-vehicles-count-events (wait-and-pull-events-fn event-service vehicles-count-events-stmt)
        vehicles-count #(get (first (pull-vehicles-count-events)) :count)
        sumo (sumo/run-sumo event-service "../simulation_grid/config.sumo.cfg" 3 2 :cli 100)]

    (is (= nil (vehicles-count)))
    (events/do-timestep event-service 1000)
    (is (= 0 (vehicles-count)))
    (doseq [event
        [{:x 1 :y 1 :direction 1}
         {:x 3 :y 1 :direction 3}
         {:x 1 :y 2 :direction 1}
         {:x 3 :y 2 :direction 3}
         {:x 1 :y 1 :direction 4}
         {:x 1 :y 2 :direction 2}
         {:x 2 :y 1 :direction 4}
         {:x 2 :y 2 :direction 2}
         {:x 3 :y 1 :direction 4}
         {:x 3 :y 2 :direction 2}]
         i (range 90)]
      (events/trigger-event event-service events/VehicleEvent event))
    (wait-a-moment)
    (events/do-timestep event-service 2000)
    (is (= 90 (vehicles-count)))

    (service/stop sumo)
    (service/stop event-service)))

(deftest ^:integration test-switchlights
  (let [event-service (events/build-esper-service "test-switchlights")
        sumo (sumo/run-sumo event-service "../simulation_grid/config.sumo.cfg" 3 2 :cli 100)]

    (events/trigger-event event-service events/SwitchEvent {:x 1 :y 1 :phase-time 9 :phase-length 20 :direction "ns"})
    (Thread/sleep 100)
    (let [tl (sumo/retrieve-tl (:conn sumo) "1/1")]
      (is (= 11000 (:remaining-duration tl)))
      (is (= "GGgrrrGGgrrr" (:state tl))))

    (service/stop sumo)
    (service/stop event-service)))
