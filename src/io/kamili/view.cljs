(ns io.kamili.view)

(defmulti route-view
  (fn [match]
    (some-> match :data :view)))

(defmethod route-view :default [match]
  [:div "unsupported route " (pr-str match)])
