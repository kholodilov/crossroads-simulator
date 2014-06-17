(ns client.core
  (:require [enfocus.core :as ef]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(defn display-switch-crossroads [x y t state]
  (ef/at
    (str ".crossroads .crow:nth-child(" (+ y 1) ") .ccol:nth-child(" (+ x 1) ")")
    (ef/do-> 
      (ef/content (str t))
      (ef/set-class "ccol" state))
  ))

(def ws (js-obj))

(defn init-websocket [ws-url]
  (set! ws (js/WebSocket. ws-url))
  (set! (.-onopen ws) (fn [] (.log js/console "Connection opened...")))
  (set! (.-onclose ws) (fn [] (.log js/console "Connection closed...")))
  (set! (.-onmessage ws)
    (fn [message]
      (let [data (cljs.reader/read-string (.-data message))
            {:keys [x y t state]} data]
        (display-switch-crossroads x y t state)))))

(defn setup-crossroads-table []
  (GET "/state" {:handler
    (fn [{:keys [width height switch-times]}]
      (ef/at ".crossroads .crow .ccol"
        (em/clone-for [i (range width)]))
      (ef/at ".crossroads .crow"
        (em/clone-for [i (range height)]))
      (doseq [{:keys [x y t state]} switch-times]
        (display-switch-crossroads x y t state))
    )}))

(defn start []
  (em/wait-for-load
    (setup-crossroads-table)
    (init-websocket "ws://localhost:3000/events")))

;(set! (.-onload js/window) #(em/wait-for-load (start)))

