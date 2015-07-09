(ns arpad.pool.load
  (:require [clojure.java.io   :as io]
            [clojure.data.json :as json]
            [arpad.pool.schema :refer [json->Pool]]))

(defn- exists? [file-name]
  (.exists (io/file file-name)))

(defn load-pool
  "Loads a pool, given a file containing the pool in JSON
  format."
  [file-name]
  (-> (slurp file-name)
      (json/read-str :key-fn keyword)
      (json->Pool)))
