(ns io.kamili.http.pedestal.server
  (:require [integrant.core :as ig]
            [io.kamili.log :as log]
            [io.kamili.server.routes]
            [io.pedestal.http :as pedestal]
            [io.pedestal.http.params :as params]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.helpers :as interceptor-helpers]
            [reitit.coercion.malli]
            [reitit.core]
            [ring.util.response]))

(defn update-interceptors [interceptors]
  (into
   []
   (map interceptor/interceptor)
   (concat
    interceptors
    [params/keyword-params
     params/keyword-body-params
     params/keywordize-request-body-params
     params/keywordize-request-params
     (pedestal/transit-body-interceptor :transit-body-interceptor "application/transit+json;charset=UTF-8" :json)])))

(def default-response
  "An interceptor that returns a 404 when routing failed to resolve a route."
  (interceptor-helpers/after
   ::not-found
   (fn [context]
     (if-not (pedestal/response? (:response context))
       (assoc context :response (-> (ring.util.response/resource-response "public/404.html")
                                    (ring.util.response/content-type "text/html;charset=UTF-8")))
       context))))

(defn add-interceptors [service-map]
  (-> service-map
      pedestal/default-interceptors
      (update ::pedestal/interceptors update-interceptors)))

(defmethod ig/init-key :io.kamili.http/system-map [_ system-map]
  (assoc system-map
         ::pedestal/allowed-origins {:creds true :allowed-origins (constantly true)}
         ::pedestal/not-found-interceptor default-response))

(defmethod ig/init-key :io.kamili.http/pedestal-router [_ {:keys [system-map]}]
  (-> system-map
      pedestal/default-interceptors
      pedestal/dev-interceptors
      add-interceptors))
