(ns io.kamili.server.routes
  (:require [integrant.core :as ig]
            [io.kamili.server.views :as views]
            [io.kamili.ui.routes :as routes]
            [io.kamili.handlers.auth :as auth]
            [io.kamili.handlers.db :as db]
            [io.kamili.log :as log]
            [reitit.coercion.malli]
            [io.pedestal.http :as pedestal]
            [io.pedestal.http.route.definition.table :as table]
            [io.pedestal.service-tools.dev :as service-tools.dev]
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

;; pedestal
(defn api-person
  [req]
  (let [id (Integer/parseInt (get-in req [:path-params :id] 1))]
    {:status 200
     :body (db/get-person id)}))

(defn api-results
  [req]
  {:status 200
   :body (db/search (get-in req [:path-params :search]))})

(def routes2
  ;; interceptor vs simple response
  [["/api/person/:id"
    :get [auth/authorized? api-person]
    :route-name :api-person]
   ["/api/results/:search"
    :get [auth/authorized? api-results]
    :route-name :api-results]])

(def app (->
          (into routes2
                (map (fn [[path _]]
                       [path
                        :get [pedestal/html-body frontend-response]
                        :route-name (keyword path)]))
                (flatten-routes routes/routes))
          table/table-routes))

(defmethod ig/init-key :io.kamili.server/pedestal-routes [_ {:keys [autoreload]}]
  (if autoreload
    (service-tools.dev/watch-routes-fn #'app)
    app))
