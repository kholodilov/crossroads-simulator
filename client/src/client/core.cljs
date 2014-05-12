(ns client.core
  (:require [enfocus.core :as ef]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(defn display-counter [counter]
  (ef/at "body" (ef/content counter)))

(def ws (js-obj))

(defn init-websocket [ws-url]
  (set! ws (js/WebSocket. ws-url))
  (set! (.-onopen ws) (fn [] (.log js/console "Connection opened...")))
  (set! (.-onclose ws) (fn [] (.log js/console "Connection closed...")))
  (set! (.-onmessage ws) (fn [message] (display-counter (.-data message)))))

(defn get-counter []
  (GET "/counter"
        {:handler display-counter
         :error-handler error-handler
         :finally #(js/setTimeout get-counter 1000)
         :timeout 30000}))

(defn start []
  (ef/at "body" (ef/content "Hello world!111"))
  (init-websocket "ws://localhost:3000/counter-ws"))


;; (set! (.-onload js/window) start)
(set! (.-onload js/window) #(em/wait-for-load (start)))

