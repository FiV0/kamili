(ns io.kamili.view.results
  (:require [io.kamili.view :as view]
            [io.kamili.log :as log]
            [re-frame.core :as rf]))

(defn results [[tag res]]
  (log/info :kamili.ui/results {:tag tag :res res})
  (case tag
    :loading [:div [:h1 "Loading..."]]
    :error [:div [:h1 "Error!" [:pre (pr-str res)]]]
    :result (into [:div]
                  (-> (map (fn [{:keys [name id] :as _person}]
                             [:a
                              {:href "#"
                               :on-click #(rf/dispatch [:navigate-to [:nav/person {:id id}]])}
                              (str "Name: " name)]) res)
                      (interleave (repeat [:br]))))))

(defmethod io.kamili.view/route-view :kamili.ui/results [_ data]
  [results data])
