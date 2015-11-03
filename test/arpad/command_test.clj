(ns arpad.command-test
  (:require [arpad.commands.english :as eng]
            [arpad.help :refer [help]]
            [arpad.release-notes :refer [latest]]
            [clojure.test :refer :all]))

(deftest english-commands-test
  (testing "new game"
    (is (= (eng/str->Command "arpad: masoud beat ajh")
           {:cmd :new-game
            :score [{:id :masoud} {:id :ajh} 1]})))
  (testing "follow"
    (is (= (eng/str->Command "arpad: follow lola")
           {:cmd :follow :player {:id :lola}})))
  (testing "unfollow"
    (is (= (eng/str->Command "arpad ignore bob")
           {:cmd :ignore :player {:id :bob}})))
  (testing "standings"
    (is (= (eng/str->Command "arpad: standings")
           {:cmd :standings}))
    (is (= (eng/str->Command "arpad: standing")
           {:cmd :standings}))
    (is (= (eng/str->Command "arpad: top 10")
           {:cmd :standings :number 10})))
  (testing "rating"
    (is (= (eng/str->Command "arpad: alice rating")
           {:cmd :rating :player {:id :alice}})))
  (testing "undo"
    (is (= (eng/str->Command "arpad: undo")
           {:cmd :undo})))
  (testing "help"
    (is (= (eng/str->Command "arpad help")
           {:help help})))
  (testing "release notes"
    (is (= (eng/str->Command "arpad release notes")
           {:release-notes latest})))
  (testing "nonsense"
    (is (= (eng/str->Command "arpad: I really like bananas")
           {:error "arpad: I really like bananas"}))))
