(defproject kholodilov.crossroads-simulator/experiments "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure _]
                 [org.clojure/tools.cli "0.3.1"]
                 [kholodilov.crossroads-simulator/common _]
                 [kholodilov.crossroads-simulator/switchlights-control _]
                 [kholodilov.crossroads-simulator/sumo-integration _]]
  :plugins [[lein-modules "0.3.6"]]
  :main experiments.core)

