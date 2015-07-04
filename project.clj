(defproject arpad "0.1.0-SNAPSHOT"
  :description "An Elo ratings bot"
  :url "https://github.com/ahammel/arpad"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/core.match "0.3.0-alpha4"]]
  :main ^:skip-aot arpad.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
