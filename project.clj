(defproject swagger-spec "0.2.0-SNAPSHOT"
  :description "clojure.spec spec for Swagger definition"
  :url "https://github.com/immoh/swagger-spec"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14" :scope "provided"]
                 [circleci/clj-yaml "0.5.5"]
                 [cheshire "5.6.3"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]}})
