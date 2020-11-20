(ns io.kamili.view.search
  (:require [io.kamili.view :as view]
            #?@(:cljs
                [[re-frame.core :as rf]
                 [reagent.core :as reagent]
                 [re-com.core :refer [input-text]]]))
  #?(:cljs (:require-macros [re-com.core :refer [handler-fn]])))

(defn search-page []
  #?(:clj [:input {:type "search"}]
     :cljs
     (let [query (reagent/atom "")]
       (fn []
         [input-text
          :model query
          :on-change #(reset! query %)
          :attr {:auto-focus false
                 :on-focus     (handler-fn (-> event .-target .select))
                 :on-key-press (handler-fn (case (.-which event)
                                             13 (rf/dispatch [:navigate-to [:nav/results {:search @query}]])
                                             nil))
                 :size 70
                 :type "text"
                 :placeholder "Search..."}
          :change-on-blur? false]))))

(defmethod io.kamili.view/route-view :kamili.ui/search [_match _]
  [search-page])