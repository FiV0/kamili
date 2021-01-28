((nil . ((cider-preferred-build-tool . clojure-cli)
         (cider-clojure-cli-global-options . "-A:dev:test")
         (cider-default-cljs-repl . custom)
         (cider-custom-cljs-repl-init-form . "(do (user/go) (user/cljs-connect) (user/browse 8280))")
         (cider-refresh-before-fn . "integrant.repl/suspend")
         (cider-refresh-after-fn . "integrant.repl/resume")
         (eval . (progn
                   (make-variable-buffer-local 'cider-jack-in-nrepl-middlewares)
                   (add-to-list 'cider-jack-in-nrepl-middlewares "shadow.cljs.devtools.server.nrepl/middleware"))))))
