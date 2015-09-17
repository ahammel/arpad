(ns arpad.slack-parser-test
  (:require [arpad.slack.parser :refer [parse-slack-request]]
            [arpad.release-notes :refer :all]
            [clojure.test :refer :all]))

(deftest parse-slack-request-test
  (testing "new game"
    (is (= (parse-slack-request {:text "arpad: masoud beat ajh"})
           {:new-game [{:id :masoud} {:id :ajh} 1]})))
  (testing "follow"
    (is (= (parse-slack-request {:text "arpad: follow lola"})
           {:follow {:id :lola}})))
  (testing "unfollow"
    (is (= (parse-slack-request {:text "arpad ignore bob"})
           {:ignore {:id :bob}})))
  (testing "standings"
    (is (= (parse-slack-request {:text "arpad: standings"})
           {:standings nil}))
    (is (= (parse-slack-request {:text "arpad: top 10"})
           {:standings 10})))
  (testing "release notes"
    (is (= (parse-slack-request {:text "arpad release notes"})
           {:release-notes alpha-0-0-2})))
  (testing "nonsense"
    (is (= (parse-slack-request {:text "arpad: I really like bananas"})
           nil))))

(deftest parse-slack-request-first-person-test
  (testing "new game"
    (is (= (parse-slack-request {:text "arpad: I beat ajh"
                                 :user_name "masoud"})
           {:new-game [{:id :masoud} {:id :ajh} 1]})))
  (testing "follow"
    (is (= (parse-slack-request {:text "arpad: follow me"
                                 :user_name "lola"})
           {:follow {:id :lola}})))
  (testing "unfollow"
    (is (= (parse-slack-request {:text "arpad ignore my ratings"
                                 :user_name "bob"})
           {:ignore {:id :bob}}))))
