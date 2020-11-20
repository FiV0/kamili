(ns io.kamili.server.routes
  (:require [integrant.core :as ig]
            [io.kamili.server.views :as views]
            [io.kamili.ui.routes :as routes]
            [io.kamili.handlers.auth :as auth]
            [io.kamili.handlers.db :as db]
            [io.kamili.logging :as log]
            [reitit.coercion.malli]
            [io.pedestal.http :as pedestal]
            [io.pedestal.http.route.definition.table :as table]
            [io.pedestal.service-tools.dev :as service-tools.dev]
            [reitit.coercion.spec]
            [io.kamili.ui :as ui]
            [hiccup2.core :as hiccup]))

(defn frontend-response [req app-data]
  {:status 200
   :body (-> req
             (views/layout app-data [:script "io.kamili.ui.main()"])
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
  (let [flattened-routes (flatten-routes routes/routes)
        frontend-routes (map (fn [[path {:keys [view data] :as route-data}]]
                               (log/info :frontend {:view-name view
                                                    :data data})
                               [path {:get (merge
                                            (select-keys route-data [:parameters])
                                            {:handler (fn [ctx]
                                                        (frontend-response ctx (ui/app view (when data
                                                                                              [:result (data ctx)]))))})}])
                             flattened-routes)
        api-routes (->> flattened-routes
                        (filter #(-> % second :handler))
                        (map (fn [[path route-data]]
                               [(str "/api" path) {:get (select-keys route-data [:parameters :handler])}])))]
    (into [] (concat frontend-routes api-routes))))

;; pedestal
(defn frontend-response2 [req]
  {:status 200
   :body (-> req
             (views/layout nil [:script "io.kamili.ui.main()"])
             hiccup/html
             str)})

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
                        :get [pedestal/html-body frontend-response2]
                        :route-name (keyword path)]))
                (flatten-routes routes/routes))
          table/table-routes))

(defmethod ig/init-key :io.kamili.server/pedestal-routes [_ {:keys [autoreload]}]
  (if autoreload
    (service-tools.dev/watch-routes-fn #'app)
    app))
