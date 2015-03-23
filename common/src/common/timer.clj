(ns common.timer
  (:require [ruiyun.tools.timer :as timer]
            [common.events      :as events]
            [common.service     :as service]))

(defn run-timer [event-service period]
  (let [time (atom 0)
        timer (timer/timer)
        timer-fn
          (fn []
            (swap! time + period)
            (events/do-timestep event-service (deref time)))]
      (timer/run-task! timer-fn :period period :by timer :delay period)
      (service/build-service :conn {:get-time #(deref time)} :stop-fn #(timer/cancel! timer))
    ))

(defn get-time [timer-service]
  ((:get-time (:conn timer-service))))