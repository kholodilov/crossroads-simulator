(ns sumo-integration.generator
  (:require [clojure.java.shell :refer (sh)]
            [clostache.parser :as clostache]
            [clojure.java.io :as io]
            [sumo-integration.network :as network]))

(defn write-file [path content]
  (with-open [w (io/writer path)]
    (.write w content)))

(defn copy-resources [dir & resources]
  (doseq [resource resources]
    (write-file (str dir "/" resource) (slurp (io/resource resource)))))

(defn sh* [& cmd]
  (let [{:keys [exit out err] :as sh-result} (apply sh cmd)]
    (if (not= exit 0)
      (throw (Exception. (str sh-result)))
      (println out))))

; http://sumo.dlr.de/wiki/Simulation/Output/Lanearea_Detectors_(E2)
; http://sumo.dlr.de/wiki/TraCI/Lane_Area_Detector_Value_Retrieval
(defn generate-e2 [sumo-home network-file e2-length]
  (sh* (str sumo-home "/tools/output/generateTLSE2Detectors.py")
        "-n" network-file
        "-l" (str e2-length)
        "-f" "1"
        "-r" "/dev/null"))

(defn generate-config [template dest params]
  (write-file dest (clostache/render-resource template params)))

(def car-interval 6)
(defn tls-off [width height] (for [x (range width) y (range height)] {:id (str x "/" y) :program-id "off"}))

(defn generate-vehicles 
  ([route-id count grid-length car-interval]
    (for [i (range count)]
      {:id (str route-id "_" i)
       :route-id route-id
       :depart-pos (- grid-length (* car-interval (+ i 2)))}))
  ([vehicles-defs grid-length car-interval]
    (flatten (map #(generate-vehicles (:route-id %) (:count %) grid-length car-interval) vehicles-defs))))

(defn generate-network [sumo-home output-dir network-name & {:keys [width height grid-length attach-length e2-length routes vehicles-defs tls] :as params}]
  (let [network-dir (str output-dir "/" network-name)
        config-file #(str network-dir "/" %)
        netgenerate-file (config-file "netgenerate.xml")
        network-file (config-file "network.xml")
        tls-file (config-file "tls.xml")
        routes-file (config-file "routes.xml")
        
        netgenerate-params (merge params {:network-dir network-dir})
        tls-params {:tls (if (nil? tls) (tls-off width height) tls)}
        routes-params {:routes (if (nil? routes) (network/routes width height) routes)
                       :vehicles (generate-vehicles vehicles-defs grid-length car-interval)}]

    (sh* "mkdir" "-p" network-dir)
    (generate-config "netgenerate.xml.template" netgenerate-file netgenerate-params)
    (sh* (str sumo-home "/bin/netgenerate") "-c" netgenerate-file :env {}) ; :env {} - shit hack to w/a matlab library path

    (generate-e2 sumo-home network-file e2-length)
    (generate-config "tls.xml.template" tls-file tls-params)
    (generate-config "routes.xml.template" routes-file routes-params)

    (copy-resources network-dir "config.sumo.cfg" "vtypes.xml" "settings.xml")

    (config-file "config.sumo.cfg")
))
