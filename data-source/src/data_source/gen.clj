(ns data-source.gen
  (:require [clojure.set]))

(def max-wait 30)
(def states ["ns" "we"])

(defn rand-state [] (rand-nth states))
(defn flip-state [state]
  (first (clojure.set/difference (set states) (list state))))

(defn next-state [t state]
  (if (> t 0)
    [(dec t) state]
    [(rand-int max-wait) (flip-state state)]))
