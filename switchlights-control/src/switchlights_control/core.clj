(ns switchlights-control.core
  (:require [common.events      :as events]
            [common.service     :as service]
            [switchlights-control.control    :as control]))

(defn run-switchlights [event-service width height full-cycle-time frequency]
  (let [switch-events (atom (control/initial-switch-events width height full-cycle-time))
        generate-and-trigger-switch-events
          (fn [_] 
            (swap! switch-events control/next-switch-events)
            (doseq [event @switch-events]
              (events/trigger-event event-service events/SwitchEvent event)))
        timer-statement (events/create-statement event-service (str "select * from pattern[every timer:interval(" frequency " msec)]"))]
    (events/subscribe event-service timer-statement generate-and-trigger-switch-events)
    (service/build-service :stop-fn #(events/destroy-statement event-service timer-statement))))
