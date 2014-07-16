(ns data-source.core
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.basic     :as lb]
            [ruiyun.tools.timer :as timer]
            [clojure.tools.cli  :as cli]))

(def default-exchange "")

(defn run [width height queue]
  (let [conn (rmq/connect)
        ch (lch/open conn)]
    (lq/declare ch queue :exclusive false)
    (lb/publish ch default-exchange queue "Hello!")
    (rmq/close ch)
    (rmq/close conn)
  ))

(def cli-options
  [["-w" "--width n" "Width"
    :default 4
    :parse-fn #(Integer/parseInt %)]
   ["-h" "--height n" "Height"
    :default 3
    :parse-fn #(Integer/parseInt %)]
   [nil "--switch-events-queue queue-name" "Queue"
    :default "switch-events"]])

(defn -main [& args]
  (let [{:keys [options]} (cli/parse-opts args cli-options)]
    (run (options :width) (options :height) (options :switch-events-queue))))
