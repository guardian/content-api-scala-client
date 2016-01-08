## 7.13
* Add `embed` to the list of asset types

## 7.12
* Tweak model and fix deserialisation for crossword `separatorLocations` field

## 7.11
* Added `crossword` content type

## 7.10
* Parse "keyEvent" and "summary" boolean properties in block attributes

## 7.9
* Added `clean` field to AssetFields
* Added `sensitive` 

## 7.8
* Added `explicit` field to AssetFields
* Added tests for audio elements
* Added `durationMinutes`, `durationSeconds`, `explicit` and `clean` field to AudioElementField

## 7.7
* Added the `embedType` and `html` fields to asset fields

## 7.6
* Added the image field to Podcast metadata

## 7.5
* Added thumbnailUrl, role, mediaId, iframeUrl, scriptName, scriptUrl, blockAds to AssetFields.
* Added allowUgc to ContentFields.

## 7.4
* Added AUDIO type to AssetType.

## 7.3
* Added displayCredits field to AssetFields.
* Added MaxSearchQueryIdSize to Limits.
* Make userAgent protected.

## 7.2
* Additional fields added to block elements.
* 'durationMinutes' and 'durationSeconds' fields added to AssetFields.
* 'legallySensitive' field added to ContentFields.
* Travis file added.
* Remove incorrectly added isMaster field.

## 7.1
* Add thrift definition to built jar file.

## 7.0
* Define and generate the Content API data model via Scrooge using Thrift. 

## 6.10
* Provide proper parsing of error responses.
* Add support for the `type` field on `Content`, including a filter to search by content type.

## 6.9
* Add an `isMaster` field to the ImageTypeData model.
* Add a workaround for a bug in Dispatch that can cause a resource leak.

## 6.8
* Include the client's version in the user-agent header sent with requests.
* Make it easier to inject a custom implementation for the underlying HTTP client.

## 6.7
* Add support for querying what content has been removed from the Content API.
* Bump Scala to 2.11.7

## 6.6
* Add crosswords.

## 6.5
* Add audio and pull quote type data for block level elements.
* Add additional field (credit) to type data of image block elements.

## 6.4
* Update to dispatch v0.11.3, to avoid clashes when using Play v2.4.0 - see [#77](https://github.com/guardian/content-api-scala-client/pull/77)


## 6.3
* Add type data for image block elements.
* Add additional field (html) to type data of video block element.

## 6.2
* Add type data for video, text and tweet block elements.

## 6.1
* Add editions 
* Add block elements and date fields

## 6.0
* Add rights in response 
* Remove collections

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
