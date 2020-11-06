(ns io.kamili.bootstrap.shadow-cljs
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [integrant.core :as ig]
            [shadow.cljs.devtools.api :as shadow]
            [shadow.cljs.devtools.server :as server]))

(defmethod ig/init-key :io.kamili.bootstrap/shadow-cljs [_ {:keys [builds watch-opts] :as config}]
  (when (seq config)
    (when-not (.exists (io/file "node_modules"))
      (shell/sh "yarn" "install"))
    (server/start!)
    (doseq [build builds]
      (shadow/watch build watch-opts)))
  config)

(defmethod ig/suspend-key! :io.kamili.bootstrap/shadow-cljs [_ {:keys [builds _watch-opts] :as _config}]
  (doseq [build builds]
    (shadow/stop-worker build)))

(defmethod ig/resume-key :io.kamili.bootstrap/shadow-cljs [_ {:keys [builds watch-opts] :as config} _ _]
  (doseq [build builds]
    (shadow/watch build watch-opts))
  config)
