(ns swagger.spec-test
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test :refer [deftest is]]
            [swagger.spec]
            [clojure.test.check.generators]
            [swagger.reader.json :as json]
            [swagger.reader.yaml :as yaml]
            #?@(:cljs [[cljs-node-io.core :as io :refer [slurp file-seq]]]
                :clj [[clojure.java.io :as io]])))

(def ^:private spec->freq (->> (slurp "src/swagger/spec.cljc")
                               (re-seq #":swagger[^ /)]*/[^ )\]\n]+")
                               (map #(keyword (subs % 1)))
                               (frequencies)))

(defn- valid-swagger? [path]
  (let [reader (case (last (clojure.string/split path #"\."))
                 "json" json/parse-string
                 "yaml" yaml/parse-string)
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

(deftest specs-can-be-used-as-generators
  (doseq [spec (keys spec->freq)]
    (is (sample-one spec))))
