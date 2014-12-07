(ns switchlights-control.gen-test
  (:require [clojure.test :refer :all]
            [switchlights-control.gen :refer :all]))

(deftest flip-direction-test
  (is (= "ns" (flip-direction "we")))
  (is (= "we" (flip-direction "ns")))
)

(deftest next-state-test
  (is (= {:x 1 :y 1 :t 9 :direction "ns"} (next-state {:x 1 :y 1 :t 10 :direction "ns"})))
  (is (= {:t 9 :direction "we"} (next-state {:t 10 :direction "we"})))
  (with-redefs [max-wait 111]
    (is (= {:t 111 :direction "ns"} (next-state {:t 0 :direction "we"})))
    (is (= {:t 111 :direction "we"} (next-state {:t 0 :direction "ns"}))))
)

(deftest initial-switch-events-test
  (with-redefs [rand-int (constantly 30)
                rand-nth (constantly "ns")]
    (is (= 
      [{:x 1 :y 1 :t 30 :direction "ns"}
       {:x 1 :y 2 :t 30 :direction "ns"}
       {:x 2 :y 1 :t 30 :direction "ns"}
       {:x 2 :y 2 :t 30 :direction "ns"}]
      (initial-switch-events 2 2)))
))

(deftest next-switch-events-test
  (is (=
    [{:x 1 :y 1 :t 9 :direction "ns"}
     {:x 1 :y 2 :t 2 :direction "we"}]
    (next-switch-events
      [{:x 1 :y 1 :t 10 :direction "ns"}
       {:x 1 :y 2 :t 3  :direction "we"}])))
)
