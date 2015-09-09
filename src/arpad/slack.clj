(ns arpad.slack
  (:gen-class)
  (:require [arpad.pool.load :refer [load-pool]]
            [arpad.pool.manager :refer [spawn-pool-manager]]
            [arpad.pool.schema :refer [json->Pool]]
            [arpad.persistor :refer [spawn-persistor]]
            [arpad.pretty-printer.english :refer [pretty-print]]
            [arpad.slack.parser :refer [parse-slack-request]]
            [clojure.data.json :as json]
            [clojure.core.async :refer [<! >! >!! alts!! chan go-loop
                                        sliding-buffer timeout]]
            [clojure.tools.logging :as log]
            [clojure.pprint :refer [pprint]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]))

(defonce pool-manager-chan (chan))
(defonce report-chan (chan))
(def pool-file (str (System/getenv "HOME") "/arpad-pool.json"))

(defn error-msg
  [cmd]
  (str "I'm sorry, I didn't understand the message: " cmd))

(defroutes app-routes
  (POST "/v1/arpad" request
        (let [params (:params request)
              cmd (parse-slack-request params)]
          (cond
           (or (nil? (:text params)) (nil? (:user_name params)))
           {:body {"text"
                   (str "Oh dear, it seems somebody has made some incorrect"
                        "assumptions about the Slack API. Go yell at"
                        " @ajh.")}}

           (nil? cmd)
           {:body {"text" (error-msg cmd)}}

           (:release-notes cmd)
           {:body {"text" (:release-notes cmd)
                   "mrkdwn" "true"}}

           :else
           (do
             (>!! pool-manager-chan cmd)
             (let [[resp _] (alts!! [report-chan (timeout 10000)])]
               (if resp
                 {:body {"text"   (pretty-print resp)
                         "mrkdwn" "true"}}
                 {:status 503 :body "Request timed out"}))))))
  (route/not-found "Not found"))

(defn init []
  (let [channels {:pool-manager pool-manager-chan
                  :player-report report-chan
                  :new-state (chan (sliding-buffer 1))}
        init-pool (or (load-pool pool-file :throw? false)
                      {:players {}
                       :k :uscf
                       :default-rating 750})]
    (spawn-pool-manager init-pool (:pool-manager channels) channels)
    (spawn-persistor pool-file (:new-state channels))))

(def app
  (-> app-routes
      (wrap-defaults api-defaults)
      (wrap-params)
      (wrap-json-response)))

(defn -main
  [& args]
  (init)
  (run-jetty #'app {:port 1128}))
