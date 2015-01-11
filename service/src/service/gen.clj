(ns service.gen
  (:require [common.events      :as events]
            [common.service     :as service]
            [common.crossroads  :as crossroads]))

(defn trigger-vehicle-events-fn [event-service events]
  (fn [_]
    (doseq [event events]
      (events/trigger-event event-service events/VehicleEvent event))))

(defn run-vehicles-generation [event-service width height]
  (let [timer-statement-ns
          (events/create-statement event-service "select * from pattern[every timer:interval(1 sec)] where Math.pow(Math.sin(current_timestamp), 2) > 0.85")
          ;(events/create-statement event-service "select * from pattern[every timer:interval(4 sec)]")
        timer-statement-we
          (events/create-statement event-service "select * from pattern[every timer:interval(1 sec)] where Math.pow(Math.sin(current_timestamp), 2) > 0.962")]
          ;(events/create-statement event-service "select * from pattern[every timer:interval(8 sec)]")]
    (events/subscribe event-service timer-statement-ns (trigger-vehicle-events-fn event-service (crossroads/incoming-directions-ns width height)))
    (events/subscribe event-service timer-statement-we (trigger-vehicle-events-fn event-service (crossroads/incoming-directions-we width height)))
    (service/build-service
      :stop-fn (fn []
        (events/destroy-statement event-service timer-statement-we)
        (events/destroy-statement event-service timer-statement-ns)))))
