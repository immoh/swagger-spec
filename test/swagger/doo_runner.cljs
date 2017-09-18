(ns swagger.doo-runner
  (:require  [doo.runner :refer-macros [doo-tests]]
             [swagger.spec-test]))

(doo-tests 'swagger.spec-test)

