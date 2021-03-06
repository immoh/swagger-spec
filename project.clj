(defproject swagger-spec "0.6.0-SNAPSHOT"
  :description "clojure.spec spec for Swagger definition"
  :url "https://github.com/immoh/swagger-spec"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/spec.alpha "0.1.143"]]
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-doo "0.1.7"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.9.0"]
                                  [org.clojure/clojurescript "1.10.238"]
                                  [org.clojure/test.check "0.9.0"]
                                  [circleci/clj-yaml "0.5.6"]
                                  [cheshire "5.8.0"]
                                  [cljsjs/js-yaml "3.3.1-0"]
                                  [cljs-node-io "0.5.0"]]}}
  :doo {:build "test"
        :alias {:default [:node]}}
  :cljsbuild {:builds [{:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "target/testable.js"
                                   :output-dir "target/out"
                                   :main swagger.doo-runner
                                   :optimizations :none
                                   :target :nodejs}}]})
