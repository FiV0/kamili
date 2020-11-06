(ns io.kamili.server.routes
  (:require [integrant.core :as ig]
            [io.kamili.server.views :as views]
            [io.kamili.ui.routes :as routes]
            [hiccup2.core :as hiccup]))

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
  (into []
        (map (fn [[path _]]
               [path {:get frontend-response}]))
        (flatten-routes routes/routes)))
