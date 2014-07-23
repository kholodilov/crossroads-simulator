(ns data-source.gen
  (:require [clojure.set]))

(def max-wait 30)
(def directions ["ns" "we"])

(defn flip-direction [direction]
  (first (clojure.set/difference (set directions) (list direction))))

(defn next-state [{:keys [t direction] :as state}]
  (merge state
    (if (> t 0)
      {:t (dec t) :direction direction}
      {:t max-wait :direction (flip-direction direction)})))

(defn initial-switch-events [width height] 
  (for [x (range width) y (range height)]
    {:x x :y y :t (rand-int max-wait) :direction (rand-nth directions)}))

(defn next-switch-events [switch-events]
  (map next-state switch-events))