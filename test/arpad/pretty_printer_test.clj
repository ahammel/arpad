(ns arpad.pretty-printer-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [aprad.pretty-printer.english :as eng]))

(deftest english-pprint-test
  (testing "Follow message"
    (is (= (eng/pretty-print {:following {:id :bob}})
           "Following @bob")))
  (testing "Ignoring message"
    (is (= (eng/pretty-print {:ignoring {:id :sally}})
           "Ignoring @sally")))
  (testing "Standings message"
    (is (= (eng/pretty-print [[:williams {:rating 2321.1}]
                              [:halep    {:rating 2217.9}]])
           (string/join "\n" ["@williams:          2321"
                              "@halep:             2217"]))))
  (testing "Empty standings message"
    ; An empty map just results in a confirmation that an action was
    ; carried out.
    (is (= (eng/pretty-print {})
           "OK")))
  (testing "Nonsense message"
    ; Any other message results in an error message
    (is (= (eng/pretty-print {:foo :bar})
           "I didn't understand the message '{:foo :bar}'"))))
