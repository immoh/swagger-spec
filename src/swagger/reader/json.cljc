(ns swagger.reader.json
  (:require #?@(:clj [[cheshire.core :as cheshire]]
                :cljs [])))

(defn parse-string
  "Returns Clojure object corresponding to the given JSON-encoded string.
  Keys are coerced to keywords if they can be coerced back to the same string,
  otherwise they are left as strings."
  [s]
  #?(:clj (cheshire/parse-string s (fn [k]
                                     (let [kw (keyword k)]
                                       (if (and kw (= (name kw) k))
                                         kw
                                         k))))
     :cljs (js->clj (js/JSON.parse s) :keywordize-keys true)))
