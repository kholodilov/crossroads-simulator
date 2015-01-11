(ns sumo-integration.network-test
  (:require [clojure.test :refer :all]
            [sumo-integration.network :as network]
            [common.crossroads :refer [crossroads-direction]]))

(deftest test-crossroads-id
  (is (= "1/2"  (network/crossroads-id 1 2)))
  )

(deftest test-route-id
  (is (= "r1/2_3"  (network/route-id (crossroads-direction :x 1 :y 2 :direction 3))))
  )

(deftest test-neighbour-crossroads-id
  (is (= "0/1" (#'network/neighbour-crossroads-id (crossroads-direction :x 1 :y 1 :direction 1))))
  (is (= "1/2" (#'network/neighbour-crossroads-id (crossroads-direction :x 1 :y 1 :direction 2))))
  (is (= "2/1" (#'network/neighbour-crossroads-id (crossroads-direction :x 1 :y 1 :direction 3))))
  (is (= "1/0" (#'network/neighbour-crossroads-id (crossroads-direction :x 1 :y 1 :direction 4))))
  )

(deftest test-lane-src-id
  (is (= "left0"   (#'network/lane-src-id (crossroads-direction :x 0 :y 0 :direction 1) 3 2)))
  (is (= "bottom0" (#'network/lane-src-id (crossroads-direction :x 0 :y 0 :direction 4) 3 2)))
  (is (= "right1"  (#'network/lane-src-id (crossroads-direction :x 2 :y 1 :direction 3) 3 2)))
  (is (= "top1"    (#'network/lane-src-id (crossroads-direction :x 1 :y 1 :direction 2) 3 2)))
  (is (= "1/0"     (#'network/lane-src-id (crossroads-direction :x 1 :y 1 :direction 4) 3 2)))
  )

(deftest test-lane-id
  (is (= "0/1to1/1_0"  (network/lane-id (crossroads-direction :x 1 :y 1 :direction 1) 3 2)))
  (is (= "top1to1/1_0" (network/lane-id (crossroads-direction :x 1 :y 1 :direction 2) 3 2)))
  )
