(ns switchlights-control.control
  (:require [clojure.set]
            [common.crossroads :as crossroads]))

(def directions ["ns" "we"])

(defn flip-direction [direction]
  (first (clojure.set/difference (set directions) (list direction))))

(defn next-state [{:keys [phase-length phase-time direction] :as state}]
  (merge state
    (if (< phase-time phase-length)
      {:phase-time (inc phase-time) :phase-length phase-length :direction direction}
      {:phase-time 1                :phase-length phase-length  :direction (flip-direction direction)})))

(defn initial-switch-events [width height full-cycle-time] 
    (for [x (crossroads/coord-range width)
          y (crossroads/coord-range height)]
      {:x x :y y :phase-time (+ (rand-int (/ full-cycle-time 2)) 1) :phase-length (/ full-cycle-time 2) :direction (rand-nth directions)}))

(defn next-switch-events [switch-events]
  (map next-state switch-events))

(defn build-next-state-fn [queues full-cycle-time next-phase-length-fn]
  (fn [{:keys [phase-time direction] :as state}]
    (let [next-phase-time (inc phase-time)
          next-phase-length (next-phase-length-fn phase-time direction queues)]
      (merge state
        (if (< next-phase-time next-phase-length)
          {:phase-time next-phase-time :phase-length next-phase-length :direction direction}
          {:phase-time 0               :phase-length (- full-cycle-time next-phase-length) :direction (flip-direction direction)})))))

(defn build-next-switch-events-fn [queues full-cycle-time next-phase-length-fn]
  (let [next-state-fn (build-next-state-fn queues full-cycle-time next-phase-length-fn)]
    (fn [switch-events]
      (map next-state-fn switch-events))))

(defn build-next-phase-length-const-fn [full-cycle-time]
  (fn [phase-time direction queues]
    (/ full-cycle-time 2)))
