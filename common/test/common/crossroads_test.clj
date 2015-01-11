(ns common.crossroads-test
  (:require [clojure.test :refer :all]
            [common.crossroads :refer :all]))

(deftest test-coord-range
  (is (= [0 1 2] (coord-range 3))))

(deftest test-max-coord
  (is (= 2 (max-coord 3))))

(deftest test-max-dimension-size
  (is (= 1 (max-dimension-size [] :x)))
  (is (= 1 (max-dimension-size [] :y)))
  (is (= 2 (max-dimension-size [{:x 0 :y 0} {:x 0 :y 1} {:x 1 :y 0} {:x 1 :y 1}] :x)))
  (is (= 2 (max-dimension-size [{:x 0 :y 0} {:x 0 :y 1} {:x 1 :y 0} {:x 1 :y 1}] :y)))
)

(deftest test-queues-directions
  (is (= [1 2 3 4] queues-directions)))

(deftest test-incoming-directions-ns
  (is (=   
    #{{:x 0 :y 0 :direction 4}
      {:x 0 :y 1 :direction 2}
      {:x 1 :y 0 :direction 4}
      {:x 1 :y 1 :direction 2}
      {:x 2 :y 0 :direction 4}
      {:x 2 :y 1 :direction 2}}
    (set (incoming-directions-ns 3 2)))))

(deftest test-incoming-directions-we
  (is (=   
    #{{:x 0 :y 0 :direction 1}
      {:x 2 :y 0 :direction 3}
      {:x 0 :y 1 :direction 1}
      {:x 2 :y 1 :direction 3}}
    (set (incoming-directions-we 3 2)))))
