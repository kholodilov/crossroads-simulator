(ns experiments.bandwidth-test
  (:require [clojure.test :refer :all]
            [experiments.bandwidth :refer [bisect]]))

(deftest test-bisect
  (is (>= 5 (- 38 (bisect 0 100 5 #(< % 38))) 0))
  (is (>= 5 (- 50 (bisect 0 100 5 #(< % 50))) 0))
  (is (= 50 (bisect 0 100 5 #(<= % 50)))))
