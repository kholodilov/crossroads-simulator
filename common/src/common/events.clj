(ns common.events
  (:require [common.service :as service]
            [clj-esper.core :as esper]))

(defprotocol EventDriven
  (trigger-event [this event-type event-attrs])
  (create-statement [this query])
  (destroy-statement [this statement])
  (subscribe [this statement listener-fn])
  (pull-events [this statement])
  (do-timestep [this new-time])
  (current-time [this]))

(esper/defevent SwitchEvent  [x :int y :int phase-length :int phase-time :int direction :string])
(esper/defevent QueueEvent   [x :int y :int direction :int queue :int])
(esper/defevent VehicleEvent [x :int y :int direction :int])
(esper/defevent TotalVehiclesCountEvent [count :int])

(defn build-esper-service [name]
  (let [esper-conf (esper/create-configuration (esper/xml-configuration) [SwitchEvent QueueEvent VehicleEvent TotalVehiclesCountEvent])
        esper-service (esper/create-service name esper-conf)]
    (esper/send-current-time-event esper-service 0)
    (reify
      EventDriven
        (trigger-event [this event-type event-attrs]
          (esper/trigger-event esper-service event-type event-attrs))
        (create-statement [this query]
          (esper/create-statement esper-service query))
        (destroy-statement [this statement]
          (.destroy statement))
        (subscribe [this statement listener-fn]
          (esper/attach-listener statement (esper/create-listener listener-fn)))
        (pull-events [this statement]
          (esper/pull-events statement))
        (do-timestep [this new-time]
          (esper/send-current-time-event esper-service new-time))
        (current-time [this]
          (esper/get-current-time esper-service))
      service/Stoppable
        (stop [this]
          (.destroy esper-service))
    )
  )
)