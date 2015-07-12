(ns arpad.pool.schema
  (:require [schema.core :as s]
            [schema.coerce :refer [coercer json-coercion-matcher]]))

(def Player
  "Player data as represented in a pool"
  {(s/required-key :rating) s/Num
   (s/optional-key :ignore?) s/Bool
   (s/optional-key :total-games) s/Int
   (s/optional-key :peak-rating) s/Num})

(def Pool
  {(s/required-key :players)        {s/Keyword Player}
   (s/required-key :default-rating) s/Num
   (s/required-key :k)              (s/either
                                      (s/enum :fide :uscf)
                                      s/Num)})

(def json->Pool
  (coercer Pool json-coercion-matcher))
