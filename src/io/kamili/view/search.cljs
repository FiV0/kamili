(ns io.kamili.view.search
  (:require [io.kamili.view :as view]))

(defmethod io.kamili.view/route-view :kamili.ui/search [_match]
  [:div [:h1 "Search"]])
