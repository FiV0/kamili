(ns io.kamili.ui.routes)

(def routes
  [["/"
    {:name :nav/search
     :view :kamili.ui/search}]
   ["/results"
    ["/:search"
     {:name :nav/results
      :view :kamili.ui/results}]]
   ["/person"
    ["/:id"
     {:name :nav/person
      :view :kamili.ui/person}]]])
