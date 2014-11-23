(ns common.events
  (:require [common.service :as service]
            [clj-esper.core :as esper]))

(defprotocol EventDriven
  (trigger-event [this event-type event-attrs])
  (subscribe [this selector listener-fn])
  (unsubscribe [this subscription])
  (pull-events [this selector]))

(esper/defevent SwitchEvent [x :int y :int t :int direction :string])

(defn build-esper-service [name]
  (let [esper-conf (esper/create-configuration [SwitchEvent])
        esper-service (esper/create-service name esper-conf)]
    (reify
      EventDriven
        (trigger-event [this event-type event-attrs]
          (esper/trigger-event esper-service event-type event-attrs))
        (subscribe [this selector listener-fn]
          (let [statement (esper/create-statement esper-service selector)
                listener (esper/create-listener listener-fn)]
            (esper/attach-listener statement listener)
            {:statement statement :listener listener}))
        (unsubscribe [this subscription]
          (let [{:keys [statement listener]} subscription]
            (esper/detach-listener statement listener)
            (.destroy statement)))
        (pull-events [this selector]
          (let [statement (esper/create-statement esper-service selector)
                result (esper/pull-events statement)]
            (.destroy statement)))
      service/Stoppable
        (stop [this]
          (.destroy esper-service))
    )
  )
)