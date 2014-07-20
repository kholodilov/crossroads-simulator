(ns common.cli
  (:require [clojure.tools.cli :as cli]))

(def common-options
  [["-w" "--width n" "Width"
    :default 4
    :parse-fn #(Integer/parseInt %)]
   ["-h" "--height n" "Height"
    :default 3
    :parse-fn #(Integer/parseInt %)]
   [nil "--switch-events-queue queue-name" "Queue"
    :default "switch-events"]])

(defn parse-opts [args options]
  (:options (cli/parse-opts args (vec (concat common-options options)))))


