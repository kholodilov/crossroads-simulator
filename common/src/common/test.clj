(ns common.test
  (:require [common.service :as service]
            [common.events  :as events]))

(defn wait-a-moment []
  (Thread/sleep 150))

(defn wait-and-pull-events-fn [event-service stmt]
  (fn []
    (wait-a-moment)
    (events/pull-events event-service stmt)))
