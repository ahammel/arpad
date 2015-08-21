(ns arpad.slack
  (:require [arpad.pool.load :refer [load-pool]]
            [arpad.pool.manager :refer [spawn-pool-manager]]
            [arpad.pool.schema :refer [json->Pool]]
            [arpad.persistor :refer [spawn-persistor]]
            [clojure.data.json :as json]
            [clojure.core.async :refer [<! >! >!! alts!! chan go-loop
                                        sliding-buffer timeout]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]))

(defonce slack-parser-chan (chan))
(defonce report-chan (chan))
(def pool-file "~/arpad-pool.json")

(defn- parse-slack-request
  [s]
  (json/read-str s)
  ;; ^ Wrong
  )

(defn error-msg
  [cmd]
  (str "I'm sorry, I didn't understand the message: " cmd))

(defn spawn-parser
  [in-chan out-chans]
  {:pre [(contains? out-chans :player-report)
         (contains? out-chans :pool-manager)]}
  (go-loop [in (<! in-chan)]
    (when in
      (if-let [cmd (parse-slack-request in)]
        (>! (:pool-manager  out-chans) cmd)
        (>! (:player-report out-chans) (error-msg in)))
      (recur (<! in-chan)))))

(defroutes app-routes
  (POST "/v1/arpad" request
        (let [_ (>!! slack-parser-chan (:body request))
              [resp _] (alts!! [report-chan (timeout 10000)])]
          (if resp
            {:body resp}
            {:status 503 :body "Request timed out"})))
  (route/not-found "Not found"))

(defn init []
  (let [channels {:slack-command-parser slack-parser-chan
                  :pool-manager (chan)
                  :player-report report-chan
                  :new-state (chan (sliding-buffer 1))}
        init-pool (or (load-pool pool-file :throw? false)
                      {:players {}
                       :k :uscf
                       :default-rating 750})]
    (spawn-pool-manager (:pool-manager channels) channels)
    (spawn-persistor pool-file (:new-state channels))
    (spawn-parser (:slack-command-parser channels) channels)))

(def app
  (-> (wrap-defaults app-routes api-defaults)
      (wrap-json-response)))

(defn -main
  [& args]
  (run-jetty #'app {:port 1128}))
