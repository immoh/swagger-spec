(defproject swagger-spec "0.3.0"
  :description "clojure.spec spec for Swagger definition"
  :url "https://github.com/immoh/swagger-spec"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17" :scope "provided"]
                 [org.clojure/spec.alpha "0.1.123"]
                 [circleci/clj-yaml "0.5.5"]
                 [cheshire "5.7.1"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]}})
