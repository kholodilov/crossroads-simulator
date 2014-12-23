(ns common.test
  (:require [common.service :as service]
            [common.events  :as events]))

(defn wait-and-pull-events-fn [event-service stmt]
  (fn []
    (Thread/sleep 150) ; wait a moment
    (events/pull-events event-service stmt)))
