(ns arpad.commands.english
  (:require [arpad.release-notes :refer [latest]]
            [arpad.help :refer [help]]
            [clojure.core.match :refer [match]]
            [clojure.java.io :as io]
            [instaparse.core :as insta]))

(def parser
  (insta/parser (io/resource "english.bnf")))

(defn str->Command
  [string]
  (match [(insta/parse parser string)]
    [[:Command [:NewGame [:Player a] [:Player b]]]]
    {:cmd :new-game
     :score [{:id (keyword a)} {:id (keyword b)} 1]}

    [[:Command [:Follow [:Player p]]]]
    {:cmd :follow
     :player {:id (keyword p)}}

    [[:Command [:Ignore [:Player p]]]]
    {:cmd :ignore
     :player {:id (keyword p)}}

    [[:Command [:Standings]]]
    {:cmd :standings}

    [[:Command [:Standings n]]]
    {:cmd :standings
     :number (Integer/parseInt n)}

    [[:Command [:Rating [:Player p]]]]
    {:cmd :rating
     :player {:id (keyword p)}}

    [[:Command [:Undo]]]
    {:cmd :undo}

    [[:Command [:Help]]]
    {:help help}

    [[:Command [:ReleaseNotes]]]
    {:release-notes latest}

    :else
    {:error string}))
