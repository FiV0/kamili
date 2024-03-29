(ns io.kamili.server.routes
  (:require [clojure.string :as str]
            [hiccup2.core :as hiccup]
            [integrant.core :as ig]
            [io.kamili.handlers.auth :as auth]
            [io.kamili.handlers.db :as db]
            [io.kamili.server.views :as views]
            [io.kamili.ui.routes :as routes]
            [lambdaisland.glogc :as log]))

;; routes only for the backend
(def routes
  [["/api"
    ["/person/:id"
     {:get {:parameters {:path [:map [:id int?]]}
            ;; example interceptor
            :interceptors [auth/authorized?]
            :handler (fn [{:keys [uri] {:keys [path]} :parameters :as _ctx}]
                       {:status 200
                        :body   (assoc (db/get-person (:id path))
                                       :uri uri)})}}]
    ["/results/:search"
     {:get {:parameters {:path [:map [:search string?]]}
            :handler (fn [{{:keys [path]} :parameters :as _ctx}]
                       {:status 200
                        :body (db/search (:search path))})}}]]])

;; FIXME: This is just a cheap hack to make the template work with
;; renaming of namespaces. (script-entrypoint) can be replaced with
;; a static string similar to "you.awesome.app.ui.main()".
(def script-entrypoint
  (let [ns-str (str *ns*)]
    (str (subs ns-str 0 (str/last-index-of ns-str ".server.routes"))
         ".ui.main()")))

(defn frontend-response [req]
  {:status 200
   :body (-> req
             (views/layout [:script script-entrypoint])
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
