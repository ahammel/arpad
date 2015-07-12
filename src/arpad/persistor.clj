(ns arpad.persistor
  (:require [clojure.java.io    :refer [file]]
            [clojure.data.json  :as json]
            [clojure.core.async :refer [<! go-loop]]))

(defn persist
  "Write the target string to a file, moving the original file to
  'filename.bak'."
  [filename pool]
  (let [f (file filename)
        bak (file (str filename ".bak"))
        pool-json (json/write-str pool)]
    (.renameTo f bak)
    (spit filename pool-json)))

(defn spawn-persistor
  [filename chan]
  (go-loop [pool (<! chan)]
    (when pool
      (persist filename pool))))
