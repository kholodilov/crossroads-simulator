(ns sumo-integration.core-test
  (:require [clojure.test :refer :all]
            [sumo-integration.core :as sumo]))

(deftest test-sumo-binary-path
  (is (= "/opt/sumo/bin/sumo" (sumo/sumo-binary-path "/opt/sumo" :cli)))
  (is (= "/opt/sumo/bin/sumo-gui" (sumo/sumo-binary-path "/opt/sumo" :gui)))
  (is (thrown-with-msg? RuntimeException #"Unknown SUMO mode" (sumo/sumo-binary-path "/opt/sumo" :unknown-mode))))
