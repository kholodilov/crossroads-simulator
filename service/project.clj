(defproject service "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.6"]
                 [http-kit "2.1.13"]
                 [ring/ring-core "1.2.2"]
                 [ring/ring-devel "1.2.2"]
                 [javax.servlet/servlet-api "2.5"]
                 [sonian/carica "1.1.0" :exclusions [[cheshire]]]
                 [fogus/ring-edn "0.2.0"]
                 [kholodilov/clj-esper "1.0.2-SNAPSHOT"]
                 [org.clojure/tools.cli "0.3.1"]
                 [com.novemberain/langohr "2.11.0"]]
  :profiles {:test 
              {:dependencies [[stylefruits/gniazdo "0.2.1"]]}}
  :main service.core
  :test-selectors {
    :default (fn [m] (not (:integration m)))
    :itest (fn [m] true)
  })

