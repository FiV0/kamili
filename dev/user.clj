(ns user
  (:require [clojure.java.browse :as browse]
            [integrant.repl :as repl]
            [integrant.repl.state :as state]
            [io.kamili.bootstrap :as bootstrap]
            [shadow.cljs.devtools.api :as shadow]
            [shadow.cljs.devtools.server :as server]
            [shadow.cljs.devtools.server.worker :as worker]))

(defn wait-for-cljs
  "Wait for the build with build-id to have compiled.
  shadow/watch only returns when the build is finished."
  [build-id]
  (if-some [worker (shadow/get-worker build-id)]
    (worker/sync! worker)
    (shadow/watch build-id)))

(defn cljs-repl
  "Connects to a given build-id. Defaults to `:app`."
  ([]
   (cljs-repl :app))
  ([build-id]
   (server/start!)
   (shadow/watch build-id)
   (shadow/nrepl-select build-id)))

(defn cljs-connect
  ([]
   (cljs-connect :app))
  ([build-id]
   (wait-for-cljs build-id)
   (shadow/nrepl-select build-id)))

(defn browse [port]
  (browse/browse-url (str "http://localhost:" port)))

(defn set-prep! []
  (repl/set-prep! (bootstrap/set-prep! :dev)))

(defn go []
  (set-prep!)
  (integrant.repl/go))

(defn reset []
  (set-prep!)
  (integrant.repl/reset))

(defn system []
  state/system)

(defn config []
  state/config)
