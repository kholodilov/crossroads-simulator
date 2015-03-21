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

(deftest test-edge-id
  (is (= "0/1to1/1"  (network/edge-id (crossroads-direction :x 1 :y 1 :direction 1) 3 2)))
  (is (= "top1to1/1" (network/edge-id (crossroads-direction :x 1 :y 1 :direction 2) 3 2)))
  )

(deftest test-opposite-edge-id
  (is (= "1/1to0/1"  (network/opposite-edge-id (crossroads-direction :x 1 :y 1 :direction 1) 3 2)))
  (is (= "1/1totop1" (network/opposite-edge-id (crossroads-direction :x 1 :y 1 :direction 2) 3 2)))
  )

(deftest test-lane-id
  (is (= "0/1to1/1_0"  (network/lane-id (crossroads-direction :x 1 :y 1 :direction 1) 3 2)))
  (is (= "top1to1/1_0" (network/lane-id (crossroads-direction :x 1 :y 1 :direction 2) 3 2)))
  )

(deftest test-routes
  (is (= [{:id "r0/0_1" :edges "left0to0/0 0/0to1/0 1/0to2/0 2/0toright0"}
          {:id "r0/1_1" :edges "left1to0/1 0/1to1/1 1/1to2/1 2/1toright1"}

          {:id "r2/0_3" :edges "right0to2/0 2/0to1/0 1/0to0/0 0/0toleft0"}
          {:id "r2/1_3" :edges "right1to2/1 2/1to1/1 1/1to0/1 0/1toleft1"}

          {:id "r0/0_4" :edges "bottom0to0/0 0/0to0/1 0/1totop0"}
          {:id "r1/0_4" :edges "bottom1to1/0 1/0to1/1 1/1totop1"}
          {:id "r2/0_4" :edges "bottom2to2/0 2/0to2/1 2/1totop2"}

          {:id "r0/1_2" :edges "top0to0/1 0/1to0/0 0/0tobottom0"}
          {:id "r1/1_2" :edges "top1to1/1 1/1to1/0 1/0tobottom1"}
          {:id "r2/1_2" :edges "top2to2/1 2/1to2/0 2/0tobottom2"}
          ]
    (network/routes 3 2))))
