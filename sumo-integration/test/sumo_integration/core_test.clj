(ns sumo-integration.core-test
  (:require [clojure.test :refer :all]
            [sumo-integration.core :as sumo]))

(deftest test-sumo-binary-path
  (is (= "/opt/sumo/bin/sumo" (sumo/sumo-binary-path "/opt/sumo" :cli)))
  (is (= "/opt/sumo/bin/sumo-gui" (sumo/sumo-binary-path "/opt/sumo" :gui)))
  (is (thrown-with-msg? RuntimeException #"Unknown SUMO mode" (sumo/sumo-binary-path "/opt/sumo" :unknown-mode))))

(deftest test-neighbour-tl-id
  (is (= "0/1" (sumo/neighbour-tl-id 1 1 1)))
  (is (= "1/2" (sumo/neighbour-tl-id 1 1 2)))
  (is (= "2/1" (sumo/neighbour-tl-id 1 1 3)))
  (is (= "1/0" (sumo/neighbour-tl-id 1 1 4)))
  )

(deftest test-lane-src-id
  (is (= "left0"   (sumo/lane-src-id 0 0 1 3 2)))
  (is (= "bottom0" (sumo/lane-src-id 0 0 4 3 2)))
  (is (= "right1"  (sumo/lane-src-id 2 1 3 3 2)))
  (is (= "top1"    (sumo/lane-src-id 1 1 2 3 2)))
  (is (= "1/0"     (sumo/lane-src-id 1 1 4 3 2)))
  )

(deftest test-lane-id
  (is (= "0/1to1/1_0"  (sumo/lane-id 1 1 1 3 2)))
  (is (= "top1to1/1_0" (sumo/lane-id 1 1 2 3 2)))
  )
