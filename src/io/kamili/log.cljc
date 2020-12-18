(ns io.kamili.log
  (:refer-clojure :exclude [case])
  #?(:cljs (:require-macros [io.kamili.log]))
  (:require [lambdaisland.glogi :as glogi]
            #?(:clj [io.pedestal.log :as pedestal])
            #?(:cljs [lambdaisland.glogi.console :as glogi-console])))

#?(:clj
   (do

     (defmacro case [& {:keys [cljs clj]}]
       `(if (:ns ~'&env) ~cljs ~clj))

     (defmacro trace [& keyvals]
       (case :clj  (#'pedestal/log-expr &form :trace keyvals)
             :cljs (#'glogi/log-expr &form :trace keyvals)))

     (defmacro debug [& keyvals]
       (case :clj  (#'pedestal/log-expr &form :debug keyvals)
             :cljs (#'glogi/log-expr &form :debug keyvals)))

     (defmacro info [& keyvals]
       (case :clj  (#'pedestal/log-expr &form :info keyvals)
             :cljs (#'glogi/log-expr &form :info keyvals)))

     (defmacro warn [& keyvals]
       (case :clj  (#'pedestal/log-expr &form :warn keyvals)
             :cljs (#'glogi/log-expr &form :warn keyvals)))

     (defmacro error [& keyvals]
       (case :clj  (#'pedestal/log-expr &form :error keyvals)
             :cljs (#'glogi/log-expr &form :error keyvals)))

     (defmacro spy [expr]
       (case :clj `(pedestal/spy ~expr)
             :cljs `(glogi/spy ~expr)))

     (defmacro with-context [ctx-map & body]
       `(pedestal/with-context ~ctx-map ~@body))

     (def format-name pedestal/format-name)
     (def counter pedestal/counter)
     (def gauge pedestal/gauge)
     (def histogram pedestal/histogram)
     (def meter pedestal/meter)))

#?(:cljs
   (do
     (glogi-console/install!)

     (glogi/set-levels
      '{:glogi/root :debug
        ;; io.kamili.ui.router :trace
        goog.net.XhrIo :warn
        re-frame :info
        ;; See all dispatch/subscribe events
        ;;re-frame.router :trace
        ;;re-frame.subs   :trace
        })))
