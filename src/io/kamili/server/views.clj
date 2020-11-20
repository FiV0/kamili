(ns io.kamili.server.views)

(defn layout [req app & body]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport" :content "minimum-scale=1, initial-scale=1, width=device-width, shrink-to-fit=no"}]
    [:meta {:name "csrf-token" :content (:anti-forgery-token req)}]
    [:link
     {:href "https://cdn.jsdelivr.net/npm/@tailwindcss/ui@latest/dist/tailwind-ui.min.css"
      :rel "stylesheet"}]
    [:link
     {:href "https://cdnjs.cloudflare.com/ajax/libs/IBM-type/0.5.4/css/ibm-type.min.css"
      :rel "stylesheet"}]
    [:style
     (str
      "body { font-family: IBM Plex Sans, sans-serif; } "
      ".scrollbar-none { scrollbar-width: none; -ms-overflow-style: none; } "
      ".scrollbar-none::-webkit-scrollbar { width: 0; height: 0; } "
      ;; Puget
      "code .class-delimiter {color: #a3685a;}
      code .class-name {color: #a3685a;}
      code .nil {color: #4d4d4c;}
      code .boolean {color: #4d4d4c;}
      code .number {color: #4271ae;}
      code .character {color: #a3685a;}
      code .string {color: #3e999f;}
      code .keyword {color: #4271ae;}
      code .symbol {color: #3e999f;}
      code .delimiter {color: #8959a8;}
      code .function-symbol {color: #8959a8;}
      code .tag {color: #a3685a;}
      code .insertion {color: #718c00;}
      code .deletion {color: #c82829;}")]]
   (into [:body
          [:div#app
           app]
          [:script {:src "/js/compiled/app.js" :type "text/javascript"}]]
         body)])
