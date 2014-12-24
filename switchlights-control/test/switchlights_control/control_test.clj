(ns switchlights-control.control-test
  (:require [clojure.test :refer :all]
            [switchlights-control.control :refer :all]))

(deftest flip-direction-test
  (is (= "ns" (flip-direction "we")))
  (is (= "we" (flip-direction "ns")))
)

(deftest next-state-test
  (with-redefs [queues-for-crossroad (constantly [1 2 3 4])]
    (let [queue-events []
          phase-length-fn
            (fn [phase-time direction queues] ({"ns" 20 "we" 25} direction))
          next-phase-length-update-fn (constantly 1)
          next-state-fn (build-next-state-fn queue-events phase-length-fn next-phase-length-update-fn)]
      (is (= {:x 1 :y 1 :phase-time 10 :phase-length 20 :direction "ns"} (next-state-fn {:x 1 :y 1 :phase-time 9 :phase-length 30 :direction "ns"})))
      (is (= {:phase-time 0 :phase-length 25 :direction "we"} (next-state-fn {:phase-time 19 :phase-length 30 :direction "ns"})))
)))

(deftest initial-switch-events-test
  (with-redefs [rand-int (constantly 9)
                rand-nth (constantly "ns")]
    (is (= 
      [{:x 1 :y 1 :phase-time 9 :phase-length 20 :direction "ns"}
       {:x 1 :y 2 :phase-time 9 :phase-length 20 :direction "ns"}
       {:x 2 :y 1 :phase-time 9 :phase-length 20 :direction "ns"}
       {:x 2 :y 2 :phase-time 9 :phase-length 20 :direction "ns"}]
      (initial-switch-events 2 2 20)))
))

(deftest queues-for-crossroad-test
  (let [no-queue-events []
        some-queue-events 
          [{:x 1 :y 1 :direction 2 :queue 1}
           {:x 1 :y 1 :direction 1 :queue 1}
           {:x 1 :y 1 :direction 3 :queue 1}
           {:x 1 :y 1 :direction 4 :queue 1}
           {:x 1 :y 2 :direction 2 :queue 14}
           {:x 1 :y 2 :direction 1 :queue 11}
           {:x 1 :y 2 :direction 3 :queue 12}
           {:x 1 :y 2 :direction 4 :queue 13}
           {:x 2 :y 1 :direction 2 :queue 2}
           {:x 2 :y 1 :direction 3 :queue 2}
           {:x 2 :y 1 :direction 4 :queue 2}]]
    (is (= [1 1 1 1] (queues-for-crossroad 1 1 some-queue-events)))
    (is (= [11 14 12 13] (queues-for-crossroad 1 2 some-queue-events)))
    (is (= [0 0 0 0] (queues-for-crossroad 2 1 some-queue-events)))
    (is (= [0 0 0 0] (queues-for-crossroad 1 2 no-queue-events)))
    (is (= [0 0 0 0] (queues-for-crossroad 2 2 some-queue-events)))
  ))
