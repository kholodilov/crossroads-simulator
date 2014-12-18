(ns switchlights-control.control-test
  (:require [clojure.test :refer :all]
            [switchlights-control.control :refer :all]))

(deftest flip-direction-test
  (is (= "ns" (flip-direction "we")))
  (is (= "we" (flip-direction "ns")))
)

(deftest next-state-test
  (is (= {:x 1 :y 1 :phase-time 10 :phase-length 20 :direction "ns"} (next-state {:x 1 :y 1 :phase-time 9 :phase-length 20 :direction "ns"})))
  (is (= {:phase-time 0 :phase-length 20 :direction "ns"} (next-state {:phase-time 19 :phase-length 20 :direction "we"})))
  (is (= {:phase-time 0 :phase-length 20 :direction "we"} (next-state {:phase-time 19 :phase-length 20 :direction "ns"})))
)

(deftest initial-switch-events-test
  (with-redefs [rand-int (constantly 9)
                rand-nth (constantly "ns")]
    (is (= 
      [{:x 1 :y 1 :phase-time 9 :phase-length 40 :direction "ns"}
       {:x 1 :y 2 :phase-time 9 :phase-length 40 :direction "ns"}
       {:x 2 :y 1 :phase-time 9 :phase-length 40 :direction "ns"}
       {:x 2 :y 2 :phase-time 9 :phase-length 40 :direction "ns"}]
      (initial-switch-events 2 2 40)))
))

(deftest next-switch-events-test
  (is (=
    [{:x 1 :y 1 :phase-time 10 :phase-length 20 :direction "ns"}
     {:x 1 :y 2 :phase-time 10 :phase-length 20 :direction "we"}]
    (next-switch-events
      [{:x 1 :y 1 :phase-time 9 :phase-length 20 :direction "ns"}
       {:x 1 :y 2 :phase-time 9 :phase-length 20  :direction "we"}])))
)
