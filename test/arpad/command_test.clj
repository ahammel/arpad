(ns arpad.command-test
  (:require [arpad.commands.english :as eng]
            [arpad.release-notes :refer [latest]]
            [clojure.test :refer :all]))

(deftest english-commands-test
  (testing "new game"
    (is (= (eng/str->Command "arpad: masoud beat ajh")
           {:new-game [{:id :masoud} {:id :ajh} 1]})))
  (testing "follow"
    (is (= (eng/str->Command "arpad: follow lola")
           {:follow {:id :lola}})))
  (testing "unfollow"
    (is (= (eng/str->Command "arpad ignore bob")
           {:ignore {:id :bob}})))
  (testing "standings"
    (is (= (eng/str->Command "arpad: standings")
           {:standings nil}))
    (is (= (eng/str->Command "arpad: top 10")
           {:standings 10})))
  (testing "rating"
    (is (= (eng/str->Command "arpad: alice rating")
           {:rating {:id :alice}})))
  (testing "undo"
    (is (= (eng/str->Command "arpad: undo")
           {:undo 1})))
  (testing "release notes"
    (is (= (eng/str->Command "arpad release notes")
           {:release-notes latest})))
  (testing "nonsense"
    (is (= (eng/str->Command "arpad: I really like bananas")
           {:error "arpad: I really like bananas"}))))
