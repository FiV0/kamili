(ns io.kamili.ui
  (:require [integrant.core :as ig]
            [io.kamili.ui.events]
            [io.kamili.ui.router :as router]
            [io.kamili.ui.subs]
            [io.kamili.view :as view]
            [io.kamili.views]
            [lambdaisland.glogc :as log]
            [lambdaisland.glogi :as glogi]
            [lambdaisland.glogi.console :as glogi-console]
            [re-frame.core :as re-frame]
            [reagent.dom :as reagent-dom]))


(glogi-console/install!)

(glogi/set-levels
 '{:glogi/root :debug
   ;; io.kamili.ui.router :trace
   goog.net.XhrIo :warn
   re-frame :info
   ;; See all dispatch/subscribe events
   ;;re-frame.router :trace
   ;;re-frame.subs   :trace
   })

(defonce system (atom nil))

(def config
  {::router/router {}})

(defn init!
  ([config]
   (init! config (keys config)))
  ([config build-keys]
   (try
     (reset! system
             (ig/build config
                       build-keys
                       (fn [key value]
                         (log/trace :init-key {key value})
                         (ig/init-key key value))
                       ig/assert-pre-init-spec
                       ig/resolve-key))
     (log/config ::init! build-keys)
     (catch :default e
       (log/error ::init! (ex-data e) :exception (ex-cause e))))))

(defn app []
  [:<>
   (let [{:keys [path] :as match} @(re-frame/subscribe [::view])
         data (when (router/api-route? path) @(re-frame/subscribe [:api/query path]))]
     [:div.m-5 (view/route-view (some-> match :data :view) data)])])

(defn ^:export ^:dev/after-load main []
  (init! config)
  (reagent-dom/render [app] (js/document.getElementById "app")))
