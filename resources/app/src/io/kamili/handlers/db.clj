(ns io.kamili.handlers.db
  (:require [clojure.string :as str]
            [lambdaisland.glogc :as log]))

;; an artifial db, mapping :person-id to some data of that person
;; you should replace this with a proper database
(def db
  {1 {:name "Ada Lovelace"
      :id 1
      :profession "Programmer"}
   2 {:name "Alan Turing"
      :id 2
      :profession "Logician"}
   3 {:name "Claude Shannon"
      :id 3
      :profession "Mathematician"}
   4 {:name "Edsger W. Dijkstra"
      :id 4
      :profession "Computer Scientist"}})

(defn get-person [id]
  (get db id))

(defn search [needle]
  (let [needle (str/lower-case needle)]
    (->> db
         (filter (fn [[_ v]] (str/includes? (str/lower-case (:name v)) needle)))
         (map second))))
