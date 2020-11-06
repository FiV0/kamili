(ns io.kamili.view.person
  (:require [io.kamili.view :as view]))

(defmethod io.kamili.view/route-view :kamili.ui/person [_match]
  [:div [:h1 "Person"]])
