(ns data-source.gen-test
  (:require [clojure.test :refer :all]
            [data-source.gen :refer :all]))

(deftest flip-state-test
  (is (= "ns" (flip-state "we")))
  (is (= "we" (flip-state "ns")))
)

(deftest next-state-test
  (is (= [9 "ns"] (next-state 10 "ns")))
  (is (= [9 "we"] (next-state 10 "we")))
  (with-redefs [rand-int (constantly 111)]
    (is (= [111 "ns"] (next-state 0 "we")))
    (is (= [111 "we"] (next-state 0 "ns"))))
)
