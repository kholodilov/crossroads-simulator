(ns switchlights-control.core
  (:require [common.events      :as events]
            [common.service     :as service]
            [switchlights-control.control    :as control]))

(defn run-switchlights [event-service width height max-phase-length phase-update-frequency]
  (let [switch-events (atom (control/initial-switch-events width height max-phase-length))
        next-phase-length-fn (control/build-next-phase-length-static-fn max-phase-length)
        queues-statement (events/create-statement event-service "select * from QueueEvent.std:unique(x, y, direction)")
        generate-and-trigger-switch-events
          (fn [_]
            (let [queues (events/pull-events event-service queues-statement)
                  next-switch-events-fn (control/build-next-switch-events-fn queues max-phase-length phase-update-frequency next-phase-length-fn)] 
              (swap! switch-events next-switch-events-fn)
              (doseq [event @switch-events]
                (events/trigger-event event-service events/SwitchEvent event))))
        timer-statement (events/create-statement event-service (str "select * from pattern[every timer:interval(1 sec)]"))]
    (events/subscribe event-service timer-statement generate-and-trigger-switch-events)
    (service/build-service :stop-fn #(events/destroy-statement event-service timer-statement))))
