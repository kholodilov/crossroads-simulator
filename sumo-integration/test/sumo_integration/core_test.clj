(ns sumo-integration.core-test
  (:require [clojure.test :refer :all]
            [sumo-integration.core :as sumo]))

(deftest test-sumo-binary-path
  (is (= "/opt/sumo/bin/sumo" (sumo/sumo-binary-path "/opt/sumo" :cli)))
  (is (= "/opt/sumo/bin/sumo-gui" (sumo/sumo-binary-path "/opt/sumo" :gui)))
  (is (thrown-with-msg? RuntimeException #"Unknown SUMO mode" (sumo/sumo-binary-path "/opt/sumo" :unknown-mode))))

; 1/1 q1 - 0/1to1/1_0 : (- x 1)
; 1/1 q2 - 1/2to1/1_0 : (+ y 1)
; 1/1 q3 - 2/1to1/1_0 : (+ x 1)
; 1/1 q4 - 1/0to1/1_0 : (- y 1)
(deftest test-lane-id
  (is (= "0/1to1/1_0" (sumo/lane-id 1 1 (nth sumo/lanes 0))))
  (is (= "1/2to1/1_0" (sumo/lane-id 1 1 (nth sumo/lanes 1))))
  (is (= "2/1to1/1_0" (sumo/lane-id 1 1 (nth sumo/lanes 2))))
  (is (= "1/0to1/1_0" (sumo/lane-id 1 1 (nth sumo/lanes 3))))
  )
