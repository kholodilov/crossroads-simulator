(ns common.crossroads)

(defn coord-range [dimension-size] (range dimension-size))
(defn max-coord [dimension-size] (- dimension-size 1))

(defn max-dimension-size [switch-events dimension]
  (+ (apply max (or (seq (map dimension switch-events)) [0])) 1))

(def queues-count 4)
(def queues-directions (range 1 (+ queues-count 1)))

(defn incoming-directions-ns [width height]
  (flatten
    (for [x (coord-range width)]
      [{:x x :y 0 :direction 4}
       {:x x :y (max-coord height) :direction 2}])))

(defn incoming-directions-we [width height]
  (flatten
    (for [y (coord-range height)]
      [{:x 0 :y y :direction 1}
       {:x (max-coord width) :y y :direction 3}])))

(defn incoming-directions [width height]
  (concat
    (incoming-directions-ns width height)
    (incoming-directions-we width height)))

