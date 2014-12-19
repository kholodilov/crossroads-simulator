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

; (defn next-state-fn [queues next-phase-length-fn]
;   (fn [{:keys [phase-length phase-time direction] :as state}]
;     (merge state
;       (if (< phase-time phase-length)
;         {:phase-time (inc phase-time) :phase-length phase-length :direction direction}
;         {:phase-time 1                :phase-length phase-length :direction (flip-direction direction)}))))

; (defn next-switch-events-fn [queues next-phase-length-fn]
;   (let [next-state (next-state-fn queues next-phase-length-fn)]
;     (fn [switch-events]
;       (map next-state switch-events))))

; (defn next-phase-length-const-fn [full-cycle-time model]
;   (fn [phase-time direction queues]
;     (/ full-cycle-time 2)))
