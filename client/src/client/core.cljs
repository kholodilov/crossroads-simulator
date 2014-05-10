(ns client.core
  (:require [enfocus.core :as ef]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(defn display-counter [data]
  (ef/at "body" (ef/content (:body data))))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "Something bad happened: " status " " status-text)))

(defn get-counter []
  (GET "/counter"
        {:handler display-counter
         :error-handler error-handler
         :finally #(js/setTimeout get-counter 1000)
         :timeout 30000}))

(defn start []
  (ef/at "body" (ef/content "Hello world!111"))
  (get-counter))


;; (set! (.-onload js/window) start)
(set! (.-onload js/window) #(em/wait-for-load (start)))

