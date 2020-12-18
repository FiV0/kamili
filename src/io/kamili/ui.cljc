(ns io.kamili.ui
  (:require [integrant.core :as ig]
            [io.kamili.logging :as log]
            [io.kamili.view :as view]
            [io.kamili.views]
            #?@(:cljs [[io.kamili.ui.events]
                       [io.kamili.ui.router :as router]
                       [io.kamili.ui.subs]
                       [re-frame.core :as re-frame]
                       [reagent.dom :as reagent-dom]])))

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
     (log/info ::init! build-keys)
     (catch :default e
       (log/error ::init! (ex-data e) :exception (ex-cause e))))))

(defn app [view data]
  [:<>
   [:div.m-5 (view/route-view view data)]])

#?(:cljs
   (defn app-init []
     (let [{:keys [path] :as match} @(re-frame/subscribe [::view])
           data @(re-frame/subscribe [:api/query path])]
       [app (some-> match :data :view) data])))

#?(:cljs
   (defn ^:export ^:dev/after-load main []
     (init! config)
     (reagent-dom/render [app-init] (js/document.getElementById "app"))))
