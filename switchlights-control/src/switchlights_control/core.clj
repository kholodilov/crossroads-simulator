(ns switchlights-control.core
  (:require [common.events      :as events]
            [common.service     :as service]
            [switchlights-control.control    :as control]))

(def select-phase-length-fn-builder
  {"static"
      control/build-phase-length-static-fn
   "controlled"
      control/build-phase-length-controlled-fn})

(def select-update-phase-length-fn-builder
  {"frequent"
      control/build-update-phase-length-frequent-fn?
   "on-switch"
      control/build-update-phase-length-on-switch-fn?})

(defn run-switchlights [event-service width height max-phase-length {:keys [phase-length-mode phase-length-update-mode] :as params}]
  (let [switch-events (atom (control/initial-switch-events width height max-phase-length))
        phase-length-fn ((select-phase-length-fn-builder phase-length-mode) max-phase-length)
        update-phase-length-fn?
                        ((select-update-phase-length-fn-builder phase-length-update-mode) params)
        queues-statement (events/create-statement event-service "select * from QueueEvent.std:unique(x, y, direction)")
        generate-and-trigger-switch-events
          (fn [_]
            (let [queues (events/pull-events event-service queues-statement)
                  next-switch-events-fn (control/build-next-switch-events-fn queues phase-length-fn update-phase-length-fn?)] 
              (swap! switch-events next-switch-events-fn)
              (doseq [event @switch-events]
                (events/trigger-event event-service events/SwitchEvent event))))
        timer-statement (events/create-statement event-service (str "select * from pattern[every timer:interval(1 sec)]"))]
    (events/subscribe event-service timer-statement generate-and-trigger-switch-events)
    (service/build-service :stop-fn #(events/destroy-statement event-service timer-statement))))
