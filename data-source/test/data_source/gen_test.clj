(ns data-source.gen-test
  (:require [clojure.test :refer :all]
            [data-source.gen :refer :all]))

(deftest states-test
  (is (= "ns" (flip-state "we")))
  (is (= "we" (flip-state "ns")))
  )
