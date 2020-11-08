(ns io.kamili.ui
  (:require [integrant.core :as ig]
            [io.kamili.ui.router :as router]
            [io.kamili.view :as view]
            [io.kamili.views]
            [re-frame.core :as re-frame]
            [reagent.dom :as reagent-dom]
            [lambdaisland.glogi :as log]
            [io.kamili.ui.events]
            [io.kamili.ui.subs]
            ))

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
   (let [match @(re-frame/subscribe [::view])]
     (view/route-view match))])

(defn ^:export ^:dev/after-load main []
  (init! config)
  (reagent-dom/render [app] (js/document.getElementById "app")))
