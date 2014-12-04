(ns switchlights-control.core
  (:require [common.events      :as events]
            [common.service     :as service]
            [switchlights-control.gen    :as gen]))

(defn run-switchlights [event-service width height frequency]
  (let [switch-events (atom (gen/initial-switch-events width height))
        generate-and-trigger-switch-events
          (fn [_] 
            (swap! switch-events gen/next-switch-events)
            (doseq [event @switch-events]
              (events/trigger-event event-service events/SwitchEvent event)))
        timer-statement (events/create-statement event-service (str "select * from pattern[every timer:interval(" frequency " msec)]"))]
    (events/subscribe event-service timer-statement generate-and-trigger-switch-events)
    (service/build-service :stop-fn #(events/destroy-statement event-service timer-statement))))
