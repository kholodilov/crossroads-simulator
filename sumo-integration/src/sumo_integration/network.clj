(ns sumo-integration.network
  (:require [common.crossroads :as crossroads]
            [clojure.core.match :refer (match)]))

(defn crossroads-id [x y] (str x "/" y))

(defn route-id [crossroads-direction]
  (let [{:keys [x y direction]} crossroads-direction]
    (str "r" x "/" y "_" direction)))

(defn- neighbour-shift [direction]
  ({1 {:dx -1 :dy 0}
    2 {:dx  0 :dy 1}
    3 {:dx  1 :dy 0}
    4 {:dx  0 :dy -1}}
    direction))

(defn- neighbour-crossroads-id [{:keys [x y direction]}]
  (let [{:keys [dx dy]} (neighbour-shift direction)
        x* (+ x dx)
        y* (+ y dy)]
    (crossroads-id x* y*)))

(defn- lane-src-id [crossroads-direction width height]
  (let [min-x (crossroads/min-coord)
        min-y (crossroads/min-coord)
        max-x (crossroads/max-coord width)
        max-y (crossroads/max-coord height)
        {:keys [x y]} crossroads-direction]
    (match [crossroads-direction]
      [{:x min-x :y _     :direction 1}] (str "left"   y)
      [{:x max-x :y _     :direction 3}] (str "right"  y)
      [{:x _     :y min-y :direction 4}] (str "bottom" x)
      [{:x _     :y max-y :direction 2}] (str "top"    x)
      :else (neighbour-crossroads-id crossroads-direction))))


(defn- edge [crossroads-direction width height]
  (let [{:keys [x y]} crossroads-direction
        id (crossroads-id x y)
        src-id (lane-src-id crossroads-direction width height)]
    {:from src-id :to id}))

(defn edge-id
  ([edge] (str (:from edge) "to" (:to edge)))
  ([crossroads-direction width height]
    (edge-id (edge crossroads-direction width height))))

(defn- opposite-edge [edge]
  {:from (:to edge) :to (:from edge)})

(defn opposite-edge-id [crossroads-direction width height]
    (edge-id (opposite-edge (edge crossroads-direction width height))))

(defn lane-id [crossroads-direction width height]
  (str (edge-id crossroads-direction width height) "_0"))

(defn lane-e2-id [crossroads-direction width height]
  (str "e2det_" (lane-id crossroads-direction width height)))

(defn routes
  ([width height incoming-directions-gen steps-gen]
    (for [d (incoming-directions-gen width height)]
      {
        :id (route-id d)
        :edges
          (let [steps (steps-gen d)
                last-step (last steps)]
            (clojure.string/join " "
              (conj
                (vec (for [step steps] (edge-id step width height)))
                (opposite-edge-id (crossroads/opposite-direction last-step) width height)
              )))
      }))
  ([width height]
    (concat
      (routes width height crossroads/incoming-directions-we
        (fn [d] (for [x (crossroads/coord-range width)] {:x x :y (:y d) :direction (:direction d)})))
      (routes width height crossroads/incoming-directions-ew
        (fn [d] (for [x (reverse (crossroads/coord-range width))] {:x x :y (:y d) :direction (:direction d)})))
      (routes width height crossroads/incoming-directions-sn
        (fn [d] (for [y (crossroads/coord-range height)] {:x (:x d) :y y :direction (:direction d)})))
      (routes width height crossroads/incoming-directions-ns
        (fn [d] (for [y (reverse (crossroads/coord-range height))] {:x (:x d) :y y :direction (:direction d)})))
    )))
