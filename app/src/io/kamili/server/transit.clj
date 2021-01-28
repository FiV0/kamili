(ns io.kamili.server.transit
  (:require [cognitect.transit :as transit])
  (:import (java.io ByteArrayInputStream ByteArrayOutputStream)))

(def write-meta-transformation
  (comp transit/write-meta
        #(if (meta %)
           (vary-meta %
                      (fn [m]
                        ;; mainly for clojure.core.protocols/datafy
                        (into {} (remove (comp fn? val)) m)))
           %)))

(def write-handlers
  {java.time.Instant
   (transit/write-handler "inst" #(.toEpochMilli %))})

(def read-handlers
  {"inst"
   (transit/read-handler #(java.time.Instant/ofEpochMilli %))})

(def encoder-opts ;; used by muuntaja
  {:transform write-meta-transformation
   :handlers  write-handlers})

(def decoder-opts
  {:handlers read-handlers})

(defn encode
  ([x] (encode x :json))
  ([x writer-kind] (encode x writer-kind encoder-opts))
  ([x writer-kind encode-opts]
   (let [baos (ByteArrayOutputStream.)
         w    (transit/writer baos writer-kind encode-opts)
         _    (transit/write w x)
         ret  (.toString baos "utf-8")]
     (.reset baos)
     ret)))

(defn encode-stream [x stream]
  (let [w (transit/writer stream :json encoder-opts)]
    (transit/write w x)))

(defn decode
  ([x] (decode x :json))
  ([x reader-kind]
   (let [bais (ByteArrayInputStream. (.getBytes x))
         reader (transit/reader bais reader-kind {:handlers read-handlers})]
     (transit/read reader))))

(defn decode-stream [stream & [reader-kind]]
  (-> stream
      (transit/reader (or reader-kind :json) {:handlers write-handlers})
      transit/read))
