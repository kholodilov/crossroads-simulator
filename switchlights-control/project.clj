(defproject kholodilov.crossroads-simulator/switchlights-control "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure _]
                 [kholodilov.crossroads-simulator/common _]
                 [kamenev/crossroad-control "0.0.2"]
                 [matlab/javabuilder "v83"]]
  :repositories {"local" ~(str (.toURI (java.io.File. "lib")))}
  :plugins [[lein-modules "0.3.6"]])
