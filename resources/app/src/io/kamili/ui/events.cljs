(ns io.kamili.ui.events
  (:require [ajax.core :as ajax]
            [io.kamili.log :as log]
            [re-frame.core :as re-frame]
            [day8.re-frame.http-fx]))

(re-frame/reg-sub
 :http-error
 (fn [db [_]]
   (get db :http-error)))

(re-frame/reg-event-db
 :bad-api-query
 (fn [db [_ request value]]
   (log/warn :bad-api-query {:request request :value value})
   (-> db
       (assoc :http-error value)
       (assoc-in [:api/result request] [:error value]))))

(re-frame/reg-event-db
 :successful-api-query
 (fn [db [_ request value]]
   (log/info :successful-api-query {:value value
                                    :request request})
   (assoc-in db [:api/result request] [:result value])))

(re-frame/reg-event-fx
 :http-query-api
 (fn [{:keys [db]} [_ request]]
   {:db (assoc-in db [:api/result request] [:loading])
    :http-xhrio {:method          :get
                 :uri             (str "/api" request)
                 :timeout         8000                                           ;; optional see API docs
                 ;; :response-format (ajax.edn/edn-response-format)
                 :response-format (ajax/transit-response-format)
                 #_(ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                 :on-success      [:successful-api-query request]
                 :on-failure      [:bad-api-query request]}}))
