(ns io.kamili.server.routes
  (:require [integrant.core :as ig]
            [io.kamili.server.views :as views]
            [io.kamili.ui.routes :as routes]
            [io.kamili.handlers.auth :as auth]
            [io.kamili.handlers.db :as db]
            [io.kamili.log :as log]
            [reitit.coercion.malli]
            [reitit.coercion.spec]
            [hiccup2.core :as hiccup]))

;; routes only for the backend
(def routes
  [["/api"
    ["/person/:id"
     {:get {:parameters {:path [:map [:id int?]]}
            :handler (fn [{:keys [uri] {:keys [path]} :parameters :as _ctx}]
                       {:status 200
                        :body   (assoc (db/get-person (:id path))
                                       :uri uri)})}}]
    ["/results/:search"
     {:get {:parameters {:path [:map [:search string?]]}
            :handler (fn [{{:keys [path]} :parameters :as _ctx}]
                       {:status 200
                        :body (db/search (:search path))})}}]]])

(defn frontend-response [req]
  {:status 200
   :body (-> req
             (views/layout [:script "io.kamili.ui.main()"])
             hiccup/html
             str)})

(defn flatten-routes [routes]
  (reduce (fn [acc [path & route-data]]
            (concat acc
                    (for [r route-data
                          :when (map? r)]
                      [path r])
                    (for [[p r] (flatten-routes (filter vector? route-data))]
                      [(str path p) r])))
          [] routes))

(defmethod ig/init-key :io.kamili.server/routes [_ _]
  (into routes

        (map (fn [[path _]]
               [path {:get frontend-response}]))
        (flatten-routes routes/routes)))
