(ns common.events-test
  (:require [clojure.test :refer :all]
            [common.events :refer :all]
            [common.crossroads :as crossroads]))

(deftest test-queue-event 
  (is (= {:x 1 :y 2 :direction 3 :queue 4}
         (queue-event (crossroads/crossroads-direction :x 1 :y 2 :direction 3) :queue 4))))