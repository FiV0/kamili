(ns io.kamili.view.results
  (:require [io.kamili.view :as view]))

(defmethod io.kamili.view/route-view :kamili.ui/results [_match]
  [:div [:h1 "Results"]])
