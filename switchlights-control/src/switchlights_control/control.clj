(ns switchlights-control.control
  (:require [clojure.set]
            [common.crossroads :as crossroads])
  (:import [CrossroadControl CrossroadControl]))

(def directions ["ns" "we"])

(defn flip-direction [direction]
  (first (clojure.set/difference (set directions) (list direction))))

(defn initial-switch-events [width height max-phase-length] 
    (for [x (crossroads/coord-range width)
          y (crossroads/coord-range height)]
      {:x x :y y :phase-time (rand-int max-phase-length) :phase-length max-phase-length :direction (rand-nth directions)}))

(defn queues-for-crossroad [x y queue-events]
  (let [queue-events-filter #(and (= (:x %) x) (= (:y %) y))
        queue-events-for-crossroad (clojure.set/select queue-events-filter (set queue-events))
        ordered-queue-events (sort-by :direction queue-events-for-crossroad)
        queues (map :queue ordered-queue-events)]
    (if (= crossroads/queues-count (count queue-events-for-crossroad))
      queues
      (do
        (println (str "ERROR: Failed to get queues for (" x ", " y ") from " queue-events-for-crossroad))
        [0 0 0 0]))))

(defn build-next-state-fn [queue-events phase-update-frequency next-phase-length-fn]
  (fn [{:keys [x y phase-time direction] :as switch-event}]
    (let [queues (queues-for-crossroad x y queue-events)
          next-phase-time (inc phase-time)
          next-phase-length (next-phase-length-fn next-phase-time direction queues)]
      (merge switch-event
        (if (>= next-phase-time next-phase-length)
          {:phase-time 0               :phase-length (next-phase-length-fn 0 (flip-direction direction) queues) :direction (flip-direction direction)}
          {:phase-time next-phase-time :phase-length next-phase-length :direction direction})))))

(defn build-next-switch-events-fn [queue-events phase-update-frequency next-phase-length-fn]
  (let [next-state-fn (build-next-state-fn queue-events phase-update-frequency next-phase-length-fn)]
    (fn [switch-events]
      (map next-state-fn switch-events))))

(defn build-next-phase-length-static-fn [max-phase-length]
  (fn [phase-time direction queues]
    max-phase-length))

(def direction-to-green-matrix {"ns" (double-array [0 1 0 1])
                                "we" (double-array [1 0 1 0])})

(defn build-next-phase-length-controlled-fn [max-phase-length]
  (let [cc (CrossroadControl. )
        model-args (object-array ["../model.RLSM.error.0.12.date.2013-10-08.mat" "" "RLSM"])
        model (vec (.calculateCrossroadModel cc 4 model-args))
        [mw_mfParams mw_mfCounts mw_fRules mw_modelParams] model
        next-phase-length-controlled-fn
          (fn [phase-time direction queues]
            (println (str "Queues: " queues ", phase-time: " phase-time ", direction: " direction ", max-phase-length: " max-phase-length))
            (let [args (object-array [
                          (double-array queues)
                          (double max-phase-length)
                          (direction-to-green-matrix direction)
                          mw_mfParams
                          mw_mfCounts
                          mw_fRules
                          mw_modelParams
                          "fminbnd"])
                  [mw_PhaseLength] (vec (.getPhaseLength cc 1 args))
                  next-phase-length (.getInt mw_PhaseLength)]
              (.dispose mw_PhaseLength)
              next-phase-length))]
    (println "INFO: CrossroadControl model ready")
    (next-phase-length-controlled-fn 1 "ns" [1 2 3 4])
    (println "INFO: CrossroadControl predictor warmed up")
    next-phase-length-controlled-fn
  ))
