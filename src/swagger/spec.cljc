(ns swagger.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string]))

;; Coercion

(defn keyword->int [x]
  (cond
    (int? x) x
    (nil? x) ::s/invalid
    :else #?(:clj (try
                    (Integer/parseInt (name x))
                    (catch NumberFormatException _
                      ::s/invalid))
             :cljs (let [result (js/parseInt (name x))]
                     (if (.isFinite js/Number result)
                       result
                       ::s/invalid)))))

(defn- keyword->str [x]
  (cond
    (string? x) x
    (keyword? x) (subs (str x) 1)
    :else ::s/invalid))

;; Shared

(s/def :swagger/path (s/with-gen (s/and string? #(clojure.string/starts-with? % "/"))
                                 #(gen/fmap (partial str "/") (gen/string-alphanumeric))))
(s/def :swagger/$ref string?)
(s/def :swagger/schemes (s/* #{"http" "https" "ws" "wss"}))
(s/def :swagger/extension (s/with-gen (s/and keyword? #(clojure.string/starts-with? (name %) "x-"))
                                      #(gen/fmap (fn [s] (keyword (str "x-" s))) (gen/string-alphanumeric))))

;; JSON Schema

(s/def :swagger.json-schema/simple-type #{"array" "boolean" "integer" "number" "null" "object" "string"})
(s/def :swagger.json-schema/type (s/or :simple-type :swagger.json-schema/simple-type
                                       :multiple-types (s/+ :swagger.json-schema/simple-type)))
(s/def :swagger.json-schema/format string?)
(s/def :swagger.json-schema/title string?)
(s/def :swagger.json-schema/description string?)
(s/def :swagger.json-schema/default any?)
(s/def :swagger.json-schema/maximum number?)
(s/def :swagger.json-schema/exclusiveMaximum boolean?)
(s/def :swagger.json-schema/minimum number?)
(s/def :swagger.json-schema/exclusiveMinimum boolean?)
(s/def :swagger.json-schema/maxLength nat-int?)
(s/def :swagger.json-schema/minLength nat-int?)
(s/def :swagger.json-schema/pattern string?)
(s/def :swagger.json-schema/maxItems nat-int?)
(s/def :swagger.json-schema/minItems nat-int?)
(s/def :swagger.json-schema/uniqueItems boolean?)
(s/def :swagger.json-schema/value (s/or :array coll?
                                        :boolean boolean?
                                        :integer integer?
                                        :number number?
                                        :null nil?
                                        :object map?
                                        :string string?))
(s/def :swagger.json-schema/enum (s/+ :swagger.json-schema/value))
(s/def :swagger.json-schema/multipleOf number?)
(s/def :swagger.json-schema/maxProperties nat-int?)
(s/def :swagger.json-schema/minProperties nat-int?)
(s/def :swagger.json-schema/required (s/+ string?))

;; Property

(defmulti property-base-properties :type)

(defmethod property-base-properties "string" [_]
  (s/keys :req-un [:swagger.property/type]))

(defmethod property-base-properties "number" [_]
  (s/keys :req-un [:swagger.property/type]))

(defmethod property-base-properties "integer" [_]
  (s/keys :req-un [:swagger.property/type]))

(defmethod property-base-properties "boolean" [_]
  (s/keys :req-un [:swagger.property/type]))

(defmethod property-base-properties "array" [_]
  (s/keys :req-un [:swagger.property/type
                   :swagger.property/items]
          :opt-un [:swagger.property/collectionFormat]))

(s/def :swagger.property/base-properties (s/multi-spec property-base-properties :type))

(s/def :swagger/property (s/merge :swagger.property/base-properties
                                  :swagger.property/json-schema-properties))

(s/def :swagger.property/json-schema-properties (s/keys :opt-un [:swagger.json-schema/description
                                                                 :swagger.json-schema/format
                                                                 :swagger.json-schema/default
                                                                 :swagger.json-schema/maximum
                                                                 :swagger.json-schema/exclusiveMaximum
                                                                 :swagger.json-schema/minimum
                                                                 :swagger.json-schema/exclusiveMinimum
                                                                 :swagger.json-schema/maxLength
                                                                 :swagger.json-schema/minLength
                                                                 :swagger.json-schema/pattern
                                                                 :swagger.json-schema/maxItems
                                                                 :swagger.json-schema/minItems
                                                                 :swagger.json-schema/uniqueItems
                                                                 :swagger.json-schema/enum
                                                                 :swagger.json-schema/multipleOf]))

(s/def :swagger.property/type #{"string" "number" "integer" "boolean" "array"})
(s/def :swagger.property/items :swagger/property)
(s/def :swagger.property/collectionFormat #{"csv" "ssv" "tsv" "pipes"})

;; Security Requirement Object

(s/def :swagger/security-requirement (s/map-of keyword? (s/* string?)))

;; Security Scheme Object

(defmulti security-scheme-api-key-properties :type)

(defmethod security-scheme-api-key-properties "basic" [_]
  (s/keys))

(defmethod security-scheme-api-key-properties "apiKey" [_]
  (s/keys :req-un [:swagger.security-scheme/name
                   :swagger.security-scheme/in]))

(defmethod security-scheme-api-key-properties "oauth2" [_]
  (s/keys))

(s/def :swagger.security-scheme/api-key-properties (s/multi-spec security-scheme-api-key-properties :type))

(defmulti security-scheme-oauth2-url-properties :flow)

(defmethod security-scheme-oauth2-url-properties "implicit" [_]
  (s/keys :req-un [:swagger.security-scheme/authorizationUrl]))

(defmethod security-scheme-oauth2-url-properties "password" [_]
  (s/keys :req-un [:swagger.security-scheme/tokenUrl]))

(defmethod security-scheme-oauth2-url-properties "application" [_]
  (s/keys :req-un [:swagger.security-scheme/tokenUrl]))

(defmethod security-scheme-oauth2-url-properties "accessCode" [_]
  (s/keys :req-un [:swagger.security-scheme/authorizationUrl
                   :swagger.security-scheme/tokenUrl]))

(s/def :swagger.security-scheme/oauth2-url-properties (s/multi-spec security-scheme-oauth2-url-properties :flow))

(defmulti security-scheme-oauth2-properties :type)

(defmethod security-scheme-oauth2-properties "basic" [_]
  (s/keys))

(defmethod security-scheme-oauth2-properties "apiKey" [_]
  (s/keys))

(defmethod security-scheme-oauth2-properties "oauth2" [_]
  (s/merge (s/keys :req-un [:swagger.security-scheme/flow
                            :swagger.security-scheme/scopes])
           :swagger.security-scheme/oauth2-url-properties))

(s/def :swagger.security-scheme/oauth2-properties (s/multi-spec security-scheme-oauth2-properties :type))

(s/def :swagger/security-scheme (s/merge (s/keys :req-un [:swagger.security-scheme/type]
                                                 :opt-un [:swagger.security-scheme/description])
                                         :swagger.security-scheme/api-key-properties
                                         :swagger.security-scheme/oauth2-properties))

(s/def :swagger.security-scheme/type #{"basic" "apiKey" "oauth2"})
(s/def :swagger.security-scheme/description string?)
(s/def :swagger.security-scheme/name string?)
(s/def :swagger.security-scheme/in #{"query" "header"})
(s/def :swagger.security-scheme/flow #{"implicit" "password" "application" "accessCode"})
(s/def :swagger.security-scheme/authorizationUrl string?)
(s/def :swagger.security-scheme/tokenUrl string?)
(s/def :swagger.security-scheme/scopes (s/map-of keyword? string?))

;; XML Object

(s/def :swagger/xml (s/keys :opt-un [:swagger.xml/name
                                     :swagger.xml/namespace
                                     :swagger.xml/prefix
                                     :swagger.xml/attribute
                                     :swagger.xml/wrapped]))

(s/def :swagger.xml/name string?)
(s/def :swagger.xml/namespace string?)
(s/def :swagger.xml/prefix string?)
(s/def :swagger.xml/attribute boolean?)
(s/def :swagger.xml/wrapped boolean?)

;; External Documentation Object

(s/def :swagger/external-documentation (s/keys :opt-un [:swagger.external-documentation/description
                                                        :swagger.external-documentation/url]))

(s/def :swagger.external-documentation/description string?)
(s/def :swagger.external-documentation/url string?)

;; Schema Object

(s/def :swagger/schema (s/keys :opt-un [:swagger/$ref
                                        :swagger.json-schema/type
                                        :swagger.json-schema/format
                                        :swagger.json-schema/title
                                        :swagger.json-schema/description
                                        :swagger.json-schema/default
                                        :swagger.json-schema/maximum
                                        :swagger.json-schema/exclusiveMaximum
                                        :swagger.json-schema/minimum
                                        :swagger.json-schema/exclusiveMinimum
                                        :swagger.json-schema/maxLength
                                        :swagger.json-schema/minLength
                                        :swagger.json-schema/pattern
                                        :swagger.json-schema/maxItems
                                        :swagger.json-schema/minItems
                                        :swagger.json-schema/uniqueItems
                                        :swagger.json-schema/enum
                                        :swagger.json-schema/multipleOf
                                        :swagger.json-schema/maxProperties
                                        :swagger.json-schema/minProperties
                                        :swagger.json-schema/required
                                        :swagger.schema/items
                                        :swagger.schema/allOf
                                        :swagger.schema/properties
                                        :swagger.schema/additionalProperties
                                        :swagger.schema/discriminator
                                        :swagger.schema/readOnly
                                        :swagger.schema/xml
                                        :swagger.schema/externalDocs
                                        :swagger.schema/example]))

(s/def :swagger.schema/items (s/or :schema :swagger/schema
                                   :schema-array (s/+ :swagger/schema)))
(s/def :swagger.schema/allOf (s/+ :swagger/schema))
(s/def :swagger.schema/properties (s/map-of keyword? :swagger/schema))
(s/def :swagger.schema/additionalProperties (s/or :boolean boolean?
                                                  :schema :swagger/schema))
(s/def :swagger.schema/discriminator string?)
(s/def :swagger.schema/readOnly boolean?)
(s/def :swagger.schema/xml :swagger/xml)
(s/def :swagger.schema/externalDocs :swagger/external-documentation)
(s/def :swagger.schema/example :swagger.json-schema/value)

;; Reference Object

(s/def :swagger/ref (s/keys :req-un [:swagger/$ref]))

;; Tag Object

(s/def :swagger/tag (s/keys :req-un [:swagger.tag/name]
                            :opt-un [:swagger.tag/description
                                     :swagger.tag/externalDocs]))

(s/def :swagger.tag/name string?)
(s/def :swagger.tag/description string?)
(s/def :swagger.tag/externalDocs :swagger/external-documentation)

;; Response Object

(s/def :swagger/response (s/keys :req-un [:swagger.response/description]
                                 :opt-un [:swagger.response/schema
                                          :swagger.response/headers
                                          :swagger.response/examples]))

(s/def :swagger.response/description string?)
(s/def :swagger.response/schema :swagger/schema)
(s/def :swagger.response/headers (s/map-of keyword? :swagger/property))
(s/def :swagger.response/examples (s/map-of (s/with-gen (s/conformer keyword->str) gen/string-alphanumeric)
                                            any?))

;; Responses Object

(s/def :swagger/responses (s/map-of (s/or :default #{:default}
                                          :status-code (s/with-gen
                                                         (s/and (s/conformer keyword->int) #(<= 100 % 599))
                                                         #(gen/choose 100 599)))
                                    (s/or :response :swagger/response
                                          :ref :swagger/ref)))

;; Parameter Object

(defmulti parameter :in)

(defmethod parameter "body" [_]
  (s/keys :req-un [:swagger.parameter/name
                   :swagger.parameter/in
                   :swagger.parameter/schema]
          :opt-un [:swagger.parameter/required
                   :swagger.json-schema/description]))

(defmethod parameter "path" [_]
  (s/merge (s/keys :req-un [:swagger.parameter/name
                            :swagger.parameter/in
                            :swagger.parameter/type
                            :swagger.parameter+path/required]
                   :opt-un [:swagger.parameter/allowEmptyValue
                            :swagger.parameter/collectionFormat])
           :swagger.parameter/items-property
           :swagger.parameter/json-schema-properties))

(defmethod parameter "formData" [_]
  (s/merge (s/keys :req-un [:swagger.parameter/name
                            :swagger.parameter/in
                            :swagger.parameter+form-data/type]
                   :opt-un [:swagger.parameter/required
                            :swagger.parameter/allowEmptyValue
                            :swagger.parameter+query+form-data/collectionFormat])
           :swagger.parameter/items-property
           :swagger.parameter/json-schema-properties))

(defmethod parameter "query" [_]
  (s/merge (s/keys :req-un [:swagger.parameter/name
                            :swagger.parameter/in
                            :swagger.parameter/type]
                   :opt-un [:swagger.parameter/required
                            :swagger.parameter/allowEmptyValue
                            :swagger.parameter+query+form-data/collectionFormat])
           :swagger.parameter/items-property
           :swagger.parameter/json-schema-properties))

(defmethod parameter "header" [_]
  (s/merge (s/keys :req-un [:swagger.parameter/name
                            :swagger.parameter/in
                            :swagger.parameter/type]
                   :opt-un [:swagger.parameter/required
                            :swagger.parameter/allowEmptyValue
                            :swagger.parameter/collectionFormat])
           :swagger.parameter/items-property
           :swagger.parameter/json-schema-properties))

(s/def :swagger/parameter (s/multi-spec parameter :in))

(defmulti parameters-item-property :type)

(defmethod parameters-item-property "string" [_]
  (s/keys))

(defmethod parameters-item-property "number" [_]
  (s/keys))

(defmethod parameters-item-property "integer" [_]
  (s/keys))

(defmethod parameters-item-property "boolean" [_]
  (s/keys))

(defmethod parameters-item-property "file" [_]
  (s/keys))

(defmethod parameters-item-property "array" [_]
  (s/keys :req-un [:swagger.parameter/items]))

(s/def :swagger.parameter/items-property (s/multi-spec parameters-item-property :type))

(s/def :swagger.parameter/json-schema-properties (s/keys :opt-un [:swagger.json-schema/description
                                                                  :swagger.json-schema/format
                                                                  :swagger.json-schema/default
                                                                  :swagger.json-schema/maximum
                                                                  :swagger.json-schema/exclusiveMaximum
                                                                  :swagger.json-schema/minimum
                                                                  :swagger.json-schema/exclusiveMinimum
                                                                  :swagger.json-schema/maxLength
                                                                  :swagger.json-schema/minLength
                                                                  :swagger.json-schema/pattern
                                                                  :swagger.json-schema/maxItems
                                                                  :swagger.json-schema/minItems
                                                                  :swagger.json-schema/uniqueItems
                                                                  :swagger.json-schema/enum
                                                                  :swagger.json-schema/multipleOf]))

(s/def :swagger.parameter/name string?)
(s/def :swagger.parameter/in #{"query" "header" "path" "formData" "body"})
(s/def :swagger.parameter/required boolean?)
(s/def :swagger.parameter+path/required #{true})
(s/def :swagger.parameter/schema :swagger/schema)
(s/def :swagger.parameter/type #{"string" "number" "integer" "boolean" "array"})
(s/def :swagger.parameter+form-data/type #{"string" "number" "integer" "boolean" "array" "file"})
(s/def :swagger.parameter/allowEmptyValue boolean?)
(s/def :swagger.parameter/items :swagger/property)
(s/def :swagger.parameter/collectionFormat #{"csv" "ssv" "tsv" "pipes"})
(s/def :swagger.parameter+query+form-data/collectionFormat #{"csv" "ssv" "tsv" "pipes" "multi"})

;; Operation Object

(s/def :swagger/operation (s/keys :req-un [:swagger.operation/responses]
                                  :opt-un [:swagger.operation/tags
                                           :swagger.operation/summary
                                           :swagger.operation/description
                                           :swagger.operation/externalDocs
                                           :swagger.operation/operationId
                                           :swagger.operation/consumes
                                           :swagger.operation/produces
                                           :swagger.operation/parameters
                                           :swagger.operation/schemes
                                           :swagger.operation/deprecated
                                           :swagger.operation/security]))


(s/def :swagger.operation/responses :swagger/responses)
(s/def :swagger.operation/tags (s/* string?))
(s/def :swagger.operation/summary string?)
(s/def :swagger.operation/description string?)
(s/def :swagger.operation/externalDocs :swagger/external-documentation)
(s/def :swagger.operation/operationId string?)
(s/def :swagger.operation/consumes (s/* string?))
(s/def :swagger.operation/produces (s/* string?))
(s/def :swagger.operation/parameters (s/* (s/or :ref :swagger/ref
                                                :parameter :swagger/parameter)))
(s/def :swagger.operation/schemes :swagger/schemes)
(s/def :swagger.operation/deprecated boolean?)
(s/def :swagger.operation/security (s/+ :swagger/security-requirement))

;; Path Item Object

(s/def :swagger/path-item (s/keys :opt-un [:swagger/$ref
                                           :swagger.path-item/get
                                           :swagger.path-item/put
                                           :swagger.path-item/post
                                           :swagger.path-item/delete
                                           :swagger.path-item/options
                                           :swagger.path-item/head
                                           :swagger.path-item/patch
                                           :swagger.path-item/parameters]))

(s/def :swagger.path-item/get :swagger/operation)
(s/def :swagger.path-item/put :swagger/operation)
(s/def :swagger.path-item/post :swagger/operation)
(s/def :swagger.path-item/delete :swagger/operation)
(s/def :swagger.path-item/options :swagger/operation)
(s/def :swagger.path-item/head :swagger/operation)
(s/def :swagger.path-item/patch :swagger/operation)
(s/def :swagger.path-item/parameters (s/* (s/or :ref :swagger/ref
                                                :parameter :swagger/parameter)))

;; Paths Object

(s/def :swagger.paths/path (s/with-gen (s/and (s/conformer keyword->str)
                                              :swagger/path)
                                       #(gen/fmap (partial str "/") (gen/string-alphanumeric))))

(s/def :swagger/paths (s/every (s/or :path (s/tuple :swagger.paths/path
                                                    :swagger/path-item)
                                     :extension (s/tuple :swagger/extension any?))
                               :kind map?))

;; License Object

(s/def :swagger.info/license (s/keys :opt-un [:swagger.info.license/name
                                              :swagger.info.license/url]))

(s/def :swagger.info.license/name string?)
(s/def :swagger.info.license/url string?)

;; Contact Object

(s/def :swagger.info/contact (s/keys :opt-un [:swagger.info.contact/name
                                              :swagger.info.contact/url
                                              :swagger.info.contact/email]))

(s/def :swagger.info.contact/name string?)
(s/def :swagger.info.contact/url string?)
(s/def :swagger.info.contact/email string?)

;; Info Object

(s/def :swagger/info (s/keys :req-un [:swagger.info/title
                                      :swagger.info/version]
                             :opt-un [:swagger.info/description
                                      :swagger.info/termsOfService
                                      :swagger.info/contact
                                      :swagger.info/license]))

(s/def :swagger.info/title string?)
(s/def :swagger.info/description string?)
(s/def :swagger.info/termsOfService string?)
(s/def :swagger.info/version string?)

;; Swagger Object

(s/def :swagger/definition (s/keys :req-un [:swagger.definition/swagger
                                            :swagger.definition/info
                                            :swagger.definition/paths]
                                   :opt-un [:swagger.definition/host
                                            :swagger.definition/basePath
                                            :swagger.definition/schemes
                                            :swagger.definition/consumes
                                            :swagger.definition/produces
                                            :swagger.definition/definitions
                                            :swagger.definition/parameters
                                            :swagger.definition/responses
                                            :swagger.definition/securityDefinitions
                                            :swagger.definition/security
                                            :swagger.definition/tags
                                            :swagger.definition/externalDocs]))

(s/def :swagger.definition/swagger #{"2.0"})
(s/def :swagger.definition/info :swagger/info)
(s/def :swagger.definition/host string?)
(s/def :swagger.definition/basePath :swagger/path)
(s/def :swagger.definition/schemes :swagger/schemes)
(s/def :swagger.definition/consumes (s/* string?))
(s/def :swagger.definition/produces (s/* string?))
(s/def :swagger.definition/paths :swagger/paths)
(s/def :swagger.definition/definitions (s/map-of keyword? :swagger/schema))
(s/def :swagger.definition/parameters (s/map-of keyword? :swagger/parameter))
(s/def :swagger.definition/responses (s/map-of keyword? :swagger/response))
(s/def :swagger.definition/securityDefinitions (s/map-of keyword? :swagger/security-scheme))
(s/def :swagger.definition/security :swagger/security-requirement)
(s/def :swagger.definition/tags (s/* :swagger/tag))
(s/def :swagger.definition/externalDocs :swagger/external-documentation)
