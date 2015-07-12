(ns arpad.command-test
  (:require [clojure.test           :refer :all]
            [arpad.commands.english :as eng]))

(deftest english-commands-test
  (testing "new game"
    (is (= (eng/str->Command "arpad: @masoud beat @ajh")
           {:new-game [:masoud :ajh 1]})))
  (testing "follow"
    (is (= (eng/str->Command "arpad: follow @lola")
           {:follow :lola})))
  (testing "unfollow"
    (is (= (eng/str->Command "arpad ignore @bob")
           {:ignore :bob})))
  (testing "standings"
    (is (= (eng/str->Command "arpad: standings")
           {:standings nil}))
    (is (= (eng/str->Command "arpad: top 10")
           {:standings 10})))
  (testing "nonsense"
    (is (= (eng/str->Command "arpad: I really like bananas")
           {:error "arpad: I really like bananas"}))))
