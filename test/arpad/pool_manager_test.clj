(ns arpad.pool-manager-test
  (:require [clojure.test       :refer :all]
            [clojure.core.async :refer [>! <! <!! alts!! chan go timeout]]
            [arpad.test-common  :refer [close?]]
            [arpad.pool.manager :refer :all]))

(defn- get-result
  [chan]
  (first (alts!! [chan (timeout 100)])))

(deftest pool-manager-tests
  (let [pool-agent (agent {:players        {}
                           :k              (constantly 40)
                           :default-rating 1000})
        in (chan)
        chans (spawn-pool-manager pool-agent in)]
    (testing "new players draw"
      (go (>! in {:new-game [{:id :karpov} {:id :kasparov} 0.5]}))
      (let [result (get-result (:player-report chans))]
        (is (close? 1000 (get-in result [:karpov   :rating])))
        (is (close? 1000 (get-in result [:kasparov :rating])))
        (is (= 1 (get-in result [:karpov   :total-games])))
        (is (= 1 (get-in result [:kasparov :total-games])))))
    (testing "kasparov wins"
      (go (>! in {:new-game [{:id :karpov} {:id :kasparov} 0]}))
      (let [result (get-result (:player-report chans))]
        (is (> (get-in result [:kasparov :rating])
               (get-in result [:karpov   :rating])))))
    (testing "standings"
      ; Order of events is important here
      (testing "no limit"
        (go (>! in {:standings nil}))
        (let [result (get-result (:player-report chans))]
          (is (= (into [] (map first result))
                 [:kasparov :karpov]))))
      (testing "limit of one"
        (go (>! in {:standings 1}))
        (let [result (get-result (:player-report chans))]
          (is (= (into [] (map first result))
                 [:kasparov])))))))
