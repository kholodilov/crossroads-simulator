(defproject kholodilov.crossroads-simulator/sumo-integration "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure _]
                 [org.clojure/core.match _]
                 [traas/traas "r33-3"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [kholodilov.crossroads-simulator/common _]]
  :repositories {"local" ~(str (.toURI (java.io.File. "lib")))}
  :plugins [[lein-modules "0.3.6"]])
