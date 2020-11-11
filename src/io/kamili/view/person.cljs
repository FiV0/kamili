(ns io.kamili.view.person
  (:require [io.kamili.view :as view]
            [io.kamili.logging :as log]
            [re-frame.core :as rf]))

(defn person [{:keys [path] :as _match}]
  (let [[tag res] @(rf/subscribe [:api/query path])]
    (log/info :kamili.ui/person {:path path :tag tag :res res})
    (case tag
      :loading [:div [:h1 "Loading..."]]
      :error [:div [:h1 "Error!" [:pre (pr-str res)]]]
      :result [:div
               [:h4 "Name: " (:name res)]
               [:br]
               [:h4 "Profession: " (:profession res)]])))

(defmethod io.kamili.view/route-view :kamili.ui/person [match]
  [person match])
