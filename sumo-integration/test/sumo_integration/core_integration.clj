(ns sumo-integration.core-integration
  (:require [clojure.test :refer :all]
            [common.service :as service]
            [sumo-integration.core :as sumo]))

(deftest ^:integration test-sumo-service
  (let [sumo (sumo/run-sumo "/opt/sumo" :cli 1000)]
    (Thread/sleep 5000)
    (service/stop sumo)))