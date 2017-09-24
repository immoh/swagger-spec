(ns swagger.spec-test
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test :refer [deftest is]]
            [swagger.spec]
            [clojure.test.check.generators]
    #?@(:cljs [[cljs-node-io.core :as io :refer [slurp file-seq]]
               [cljsjs.js-yaml]]
        :clj  [[cheshire.core :as cheshire]
               [clj-yaml.core :as clj-yaml]
               [clojure.java.io :as io]])))

(def ^:private spec->freq (->> (slurp "src/swagger/spec.cljc")
                               (re-seq #":swagger[^ /)]*/[^ )\]\n]+")
                               (map #(keyword (subs % 1)))
                               (frequencies)))

(defn- valid-swagger? [path]
  (let [reader (case (last (clojure.string/split path #"\."))
                 "json" #?(:clj  #(cheshire/parse-string % true)
                           :cljs #(js->clj (js/JSON.parse %) :keywordize-keys true))
                 "yaml" #?(:clj  #(clj-yaml/parse-string % :keywords true)
                           :cljs #(js->clj (js/jsyaml.load %) :keywordize-keys true)))
        definition (reader (slurp path))]
    (or
      (s/valid? :swagger/definition definition)
      (s/explain :swagger/definition definition))))

(deftest valid-swagger-definition-conforms-to-spec
  (doseq [f #?(:clj  (rest (file-seq (io/file "test/resources/")))
               :cljs (rest (map io/file (file-seq "test/resources/"))))]
    (is (valid-swagger? (.getCanonicalPath f)))))

(deftest there-are-no-orphan-specs
  (is (= #{:swagger/definition}
         (set (keys (filter (fn [[_ freq]] (< freq 2)) spec->freq))))))

(defn- sample-one [spec]
  (try
    (binding [s/*recursion-limit* 0]
      (first (gen/sample (s/gen spec) 1)))
    :success
    #?(:clj (catch Throwable t
              (println "Failed to generate data for spec" spec ":" (class t) (.getMessage t)))
       :cljs (catch js/Error e
               (println "Failed to generate data for spec" spec ":" e)))))

#_(deftest specs-can-be-used-as-generators
  (doseq [spec (keys spec->freq)]
    (is (sample-one spec))))
