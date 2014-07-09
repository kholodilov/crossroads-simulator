(ns service.core-test
  (:require [clojure.test :refer :all]
            [service.core :refer :all]))

(deftest states-test
  (is (= "ns" (flip-state "we")))
  (is (= "we" (flip-state "ns")))
  )

(defn capture-args [args-list]
  (fn [& args] (swap! args-list conj (vec args))))

(defn dont-proceed [] (constantly false))
;(defn proceed-times [n] (constantly false))


(deftest switch-loop-dont-proceed-test
  (let [result (atom [])]
    (switch-loop (capture-args result) (dont-proceed) 3 4 10 "ns")
    (is (= @result [[SwitchEvent :x 3 :y 4 :t 10 :state "ns"]]))
  ))

; (deftest switch-loop-single-proceed-test
;   (let [result (atom [])]
;     (switch-loop (capture-args result) (constantly false) 3 4 10 "ns")
;     (is (= @result [[SwitchEvent :x 3 :y 4 :t 10 :state "ns"]]))
;   ))
