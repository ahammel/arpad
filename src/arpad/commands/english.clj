(ns arpad.commands.english
  (:require [clojure.java.io    :as io]
            [clojure.core.match :refer [match]]
            [instaparse.core    :as insta]))

(def parser
  (insta/parser (io/resource "english.bnf")))

(defn str->Command
  [string]
  (match [(insta/parse parser string)]
    [[:Command [:NewGame [:Player a] [:Player b]]]]
    {:new-game [(keyword a) (keyword b) 1]}

    [[:Command [:Follow [:Player p]]]]
    {:follow (keyword p)}

    [[:Command [:Ignore [:Player p]]]]
    {:ignore (keyword p)}

    [[:Command [:Standings]]]
    {:standings nil}

    [[:Command [:Standings n]]]
    {:standings (Integer/parseInt n)}

    :else
    {:error string}))
