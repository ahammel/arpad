(ns arpad.pool-manager-test
  (:require [clojure.test                 :refer :all]
            [clojure.core.async :as async :refer [>! <! <!! go]]
            [arpad.test-common            :refer [close?]]
            [arpad.pool.manager           :refer :all]))

(deftest pool-manager-tests
  (let [pool-agent (agent {:players        {}
                           :k              (constantly 40)
                           :default-rating 1000})
        in (async/chan)
        chans (spawn-pool-manager pool-agent in)]
    (testing "new players draw"
      (go (>! in {:new-game [{:id :karpov} {:id :kasparov} 0.5]}))
      (let [result (<!! (go (<! (:player-report chans))))]
        (is (close? 1000 (get-in result [:karpov   :rating])))
        (is (close? 1000 (get-in result [:kasparov :rating])))
        (is (= 1 (get-in result [:karpov   :total-games])))
        (is (= 1 (get-in result [:kasparov :total-games])))))))
