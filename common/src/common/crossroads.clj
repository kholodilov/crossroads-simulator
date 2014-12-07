(ns common.crossroads)

(defn coord-range [dimension-size] (range 1 (+ dimension-size 1)))

(defn max-dimension-size [switch-events dimension]
  (apply max (or (seq (map dimension switch-events)) [1])))

