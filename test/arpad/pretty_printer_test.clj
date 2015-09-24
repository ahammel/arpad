(ns arpad.pretty-printer-test
  (:require [arpad.pretty-printer.english :as eng]
            [arpad.help :refer [help]]
            [clojure.string :as string]
            [clojure.test :refer :all]))

(deftest english-pprint-test
  (testing "Follow message"
    (is (= (eng/pretty-print {:following {:id :bob}})
           "Following @bob")))
  (testing "Ignoring message"
    (is (= (eng/pretty-print {:ignoring {:id :sally}})
           "Ignoring @sally")))
  (testing "Players message"
    (is (= (eng/pretty-print {:players {:bob {:rating 1400
                                              :total-games 12
                                              :peak-rating 1800.111111}
                                        :phil {:rating 500
                                               :total-games 1000
                                               :peak-rating 9001.00110100}}})
           (str "```"
                "@bob                  "
                "Rating: 1400  Games played:   12  Peak rating: 1800\n"
                "@phil                 "
                "Rating:  500  Games played: 1000  Peak rating: 9001"
                "```"))))
  (testing "Empty players message"
    (is (= (eng/pretty-print {:players []})
           "OK")))
  (testing "Edge-case players message"
    ;; When the peak-rating is less than the current rating, sub in
    ;; the current rating
    (is (= (eng/pretty-print {:players {:alice {:rating 1000
                                                :total-games 10
                                                :peak-rating 0}}})
           (str "```"
                "@alice                "
                "Rating: 1000  Games played:   10  Peak rating: 1000"
                "```"
                ))))
  (testing "Standings message"
    (is (= (eng/pretty-print {:standings [[:williams {:rating 2321.1}]
                                          [:halep    {:rating 2217.9}]]})
           (str "```"
                " 1. @williams:                          2321\n"
                " 2. @halep:                             2217"
                "```"))))
  (testing "Empty standings message"
    ; An empty map just results in a confirmation that an action was
    ; carried out.
    (is (= (eng/pretty-print {:standings []})
           "OK")))
  (testing "Successful undo message"
    (is (= (eng/pretty-print {:undo 1})
           "OK, I've undone the last command")))
  (testing "Unsuccessful undo message"
    (is (= (eng/pretty-print {:error :cannot-undo})
           "Sorry, I can't undo any more!")))
  (testing "help message"
    (is (= (eng/pretty-print {:help 1}) help)))
  (testing "Nonsense message"
    ; Any other message results in an error message
    (is (= (eng/pretty-print {:foo :bar})
           "ERR: Cannot pretty-print the message '{:foo :bar}'"))))
