(ns common.service-test
  (:require [clojure.test :refer :all]
            [common.service :refer :all]))

(deftest test-stoppable
  (let [service (build-service :stop-fn (constantly 123))]
    (is (= 123 (stop service)))
  ))

