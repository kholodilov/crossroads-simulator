(defproject client "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [enfocus "2.1.0-SNAPSHOT"]
                 [cljs-ajax "0.2.3"]
                 [jayq "2.5.1"]]
  :profiles {:dev {:plugins [[lein-cljsbuild "1.0.3"]]}}
  :cljsbuild {
    :builds [{
        :source-paths ["src"]
        :compiler {
          :output-to "target/resources/js/main.js"
          :optimizations :whitespace
          :pretty-print true}}]})
