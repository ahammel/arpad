(ns arpad.persistor
  (:require [clojure.java.io    :refer [file]]
            [clojure.data.json  :as json]
            [clojure.core.async :refer [<! <!! go-loop]]))

(defn persist
  "Write the target string to a file, moving the original file to
  'filename.bak'. Creates the file if it does not exist."
  [filename pool]
  (spit filename "" :append true)
  (let [f (file filename)
        bak (file (str filename ".bak"))
        pool-json (json/write-str pool)]
    (.renameTo f bak)
    (spit filename pool-json)))

(defn spawn-persistor
  "Spawn a process which persists a pool to a given OS file."
  [filename chan]
  (go-loop [pool (<! chan)]
    (when pool
      (persist filename pool)
      (recur (<! chan)))))

(defn run-persistor
  "Blocking version of spawn-persistor"
  [filename chan]
  (loop [pool (<!! chan)]
    (when pool
      (persist filename pool)
      (recur (<!! chan)))))
