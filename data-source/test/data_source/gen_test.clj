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

(deftest initial-switch-events-test
  (with-redefs [rand-nth (constantly "ns")]
    (is (= 
      [[0 0 0 "ns"] [0 1 0 "ns"] [1 0 0 "ns"] [1 1 0 "ns"]]
      (initial-switch-events 2 2)))
))

(deftest next-switch-events-test
  (is (=
    [[0 0 9 "ns"] [0 1 2 "we"]]
    (next-switch-events [[0 0 10 "ns"] [0 1 3 "we"]])))
)
