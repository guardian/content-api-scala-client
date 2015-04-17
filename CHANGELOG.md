## 5.3
* Add content blocks 
* Fix request headers not being sent correctly


## 5.2
* Respect DNS short TTL by setting the connection lifetime maximum to 60 seconds.

## 5.1
* Add email address field to tag

## 5.0
* Do not allow partial item or collection queries that throw exceptions
* Require ExecutionContext when running queries, not when constructing the client
* Add decent toString method to queries

## 4.1
* Expose generated URL to clients
* Add a parameter for the maximum url size that Content API will accept

## 4.0
* Large refactor to logically separate queries from the client itself. This makes queries themselves more reusable as
  they're now just datatypes.

## 3.7
* Add twitter handle field to tag (only available for contributors tag)

## 3.6
* Add new kicker fields on collections

## 3.5
* Add back extended filtering on collections

## 3.4
* Fix bug with filtering tags on tag pages

## 3.3
* Allow http client to be overridden

## 3.2
* Add back trait to ease extending

## 3.1
* Use Json4s-ext for date parsing
* Add productionOffice filter for content search
* Add first name and last name to tag (only available for contributors tag)

## 3.0
* Only provide an asynchronous future-based interface
* Move to the `com.gu.contentapi.client` package
* Remove various features no longer present in the API
* Using an API key is no longer optional
* Rename main client object to `GuardianContentClient`
* Start a changelog
* Other internal changes (eg. updating most of the tests)


*(The history of previous versions has been lost to time.)*
