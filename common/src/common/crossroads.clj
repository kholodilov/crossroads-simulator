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

(defn opposite-direction [direction]
  (merge direction
    {:direction
      ({1 3
        2 4
        3 1
        4 2}
        (:direction direction))}))

(defn incoming-directions-ns [width height]
  (for [x (coord-range width)]
     (crossroads-direction :x x :y (max-coord height) :direction 2)))

(defn incoming-directions-sn [width height]
  (for [x (coord-range width)]
     (crossroads-direction :x x :y 0 :direction 4)))

(defn incoming-directions-vertical [width height]
  (concat
    (incoming-directions-ns width height)
    (incoming-directions-sn width height)))

(defn incoming-directions-we [width height]
  (for [y (coord-range height)]
    (crossroads-direction :x 0 :y y :direction 1)))

(defn incoming-directions-ew [width height]
  (for [y (coord-range height)]
    (crossroads-direction :x (max-coord width) :y y :direction 3)))

(defn incoming-directions-horizontal [width height]
  (concat
    (incoming-directions-we width height)
    (incoming-directions-ew width height)))

(defn incoming-directions [width height]
  (concat
    (incoming-directions-vertical width height)
    (incoming-directions-horizontal width height)))

