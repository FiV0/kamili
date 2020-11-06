(ns io.kamili.bootstrap.entrypoint
  (:gen-class)
  (:require [io.kamili.bootstrap :refer [set-prep!]]
            [integrant.repl :as ig-repl]))

(defn -main [& args]
  (set-prep! (if (first args)
               (keyword (first args))
               :default))
  (ig-repl/go))
