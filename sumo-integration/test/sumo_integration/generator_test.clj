(ns sumo-integration.generator-test
  (:require [clojure.test :refer :all]
            [sumo-integration.generator :as sumo-generator]))

(deftest test-generate-vehicles
  (is (= [{:id "r1_0" :route-id "r1", :depart-pos 288} {:id "r1_1" :route-id "r1", :depart-pos 282}]
          (sumo-generator/generate-vehicles "r1" 2 300 6)))
  (is (= [{:id "r1_0" :route-id "r1", :depart-pos 288} {:id "r1_1" :route-id "r1", :depart-pos 282}
          {:id "r2_0" :route-id "r2", :depart-pos 288} {:id "r2_1" :route-id "r2", :depart-pos 282} {:id "r2_2" :route-id "r2", :depart-pos 276}]
          (sumo-generator/generate-vehicles [{:route-id "r1" :count 2} {:route-id "r2" :count 3}] 300 6))))
