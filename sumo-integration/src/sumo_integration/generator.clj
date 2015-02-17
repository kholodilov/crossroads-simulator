(ns sumo-integration.generator
  (:require [clojure.java.shell :refer (sh)]
            [clostache.parser :as clostache]
            [clojure.java.io :as io]))

(defn generate-network [sumo-home output-dir network-name & {:keys [width height grid-length attach-length] :as params}]
  (let [network-dir (str output-dir "/" network-name)
        netgenerate-file (str network-dir "/netgenerate.xml")
        netgenerate-params (merge params {:network-dir network-dir})
        netgenerate-config (clostache/render-resource "netgenerate.xml.template" netgenerate-params)]
    (sh "mkdir" "-p" network-dir)
    (with-open [w (io/writer netgenerate-file)]
      (.write w netgenerate-config))
    (sh (str sumo-home "/bin/netgenerate") "-c" netgenerate-file)
))
