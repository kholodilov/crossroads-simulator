(ns common.crossroads-test
  (:require [clojure.test :refer :all]
            [common.crossroads :refer :all]))

(deftest test-coord-range
  (is (= [1 2 3] (coord-range 3))))

(deftest test-queues-directions
  (is (= [1 2 3 4] queues-directions)))

(deftest test-max-dimension-size
  (is (= 1 (max-dimension-size [] :x)))
  (is (= 1 (max-dimension-size [] :y)))
  (is (= 2 (max-dimension-size [{:x 1 :y 1} {:x 1 :y 2} {:x 2 :y 1} {:x 2 :y 2}] :x)))
  (is (= 2 (max-dimension-size [{:x 1 :y 1} {:x 1 :y 2} {:x 2 :y 1} {:x 2 :y 2}] :y)))
)

