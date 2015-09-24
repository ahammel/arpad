(ns arpad.slack-parser-test
  (:require [arpad.help :refer [help]]
            [arpad.slack.parser :refer [parse-slack-request]]
            [arpad.release-notes :refer [latest]]
            [clojure.test :refer :all]))

(deftest parse-slack-request-test
  (testing "new game"
    (is (= (parse-slack-request {:text "arpad: masoud beat ajh"})
           {:cmd :new-game
            :score [{:id :masoud} {:id :ajh} 1]})))
  (testing "new game in capitals"
    (is (= (parse-slack-request {:text "ARPAD: MASOUD BEAT AJH"})
           {:cmd :new-game
            :score [{:id :masoud} {:id :ajh} 1]})))
  (testing "follow"
    (is (= (parse-slack-request {:text "arpad: follow lola"})
           {:cmd :follow
            :player {:id :lola}})))
  (testing "unfollow"
    (is (= (parse-slack-request {:text "arpad ignore bob"})
           {:cmd :ignore
            :player {:id :bob}})))
  (testing "standings"
    (is (= (parse-slack-request {:text "arpad: standings"})
           {:cmd :standings}))
    (is (= (parse-slack-request {:text "arpad: top 10"})
           {:cmd :standings :number 10})))
  (testing "help"
    (is (= (parse-slack-request {:text "arpad help"})
           {:help help})))
  (testing "release notes"
    (is (= (parse-slack-request {:text "arpad release notes"})
           {:release-notes latest})))
  (testing "undo"
    (is (= (parse-slack-request {:text "arpad undo"})
           {:cmd :undo})))
  (testing "nonsense"
    (is (= (parse-slack-request {:text "arpad: I really like bananas"})
           nil))))

(deftest parse-slack-request-first-person-test
  (testing "new game"
    (is (= (parse-slack-request {:text "arpad: I beat ajh"
                                 :user_name "masoud"})
           {:cmd :new-game
            :score [{:id :masoud} {:id :ajh} 1]})))
  (testing "follow"
    (is (= (parse-slack-request {:text "arpad: follow me"
                                 :user_name "lola"})
           {:cmd :follow :player {:id :lola}})))
  (testing "unfollow"
    (is (= (parse-slack-request {:text "arpad ignore my ratings"
                                 :user_name "bob"})
           {:cmd :ignore :player {:id :bob}}))))
