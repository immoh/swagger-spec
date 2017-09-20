(ns swagger.reader.yaml
  (:require #?@(:clj [[clj-yaml.core :as yaml]
                      [flatland.ordered.map :refer [ordered-map]]]
                :cljs [[cljsjs.js-yaml]])))

#?(:clj (extend-protocol yaml/YAMLCodec
          java.util.LinkedHashMap
          (decode
            [data keywords]
            (letfn [(decode-key [k]
                      (let [kw (and keywords (keyword k))]
                        (if (and kw (= (name kw) k))
                          kw
                          k)))]
              (into (ordered-map)
                    (for [[k v] data]
                      [(-> k (yaml/decode keywords) decode-key) (yaml/decode v keywords)]))))))

(defn parse-string
  "Returns the Clojure object corresponding to the given JSON-encoded string.
  String keys are coerced to keywords if they can be coerced back to the same string,
  otherwise they are left as they are."
  [s]
  #?(:clj (yaml/parse-string s :keywords true)
     :cljs (js->clj (js/jsyaml.load s) :keywordize-keys true)))
