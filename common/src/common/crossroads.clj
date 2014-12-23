(ns common.crossroads)

(def queues-count 4)
(def queues-directions (range 1 (+ queues-count 1)))

(defn coord-range [dimension-size] (range 1 (+ dimension-size 1)))

(defn max-dimension-size [switch-events dimension]
  (apply max (or (seq (map dimension switch-events)) [1])))

