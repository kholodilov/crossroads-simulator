(ns service.gen
  (:require [common.events      :as events]
            [common.service     :as service]))

(def vehicle-events-ns
  [{:x 1 :y 1 :direction 4}
   {:x 1 :y 2 :direction 2}
   {:x 2 :y 1 :direction 4}
   {:x 2 :y 2 :direction 2}
   {:x 3 :y 1 :direction 4}
   {:x 3 :y 2 :direction 2}])     

(def vehicle-events-we
  [{:x 1 :y 1 :direction 1}
   {:x 3 :y 1 :direction 3}
   {:x 1 :y 2 :direction 1}
   {:x 3 :y 2 :direction 3}])

(defn trigger-vehicle-events-fn [event-service events]
  (fn [_]
    (doseq [event events]
      (events/trigger-event event-service events/VehicleEvent event))))

(defn run-vehicles-generation [event-service]
  (let [timer-statement-ns
          (events/create-statement event-service "select * from pattern[every timer:interval(1 sec)] where Math.pow(Math.sin(current_timestamp), 2) > 0.85")
          ;(events/create-statement event-service "select * from pattern[every timer:interval(4 sec)]")
        timer-statement-we
          (events/create-statement event-service "select * from pattern[every timer:interval(1 sec)] where Math.pow(Math.sin(current_timestamp), 2) > 0.962")]
          ;(events/create-statement event-service "select * from pattern[every timer:interval(8 sec)]")]
    (events/subscribe event-service timer-statement-ns (trigger-vehicle-events-fn event-service vehicle-events-ns))
    (events/subscribe event-service timer-statement-we (trigger-vehicle-events-fn event-service vehicle-events-we))
    (service/build-service
      :stop-fn (fn []
        (events/destroy-statement event-service timer-statement-we)
        (events/destroy-statement event-service timer-statement-ns)))))
