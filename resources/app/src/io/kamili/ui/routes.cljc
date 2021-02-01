(ns io.kamili.ui.routes)

(def routes
  [["/"
    {:name :nav/search
     :view :io.kamili.ui/search}]
   ["/results"
    ["/:search"
     {:name :nav/results
      :view :io.kamili.ui/results
      :api true}]]
   ["/person"
    ["/:id"
     {:name :nav/person
      :view :io.kamili.ui/person
      :api true}]]])
