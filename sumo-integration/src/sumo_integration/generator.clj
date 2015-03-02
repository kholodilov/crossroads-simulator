(ns sumo-integration.generator
  (:require [clojure.java.shell :refer (sh)]
            [clostache.parser :as clostache]
            [clojure.java.io :as io]))

(defn write-file [path content]
  (with-open [w (io/writer path)]
    (.write w content)))

(defn copy-resources [dir & resources]
  (doseq [resource resources]
    (write-file (str dir "/" resource) (slurp (io/resource resource)))))

(defn generate-network [sumo-home output-dir network-name & {:keys [width height grid-length attach-length e2-length] :as params}]
  (let [network-dir (str output-dir "/" network-name)
        netgenerate-file (str network-dir "/netgenerate.xml")
        netgenerate-params (merge params {:network-dir network-dir})
        netgenerate-config (clostache/render-resource "netgenerate.xml.template" netgenerate-params)]

    (sh "mkdir" "-p" network-dir)
    (write-file netgenerate-file netgenerate-config)
    (sh (str sumo-home "/bin/netgenerate") "-c" netgenerate-file)

    (copy-resources network-dir "config.sumo.cfg" "additional.xml" "routes.xml" "settings.xml")

    ; http://sumo.dlr.de/wiki/Simulation/Output/Lanearea_Detectors_(E2)
    ; http://sumo.dlr.de/wiki/TraCI/Lane_Area_Detector_Value_Retrieval
    (sh (str sumo-home "/tools/output/generateTLSE2Detectors.py")
        "-n" (str network-dir "/network.xml")
        "-l" (str e2-length)
        "-f" "1"
        "-r" "/dev/null")

    (str network-dir "/config.sumo.cfg")
))
