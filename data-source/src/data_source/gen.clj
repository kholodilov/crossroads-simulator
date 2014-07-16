(ns data-source.gen
  (:require [clojure.set]))

(def max-wait 30)
(def states ["ns" "we"])


(defn rand-state [] (rand-nth states))
(defn flip-state [state]
  (first (clojure.set/difference (set states) (list state))))
