(ns io.kamili.ui.routes
  (:require #?(:clj [io.kamili.handlers.db :as db])))

(def routes
  [["/"
    {:name :nav/search
     :view :kamili.ui/search}]
   ["/results"
    ["/:search"
     {:name :nav/results
      :view :kamili.ui/results
      :parameters {:path [:map [:search string?]]}
      #?@(:clj [:handler (fn [{{:keys [path]} :parameters :as _ctx}]
                           {:status 200
                            :body (db/search (:search path))})])}]]
   ["/person"
    ["/:id"
     {:name :nav/person
      :view :kamili.ui/person
      :parameters {:path [:map [:id int?]]}
      #?@(:clj [:handler (fn [{{:keys [path]} :parameters :as _ctx}]
                           {:status 200
                            :body   (db/get-person (:id path))})])}]]])
