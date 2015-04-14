(ns experiments.bandwidth
  (:require [experiments.core]
            [clojure.tools.cli :as cli]))

(def experiment-name (experiments.core/experiment-name "bandwidth"))

(def cli-options
  [["-o" "--output file" "Output file name"
    :default (str experiment-name ".csv")]
   ["-s" "--speed n" "Speed-up coefficient (10 -> 10x speed-up)"
    :default 1
    :parse-fn #(Integer/parseInt %)]
   ["-m" "--sumo-mode mode" "Simulation mode - 'cli' or 'gui'"
    :default :cli
    :parse-fn #(keyword %)]])

(defn -main [& args]
  (let [{:keys [output speed sumo-mode]}
          (:options (cli/parse-opts args cli-options))]
    (println experiment-name)
  ))
