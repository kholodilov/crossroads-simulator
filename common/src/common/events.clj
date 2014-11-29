(ns common.events
  (:require [common.service :as service]
            [clj-esper.core :as esper]))

(defprotocol EventDriven
  (trigger-event [this event-type event-attrs])
  (create-statement [this query])
  (destroy-statement [this statement])
  (subscribe [this statement listener-fn])
  (pull-events [this statement]))

(esper/defevent SwitchEvent [x :int y :int t :int direction :string])
(esper/defevent TimerEvent [time :int])

(defn build-esper-service [name]
  (let [esper-conf (esper/create-configuration [SwitchEvent TimerEvent])
        esper-service (esper/create-service name esper-conf)]
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
      service/Stoppable
        (stop [this]
          (.destroy esper-service))
    )
  )
)