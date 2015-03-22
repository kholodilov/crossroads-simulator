(ns sumo-integration.generator-integration
  (:require [clojure.test :refer :all]
            [sumo-integration.core :as sumo]
            [sumo-integration.generator :as sumo-generator]))

(def width 3)
(def height 2)
(def step-length 1000)

(deftest ^:integration test-generator
    (let [simulation-cfg (sumo-generator/generate-network "/opt/sumo" "/tmp" "Experiments"
                          :width width :height height :grid-length 300 :attach-length 300 :e2-length 120)
          sumo-conn (sumo/start-sumo "/opt/sumo" :cli simulation-cfg step-length)]
      (.do_timestep sumo-conn)
      (.close sumo-conn)))
