(ns service.core-test
  (:require [clojure.test :refer :all]
            [service.core :refer :all]))

(deftest states-test
  (is (= "ns" (flip-state "we")))
  (is (= "we" (flip-state "ns")))
  )
