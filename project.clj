(defproject swagger-spec "0.4.0-SNAPSHOT"
  :description "clojure.spec spec for Swagger definition"
  :url "https://github.com/immoh/swagger-spec"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17" :scope "provided"]
                 [org.clojure/clojurescript "1.9.908" :scope "provided"]
                 [org.clojure/spec.alpha "0.1.123"]
                 [circleci/clj-yaml "0.5.5"]
                 [cheshire "5.7.1"]
                 [cljsjs/js-yaml "3.3.1-0"]]
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-doo "0.1.7"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]
                                  [cljs-node-io "0.5.0"]]}}
  :doo {:build "test"
        :alias {:default [:node]}}
  :cljsbuild {:builds [{:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "resources/public/js/testable.js"
                                   :main swagger.doo-runner
                                   :optimizations :none
                                   :target :nodejs}}]})
