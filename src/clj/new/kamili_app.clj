(ns clj.new.kamili-app
  (:require [clj.new.templates :as template]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [kusonga.move :as move]
            [me.raynes.fs :as fs]))

(defn project-artifact-id [s]
  (first (str/split s #"/")))

(defn- update-file
  "Reads file as a string, calls f on the string plus any args, then
  writes out return value of f as the new contents of file. Does not
  modify file if the content is unchanged."
  [file f & args]
  (let [old (slurp file)
        new (str (apply f old args))]
    (when-not (= old new)
      (spit file new))))

(defn kamili-app [name]
  (let [project-name (template/project-name name)
        artifact-id (project-artifact-id name)
        project (io/file (io/resource "app"))
        target (io/file (str project-name))]
    (println "Generating a project called"
             project-name
             "based on the 'kamili-app' template.")
    (fs/copy-dir project target)
    (move/rename-prefix
     'io.kamili (symbol artifact-id) [(io/file target "src") (io/file target "dev")])
    (move/replace-ns-symbol-in-source-files
     'io.kamili (symbol artifact-id) ".edn" [target])
    (update-file (io/file target "resources/logback.xml")
                 #(str/replace % "io.kamili" artifact-id))))
