(ns io.kamili.ui.router
  (:require [io.kamili.ui.routes :as routes]
            [integrant.core :as ig]
            ;; [lambdaisland.glogi :as log]
            [re-frame.core :as re-frame]
            [reitit.core :as reitit]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.frontend.history :as rfh]
            [io.kamili.log :as log]))


(def router (rf/router routes/routes))

(defn current-route
  "Get the currently active route, returns match data, configured route data is
  under the :data key."
  []
  (rf/match-by-path router (rfh/-get-path @rfe/history)))

(defn update-query!
  "Clojure style update function for the query string, takes a function accepts a
  query-params map and returns an updated query params map, which is then pushed
  onto the browsing history, and then routed through reitit."
  [f & args]
  (let [route (current-route)
        new-params (apply f (:query-params route) args)
        new-path (reitit/match->path route new-params)]
    (.pushState js/window.history nil "" new-path)
    (rfh/-on-navigate @rfe/history new-path)))

(defn query-link
  "Returns both a link and an on-click property which will both update the query
  params"
  ([params]
   (query-link (:name (:data (current-route))) params))
  ([route params]
   (let [{:keys [path-params query-params]} (current-route)
         new-query-params (into {} (remove (comp nil? val)) (merge query-params params))]
     {:href (rfe/href route path-params new-query-params)
      :on-click #(update-query! (constantly new-query-params))})))

(defmethod ig/init-key ::router [_ _]

  (re-frame/reg-fx :push-history (fn [args] (apply rfe/push-state args)))
  (re-frame/reg-sub :io.kamili.ui/view (fn [db _] (:io.kamili.ui/view db)))
  (re-frame/reg-event-db :nav (fn [db [_ match]] (assoc db :io.kamili.ui/view match)))
  (re-frame/reg-event-fx :navigate-to (fn [_ctx [_ routing-data]] {:push-history routing-data}))

  (rfe/start!
   router
   (fn [new-match]
     (log/trace :routing-to new-match)
     (re-frame/dispatch [:nav new-match]))
   {:use-fragment false}))

(defn api-route? [path]
  (-> (rf/match-by-path router path) :data :api))

(comment
  (rf/match-by-name router :nav/search)

  (re-frame/subscribe [:io.kamili.ui/view])

  (current-route)
  )
