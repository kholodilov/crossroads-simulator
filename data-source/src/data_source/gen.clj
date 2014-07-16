(ns data-source.gen
  (:require [clojure.set]))

(def max-wait 30)
(def states ["ns" "we"])

(defn flip-state [state]
  (first (clojure.set/difference (set states) (list state))))

(defn next-state [t state]
  (if (> t 0)
    [(dec t) state]
    [(rand-int max-wait) (flip-state state)]))

(defn initial-switch-events [width height] 
  (for [x (range width) y (range height)]
    [x y 0 (rand-nth states)]))

(defn next-switch-events [switch-events]
  (map
    #(apply (fn [x y t state] (vec (concat [x y] (next-state t state)))) %) 
    switch-events))