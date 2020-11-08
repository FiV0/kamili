(ns io.kamili.ui.subs
  (:require [re-frame.core :as re-frame]
            io.kamili.ui.events))

(re-frame/reg-sub
 :api/query
 (fn [db [_ request]]
   (if-let [data (get-in db [:api/result request])]
     data
     (do
       (re-frame/dispatch [:http-query-api request])
       [:loading]))))
