(defproject kholodilov.crossroads-simulator/service "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure _]
                 [compojure "1.1.6"]
                 [http-kit "2.1.13"]
                 [ring/ring-core "1.2.2"]
                 [ring/ring-devel "1.2.2"]
                 [javax.servlet/servlet-api "2.5"]
                 [sonian/carica "1.1.0" :exclusions [[cheshire]]]
                 [fogus/ring-edn "0.2.0"]
                 [ruiyun/tools.timer _]
                 [org.clojure/tools.cli "0.3.1"]
                 [kholodilov.crossroads-simulator/common _]
                 [kholodilov.crossroads-simulator/switchlights-control _]
                 [kholodilov.crossroads-simulator/sumo-integration _]]
  :plugins [[lein-modules "0.3.6"]]
  :profiles {:test 
              {:dependencies [[stylefruits/gniazdo "0.2.1"]]}}
  :main service.core)

