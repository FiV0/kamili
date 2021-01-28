(ns io.kamili.util)

(defn munge-name [lib]
  (.. (name lib)
      (replace \- \_)
      (replace \. \/)))
