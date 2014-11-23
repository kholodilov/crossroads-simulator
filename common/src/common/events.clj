(ns common.events
  (:require [common.service :as service]
            [clj-esper.core :as esp]))

(defprotocol EventDriven
  (trigger-event [this event-type event-attrs])
  (subscribe [this selector listener-fn])
  (unsubscribe [this subscription])
  (pull-events [this selector]))

(esp/defevent SwitchEvent [x :int y :int t :int direction :string])

(defn build-esper-service [name]
  (let [esp-conf (esp/create-configuration [SwitchEvent])
        esp-service (esp/create-service name esp-conf)]
    (reify
      EventDriven
        (trigger-event [this event-type event-attrs]
          (esp/trigger-event esp-service event-type event-attrs))
        (subscribe [this selector listener-fn]
          (let [statement (esp/create-statement esp-service selector)
                listener (esp/create-listener listener-fn)]
            (esp/attach-listener statement listener)
            {:statement statement :listener listener}))
        (unsubscribe [this subscription]
          (let [{:keys [statement listener]} subscription]
            (esp/detach-listener statement listener)
            (.destroy statement)))
        (pull-events [this selector]
          (let [statement (esp/create-statement esp-service selector)
                result (esp/pull-events statement)]
            (.destroy statement)))
      service/Stoppable
        (stop [this]
          (.destroy esp-service))
    )
  )
)