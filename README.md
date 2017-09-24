# swagger-spec [![Build Status](https://travis-ci.org/immoh/swagger-spec.svg?branch=master)](https://travis-ci.org/immoh/swagger-spec) [![Dependencies Status](https://jarkeeper.com/immoh/swagger-spec/status.svg)](https://jarkeeper.com/immoh/swagger-spec)

A library that contains [clojure.spec](http://clojure.org/about/spec) spec for
[Swagger definition](http://swagger.io/specification/)

## Dependencies

This library depends on Clojure 1.9 which is still in alpha. It is possible to use it with Clojure 1.8 together with
[clojure-future.spec](https://github.com/tonsky/clojure-future-spec) which is an unofficial backport of clojure.spec
for Clojure 1.8.

## Installation

Add the following dependency to your project file:

```clj
[swagger-spec "0.4.0"]
```

## Usage

This library registers spec `:swagger/definition` which can be used for validation and data generation.

Generally map keys are required to be keywords except in certain places where integers (status code in Responses
Object) and strings (path in Path Items object, mime type in Example Object) can also be used. This is to make it
easier to avoid some pitfalls with keywords (e.g. first slash is treated as namespace separator.)

### Validation

```clj
(ns example
  (:require [cheshire.core :as cheshire]
            [clojure.spec.alpha :as s]
            [swagger.spec]))

(s/valid? :swagger/definition (cheshire/parse-string (slurp "http://petstore.swagger.io/v2/swagger.json") true))
=> true

(s/explain :swagger/definition (-> (cheshire/parse-string (slurp "http://petstore.swagger.io/v2/swagger.json") true)
                                   (assoc :swagger "2.1")))
;; In: [:swagger] val: "2.1" fails spec: :swagger.definition/swagger at: [:swagger] predicate: #{"2.0"}
;; :clojure.spec.alpha/spec  :swagger/definition
;; :clojure.spec.alpha/value ...
```

### Data Generation

The spec also allows test data to be generated for use with [test.check](https://github.com/clojure/test.check):

```clj
(ns example2
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [swagger.spec]))

(binding [s/*recursion-limit* 0]
  (first (gen/sample (s/gen :swagger/definition) 1)))
=> {:swagger "2.0"
    :info {:title "", :version "", :contact {:url ""}, :description "", :termsOfService ""}
    ...}
```

Unfortunately the resulting Swagger definition is 20k lines long pretty printed so I won't include it here.
You can view the complete definition [here](https://gist.githubusercontent.com/immoh/a12b1b0dfebf9ec41e2c4553ba062da0/raw/8407535f2344fd075814f7989991168b2239c9fa/generated-swagger-definition.clj).

I would have expected `gen/sample` to generate simpler definition, especially when recursion limit is set to 0.
The default recursion limit of 4 causes OutOfMemoryError, the same happens if `gen/generate` is used instead
of `gen/sample`.

## Limitations

* `url` field values are not validated to be in a format of a URL
* `email` field values are not validated to be in a format of an email
* Mime type values are not validated to be in a format of a mime type as described in
[RFC 6838](https://tools.ietf.org/html/rfc6838)
* HTTP Status code values are not validated as described in [RFC 7231](https://tools.ietf.org/html/rfc7231#section-6)
and [IANA Status Code Registry](http://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml)
* `$ref` field values are not validated to be valid JSON pointers
* Path item object `$ref` field is not validated to point to a path item object
* `default` field value is not validated to conform to the defined data type
* XML object `namespace` values are not validated to be in a format of a URL
* `operationId` value uniqueness is not validated
* `consumes` field value is not validated to be either "multipart/form-data", "application/x-www-form-urlencoded"
or both for `file` parameters
* Names and locations of extension fields are not validated
* Items object allows field `description` and validates it to be a string (not in specification)

There are probably many more aspects of specification that are missing and probably many that have been misunderstood.
Some of the validations cannot be implemented using clojure.spec.

## Contributing

If you would like to implement a missing validation or fix a misunderstood one, please file a pull request
and I'll be more than happy to merge it.

## License

Copyright © 2016 Immo Heikkinen

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
