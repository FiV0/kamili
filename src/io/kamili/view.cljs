(ns io.kamili.view)

(defmulti route-view
  (fn [view _] view))

(defmethod route-view :default [view _data]
  [:div "unsupported route " view])
