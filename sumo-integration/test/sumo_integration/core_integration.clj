(ns sumo-integration.core-integration
  (:require [clojure.test :refer :all]
            [common.test :refer :all]
            [common.events  :as events]
            [common.service :as service]
            [sumo-integration.core :as sumo]))

(deftest ^:integration test-sumo-service
  (let [event-service (events/build-esper-service "test-sumo-service")
        count-event-stmt (events/create-statement event-service "select * from TotalVehiclesCountEvent.std:lastevent()")
        pull-vehicles-count-events (wait-and-pull-events-fn event-service count-event-stmt)
        vehicles-count #(get (first %) :count 0)
        sumo (sumo/run-sumo event-service "../simulation_grid/config.sumo.cfg" 3 2 :cli 1000)]

    (is (= 0 (vehicles-count (pull-vehicles-count-events))))
    (events/do-timestep event-service 1000)
    (is (= 0 (vehicles-count (pull-vehicles-count-events))))
    (events/do-timestep event-service 2000)
    (is (= 1 (vehicles-count (pull-vehicles-count-events))))
    (events/do-timestep event-service 3000)
    (is (= 2 (vehicles-count (pull-vehicles-count-events))))
    (events/do-timestep event-service 4000)
    (is (= 3 (vehicles-count (pull-vehicles-count-events))))

    (service/stop sumo)
    (service/stop event-service)))

(deftest ^:integration test-switchlights
  (let [event-service (events/build-esper-service "test-switchlights")
        sumo (sumo/run-sumo event-service "../simulation_grid/config.sumo.cfg" 3 2 :cli 1000)]

    (events/trigger-event event-service events/SwitchEvent {:x 1 :y 1 :t 50 :direction "ns"})
    (Thread/sleep 100)
    (let [tl (sumo/retrieve-tl (:conn sumo) "1/1")]
      (is (= 50000 (:remaining-duration tl)))
      (is (= "GGgrrrGGgrrr" (:state tl))))

    (service/stop sumo)
    (service/stop event-service)))
