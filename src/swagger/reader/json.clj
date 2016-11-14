(ns swagger.reader.json
  (:require [cheshire.core :as cheshire]))

(defn parse-string
  "Returns Clojure object corresponding to the given JSON-encoded string.
  Keys are coerced to keywords if they can be coerced back to the same string,
  otherwise they are left as strings."
  [s]
  (cheshire/parse-string s (fn [k]
                             (let [kw (keyword k)]
                               (if (and kw (= (name kw) k))
                                 kw
                                 k)))))
