(defproject arpad "0.1.0-SNAPSHOT"
  :description "An Elo ratings bot"
  :url "https://github.com/ahammel/arpad"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [prismatic/schema "0.4.3"]
                 [org.clojure/data.json "0.2.6"]
                 [instaparse "1.4.1"]]
  :plugins [[lein-environ "1.0.0"]]
  :main ^:skip-aot arpad.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

{:env {:squiggly {:checkers [:eastwood :kibit]}}}
