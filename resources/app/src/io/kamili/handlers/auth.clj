(ns io.kamili.handlers.auth
  (:require [io.kamili.log :as log]))

(def authorized?
  {:doc "Check whether the person has access to the requested resource."
   :name ::authorized?
   :leave  (fn [ctx]
             (log/info :authorized-leave (-> ctx :response))
             ctx)})
