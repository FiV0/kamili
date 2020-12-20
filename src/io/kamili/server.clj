(ns io.kamili.server
  (:require [immutant.web :as immutant]
            [integrant.core :as ig]
            [io.kamili.pedestal.server] ;; for integrant
            [io.kamili.log :as log]
            [io.kamili.server.routes]
            [io.kamili.server.transit :as transit]
            [io.pedestal.http :as pedestal]
            [muuntaja.core :as muuntaja]
            [reitit.coercion.malli]
            [reitit.core]
            [reitit.interceptor.sieppari :as sieppari]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as rrc]
            [reitit.ring.middleware.exception :as exception-mw]
            [reitit.ring.middleware.multipart :as multipart-mw]
            [reitit.ring.middleware.muuntaja :as muuntaja-mw]
            [reitit.ring.middleware.parameters :as parameters-mw]
            [ring.middleware.keyword-params :as keyword-params-mw]
            [ring.middleware.session :as session-mw]
            [ring.util.response])
  (:import [java.util UUID]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def wrap-keyword-params
  {:name ::keyword-params
   :wrap keyword-params-mw/wrap-keyword-params})

(def wrap-session
  {:name ::session
   :wrap session-mw/wrap-session})

(def wrap-uid
  {:name ::uid
   :wrap
   (fn [handler]
     (fn [req]
       (handler
        (if (get-in req [:session :uid])
          req
          (update req :session assoc :uid (str (UUID/randomUUID)))))))})

(def muuntaja-instance
  (-> muuntaja/default-options
      (update-in [:formats "application/transit+json" :encoder-opts] merge transit/encoder-opts)
      (update-in [:formats "application/transit+json" :decoder-opts] merge transit/decoder-opts)
      muuntaja/create))

(defn ring-default-handler []
  (ring/routes (ring/create-resource-handler {:path "/"})
               (ring/create-default-handler
                {:not-found (constantly (ring.util.response/resource-response "public/404.html"))})))

(def last-exception (volatile! nil))

(defn make-router [{:keys [routes]}]
  (ring/router routes
               {:expand (fn [route-args opts]
                          (reitit.core/expand route-args opts))
                :data {:coercion   (reitit.coercion.malli/create
                                    {;; set of keys to include in error messages
                                     :error-keys       #{#_:type :coercion :in :schema :value :errors :humanized #_:transformed}
                                     ;; ;; schema identity function (default: close all map schemas)
                                     ;; :compile mu/closed-schema
                                     ;; strip-extra-keys (affects only predefined transformers)
                                     :strip-extra-keys false #_true
                                     ;; add/set default values
                                     :default-values   true
                                     ;; malli options
                                     :options          nil})
                       :muuntaja   muuntaja-instance
                       :middleware [wrap-session
                                    wrap-uid
                                    parameters-mw/parameters-middleware     ;; query-params & form-params
                                    wrap-keyword-params                     ;; keywordize keys in :params map, does not touch :*-params
                                    muuntaja-mw/format-negotiate-middleware ;; content-negotiation
                                    muuntaja-mw/format-response-middleware  ;; encoding response body
                                    (exception-mw/create-exception-middleware
                                     {::exception-mw/default (fn [^Exception e _]
                                                               (vswap! last-exception (constantly e))
                                                               {:status 500
                                                                :body {:type "exception"
                                                                       :class (.getName (.getClass e))
                                                                       :message (.getMessage e)
                                                                       :trace (map str (.getStackTrace e))}})})
                                    muuntaja-mw/format-request-middleware   ;; decoding request body
                                    rrc/coerce-response-middleware
                                    rrc/coerce-request-middleware
                                    multipart-mw/multipart-middleware]}}))

(defn start! [{:keys [router pedestal-router http-server-opts type]}]
  {:server (if (= type :reitit)
             (immutant/run (ring/ring-handler router (ring-default-handler)
                                              {:executor sieppari/executor})
               http-server-opts)
             (-> pedestal-router pedestal/create-server pedestal/start))
   :type type})

(defn stop! [{:keys [server type] :as _inst}]
  (if (= type :reitit)
    (immutant/stop server)
    (pedestal/stop server)))

(defmethod ig/init-key :io.kamili.server/router [_ config]
  (make-router config))

(defmethod ig/init-key :io.kamili.server/server [_ config]
  (start! config))

(defmethod ig/halt-key! :io.kamili.server/server [_ inst]
  (stop! inst))