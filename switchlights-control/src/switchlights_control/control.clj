(ns switchlights-control.control
  (:require [clojure.set]
            [common.crossroads :as crossroads]))

(def max-cycle 30)
(def directions ["ns" "we"])

(defn flip-direction [direction]
  (first (clojure.set/difference (set directions) (list direction))))

(defn next-state [{:keys [phase-length phase-time direction] :as state}]
  (merge state
    (if (< (inc phase-time) phase-length)
      {:phase-time (inc phase-time) :phase-length phase-length :direction direction}
      {:phase-time 0                :phase-length phase-length  :direction (flip-direction direction)})))

(defn initial-switch-events [width height] 
    (for [x (crossroads/coord-range width)
          y (crossroads/coord-range height)]
      {:x x :y y :phase-time (rand-int max-cycle) :phase-length max-cycle :direction (rand-nth directions)}))

(defn next-switch-events [switch-events]
  (map next-state switch-events))