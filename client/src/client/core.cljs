(ns client.core
  (:require [enfocus.core :as ef]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(defn display-value [x y value]
  (ef/at
    (str ".crossroads .crow:nth-child(" (+ y 1) ") .ccol:nth-child(" (+ x 1) ")")
      (ef/content (str value))))

(def ws (js-obj))

(defn init-websocket [ws-url]
  (set! ws (js/WebSocket. ws-url))
  (set! (.-onopen ws) (fn [] (.log js/console "Connection opened...")))
  (set! (.-onclose ws) (fn [] (.log js/console "Connection closed...")))
  (set! (.-onmessage ws)
    (fn [message]
      (let [data (cljs.reader/read-string (.-data message))
            value (:value data)
            x (:x data)
            y (:y data)]
        (display-value x y value)))))

(defn setup-crossroads-table []
  (GET "/size" {:handler
    (fn [{:keys [width height]}]
      (ef/at ".crossroads .crow .ccol"
        (em/clone-for [i (range width)]))
      (ef/at ".crossroads .crow"
        (em/clone-for [i (range height)])))
    }))

(defn start []
  (setup-crossroads-table)
  (init-websocket "ws://localhost:3000/events-ws"))

(set! (.-onload js/window) #(em/wait-for-load (start)))

