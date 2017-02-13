(ns swagger.spec-test
  (:require [clojure.java.io :as io]
            [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.test :refer :all]
            [swagger.reader.json :as json]
            [swagger.reader.yaml :as yaml]
            [swagger.spec]))

(def ^:private spec->freq (->> (slurp "src/swagger/spec.clj")
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
  (doseq [f (rest (file-seq (io/file "test/resources/")))]
    (is (valid-swagger? (.getCanonicalPath f)))))

(deftest there-are-no-orphan-specs
  (is (= #{:swagger/definition}
         (set (keys (filter (fn [[_ freq]] (< freq 2)) spec->freq))))))

(defn- sample-one [spec]
  (try
    (binding [s/*recursion-limit* 0]
      (first (gen/sample (s/gen spec) 1)))
    :success
    (catch Throwable t
      (println "Failed to generate data for spec" spec ":" (class t) (.getMessage t)))))

(deftest specs-can-be-used-as-generators
  (doseq [spec (keys spec->freq)]
    (is (sample-one spec))))
