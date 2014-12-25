(ns service.gen
  (:require [common.events      :as events]
            [common.service     :as service]))

(defn trigger-vehicle-events-fn [event-service]
  (fn [_]
    (let [event (rand-nth
      [{:x 1 :y 1 :direction 1}
       {:x 3 :y 1 :direction 3}
       {:x 1 :y 2 :direction 1}
       {:x 3 :y 2 :direction 3}
       {:x 1 :y 1 :direction 4}
       {:x 1 :y 2 :direction 2}
       {:x 2 :y 1 :direction 4}
       {:x 2 :y 2 :direction 2}
       {:x 3 :y 1 :direction 4}
       {:x 3 :y 2 :direction 2}])]
      (doseq [i (range 2)] (events/trigger-event event-service events/VehicleEvent event)))))

(defn run-vehicles-generation [event-service]
  (let [timer-statement
          (events/create-statement event-service "select * from pattern[every timer:interval(1 sec)]")]
    (events/subscribe event-service timer-statement (trigger-vehicle-events-fn event-service))
    (service/build-service :stop-fn #(events/destroy-statement event-service timer-statement))))
