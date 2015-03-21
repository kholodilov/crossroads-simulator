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

(deftest test-list-directions
  (is (= [(crossroads-direction :x 1 :y 2 :direction 1)
          (crossroads-direction :x 1 :y 2 :direction 2)
          (crossroads-direction :x 1 :y 2 :direction 3)
          (crossroads-direction :x 1 :y 2 :direction 4)] (list-directions 1 2))))

(deftest test-opposite-direction
  (is (= {:x 0 :y 0 :direction 3} (opposite-direction {:x 0 :y 0 :direction 1})))
  (is (= {:x 0 :y 0 :direction 4} (opposite-direction {:x 0 :y 0 :direction 2})))
  (is (= {:x 0 :y 0 :direction 1} (opposite-direction {:x 0 :y 0 :direction 3})))
  (is (= {:x 0 :y 0 :direction 2} (opposite-direction {:x 0 :y 0 :direction 4})))
  )

(deftest test-incoming-directions-vertical
  (is (=   
    #{(crossroads-direction :x 0 :y 0 :direction 4)
      (crossroads-direction :x 0 :y 1 :direction 2)
      (crossroads-direction :x 1 :y 0 :direction 4)
      (crossroads-direction :x 1 :y 1 :direction 2)
      (crossroads-direction :x 2 :y 0 :direction 4)
      (crossroads-direction :x 2 :y 1 :direction 2)}
    (set (incoming-directions-vertical 3 2)))))

(deftest test-incoming-directions-horizontal
  (is (=   
    #{(crossroads-direction :x 0 :y 0 :direction 1)
      (crossroads-direction :x 2 :y 0 :direction 3)
      (crossroads-direction :x 0 :y 1 :direction 1)
      (crossroads-direction :x 2 :y 1 :direction 3)}
    (set (incoming-directions-horizontal 3 2)))))
