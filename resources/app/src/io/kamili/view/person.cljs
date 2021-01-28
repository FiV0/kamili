(ns io.kamili.view.person
  (:require [io.kamili.view :as view]
            [io.kamili.log :as log]
            [re-frame.core :as rf]))

(defn person [[tag res]]
  (log/info :kamili.ui/person {:tag tag :res res})
  (case tag
    :loading [:div [:h1 "Loading..."]]
    :error [:div [:h1 "Error!" [:pre (pr-str res)]]]
    :result [:div
             [:h4 "Name: " (:name res)]
             [:br]
             [:h4 "Profession: " (:profession res)]]))

(defmethod io.kamili.view/route-view :kamili.ui/person [_view data]
  [person data])
