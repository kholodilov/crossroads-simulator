(ns sumo-integration.core-test
  (:require [clojure.test :refer :all]
            [sumo-integration.core :as sumo]
            [common.crossroads :refer [crossroads-direction]]))

(deftest test-sumo-binary-path
  (is (= "/opt/sumo/bin/sumo" (sumo/sumo-binary-path "/opt/sumo" :cli)))
  (is (= "/opt/sumo/bin/sumo-gui" (sumo/sumo-binary-path "/opt/sumo" :gui)))
  (is (thrown-with-msg? RuntimeException #"Unknown SUMO mode" (sumo/sumo-binary-path "/opt/sumo" :unknown-mode))))

(deftest test-neighbour-tl-id
  (is (= "0/1" (sumo/neighbour-tl-id (crossroads-direction :x 1 :y 1 :direction 1))))
  (is (= "1/2" (sumo/neighbour-tl-id (crossroads-direction :x 1 :y 1 :direction 2))))
  (is (= "2/1" (sumo/neighbour-tl-id (crossroads-direction :x 1 :y 1 :direction 3))))
  (is (= "1/0" (sumo/neighbour-tl-id (crossroads-direction :x 1 :y 1 :direction 4))))
  )

(deftest test-lane-src-id
  (is (= "left0"   (sumo/lane-src-id (crossroads-direction :x 0 :y 0 :direction 1) 3 2)))
  (is (= "bottom0" (sumo/lane-src-id (crossroads-direction :x 0 :y 0 :direction 4) 3 2)))
  (is (= "right1"  (sumo/lane-src-id (crossroads-direction :x 2 :y 1 :direction 3) 3 2)))
  (is (= "top1"    (sumo/lane-src-id (crossroads-direction :x 1 :y 1 :direction 2) 3 2)))
  (is (= "1/0"     (sumo/lane-src-id (crossroads-direction :x 1 :y 1 :direction 4) 3 2)))
  )

(deftest test-lane-id
  (is (= "0/1to1/1_0"  (sumo/lane-id (crossroads-direction :x 1 :y 1 :direction 1) 3 2)))
  (is (= "top1to1/1_0" (sumo/lane-id (crossroads-direction :x 1 :y 1 :direction 2) 3 2)))
  )

(deftest test-route-id
  (is (= "r1/2_3"  (sumo/route-id (crossroads-direction :x 1 :y 2 :direction 3))))
  )
