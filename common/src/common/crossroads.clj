(ns common.crossroads)

(defn coord-range [dimension-size] (range dimension-size))
(defn min-coord [] 0)
(defn max-coord [dimension-size] (- dimension-size 1))

(defn max-dimension-size [switch-events dimension]
  (+ (apply max (or (seq (map dimension switch-events)) [0])) 1))

(def queues-count 4)
(def queues-directions [1 2 3 4])

(defn crossroads-direction [& {:keys [x y direction]}]
  {:x x :y y :direction direction})
(defn list-directions [x y]
  (for [direction queues-directions]
    (crossroads-direction :x x :y y :direction direction)))

(defn incoming-directions-ns [width height]
  (flatten
    (for [x (coord-range width)]
      [(crossroads-direction :x x :y 0 :direction 4)
       (crossroads-direction :x x :y (max-coord height) :direction 2)])))

(defn incoming-directions-we [width height]
  (flatten
    (for [y (coord-range height)]
      [(crossroads-direction :x 0 :y y :direction 1)
       (crossroads-direction :x (max-coord width) :y y :direction 3)])))

(defn incoming-directions [width height]
  (concat
    (incoming-directions-ns width height)
    (incoming-directions-we width height)))

