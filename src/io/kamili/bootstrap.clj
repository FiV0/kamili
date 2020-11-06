(ns io.kamili.bootstrap
  "Bootstrap Kamili"
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [io.kamili.util :as util]
            [integrant.core :as ig]
            [integrant.repl :as ig-repl]))

(defmethod aero/reader 'ig/ref
  [_ _tag value]
  (ig/ref value))

(defmethod aero/reader 'ig/refset
  [_ _tag value]
  (ig/refset value))

(defn ig-config
  ([]
   (ig-config :default))
  ([profile]
   (aero/read-config (io/resource "kamili/system.edn") {:profile profile}) ))

(defn set-prep!
  ([]
   (set-prep! :default))
  ([profile]
   (ig-repl/set-prep! #(doto (ig-config profile) ig/load-namespaces))))

(defn ns-exists? [sym]
  (some #(io/resource (str (util/munge-name sym) "." %)) ["clj" "cljc"]))

(defn precompile! [_]
  (binding [*compile-path* "classes"]
    (run! compile
          (filter ns-exists?
                  (conj (with-redefs [ig/try-require identity]
                          (ig/load-namespaces (ig-config)))
                        'io.kamili.bootstrap.entrypoint)))))

(comment
  (ig-config)
  (set-prep!)
  (ig-repl/go)
  )
