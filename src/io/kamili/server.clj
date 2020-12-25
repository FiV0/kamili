(ns io.kamili.server
  (:require [cheshire.core :as cheshire]
            [integrant.core :as ig]
            [io.kamili.log :as log]
            [io.kamili.server.routes]
            [io.kamili.server.transit :as transit]
            [io.pedestal.http :as server]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.content-negotiation :as content-negotiation]
            [io.pedestal.http.params :as http.params]
            [io.pedestal.http.ring-middlewares :as http.ring-middleware]
            [io.pedestal.interceptor :as interceptor]
            [reitit.coercion.malli :as malli]
            [reitit.core]
            [reitit.http :as http]
            [reitit.http.coercion :as coercion]
            [reitit.pedestal :as pedestal]
            [reitit.ring :as ring]
            [ring.middleware.params]
            [ring.util.response :as response])
  (:import [java.util UUID]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defn ring-default-handler []
  (ring/routes (ring/create-resource-handler {:path "/"})
               (ring/create-default-handler
                {:not-found (constantly (assoc (ring.util.response/resource-response "public/404.html")
                                               :status 404))})))

(def formats
  ;; ordering matters here, html should come first
  {:html    {:extension    #"\.html$"
             :content-type "text/html"}
   :transit {:extension    #"\.transit$"
             :content-type "application/transit+json"}
   :json    {:extension    #"\.transit$"
             :content-type "application/json"}})

(def negotiate-content-interceptor
  (let [supported-content-types (map :content-type (vals formats))]
    (content-negotiation/negotiate-content supported-content-types)))

(defn accepted-type
  [context]
  (get-in context [:request :accept :field] "text/plain"))

(defn transform-content
  [body content-type]
  (case content-type
    "text/html"        body
    "text/plain"       body
    "application/edn"  (pr-str body)
    "application/json" (cheshire/encode body)
    "application/transit+json" (transit/encode body)))

(defn coerce-to
  [response content-type]
  (-> response
      (update :body transform-content content-type)
      (assoc-in [:headers "Content-Type"] content-type)))

(defn- response-complete?
  "Is this response already complete? i.e. has a status, body, and content-type?
  If so we don't touch it, to stay out of the way of handlers that do their own
  rendering."
  [{:keys [response]}]
  (and (:status response)
       (:body response)
       (get-in response [:headers "Content-Type"])))

(def body-params-interceptor
  (body-params/body-params
   (body-params/default-parser-map :transit-options [{:handlers transit/read-handlers}])))

(def transit-and-form-params-as-body-params-interceptor
  {:name ::transit-and-form-params-as-body-params-interceptor
   :enter
   (fn [context]
     (let [transit-params (-> context :request :transit-params http.params/keywordize-keys)
           form-params (get-in context [:request :form-params])]
       (-> context
           (assoc-in [:request :body-params] (merge transit-params form-params)))))})

(def coerce-body
  {:name ::coerce-body
   :leave
   (fn [context]
     (cond-> context
       (not (response-complete? context))
       (update-in [:response] coerce-to (accepted-type context))))})

(def root-interceptor
  {:name :root
   :leave
   (fn [context]
     ;; for now for debug only to inspect context at the very last interceptor
     context)})

(defn ensure-session-uid [req]
  (if (get-in req [:session :uid])
    req
    (update req :session assoc :uid (str (UUID/randomUUID)))))

(defn wrap-ensure-session-uid [handler]
  (fn [req]
    (handler (ensure-session-uid req))))

(def ensure-session-uid-interceptor
  {:name ::ensure-session-uid
   :enter
   (fn [context]
     (update context :request ensure-session-uid))})

(defn add-interceptors [interceptors]
  (into
   []
   (map interceptor/interceptor)
   (concat interceptors
           [root-interceptor
            coerce-body
            body-params-interceptor
            http.params/keyword-params
            transit-and-form-params-as-body-params-interceptor
            (http.ring-middleware/session)
            ensure-session-uid-interceptor
            negotiate-content-interceptor])))

(defn start! [{:keys [router http-server-opts websocket url]}]
  (let [s (-> {::server/type :jetty
               ::server/port (:port http-server-opts)
               ::server/host (:host http-server-opts)
               ::server/join? false

               ::server/request-logger nil

               ::server/secure-headers nil

               ;; no pedestal routes
               ::server/routes []}

              server/default-interceptors
              (update ::server/interceptors pop) ;; remove pedestal router, added last by default-interceptors

              (update ::server/interceptors add-interceptors)

              ;; use the reitit router
              (update ::server/interceptors conj
                      (pedestal/routing-interceptor router (ring-default-handler) {:interceptors []}))

              server/dev-interceptors
              server/create-server)]
    (server/start s)))

(defn stop! [inst]
  (server/stop inst))

(defn make-router [{:keys [routes]}]
  (http/router
   routes
   ;; the request/response coercion somehow needs to be plugged in here
   ;; as the coercion interceptors are not compatible with pedestal
   {:data {:coercion malli/coercion
           :interceptors [;; coercing response bodys
                          (coercion/coerce-response-interceptor)
                          ;; coercing request parameters
                          (coercion/coerce-request-interceptor)]}}))

(defmethod ig/init-key :io.kamili.server/router [_ config]
  (make-router config))

(defmethod ig/init-key :io.kamili.server/server [_ config]
  (start! config))

(defmethod ig/halt-key! :io.kamili.server/server [_ inst]
  (stop! inst))
