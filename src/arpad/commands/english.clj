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
    {:new-game [{:id (keyword a)} {:id (keyword b)} 1]}

    [[:Command [:Follow [:Player p]]]]
    {:follow {:id (keyword p)}}

    [[:Command [:Ignore [:Player p]]]]
    {:ignore {:id (keyword p)}}

    [[:Command [:Standings]]]
    {:standings nil}

    [[:Command [:Standings n]]]
    {:standings (Integer/parseInt n)}

    :else
    {:error string}))
