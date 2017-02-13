## 11.2
* Upgrade content-api-models dependency to 11.2 (upgrades circe to 0.7.0)

## 11.1
* Upgrade content api models dependency to 11.1 to provide images on reviews model.

## 11.0
* Upgrade content-api-models dependency to 11 (fezziwig dependency)

## 10.24
* Upgrade content-api-models dependency to include sourceArticleId field in recipe and review atom types.

## 10.22
* Upgrade content-api-models dependency (refactored circe macros)

## 10.21
* Upgraded Circe to 0.6.1.

## 10.20
* Recipe atom - adding `quantityRange` for ingredient and `images`  (content-api-model 10.20)

## 10.19
* Recipe atom - adding optional `unit` for `serves` (content-api-model 10.19)

## 10.18
* thrift union macros (content-api-model 10.18)

## 10.17
* `genre` field is now a list (content-api-model 10.17)

## 10.16
* Support film review atoms (content-api-model 10.16)

## 10.15
* Add `filename` query parameter.

## 10.4
* Add high contrast sponsor logo

## 10.3
* Add interactive atom 

## 10.2
* Add bodyText field

## 10.1
* Add sponsor logo dimensions to Sponsorship model.

## 10.0
* Remove JSON support. The client now receives only Thrift-encoded responses from the Content API.
* Breaking change: Removed the optional `useThrift` parameter from the `GuardianContentClient` constructor.

## 9.5
* Bump the content-api-models adding extra properties of media atoms: mime-type, duration and poster.

## 9.4
* Bump the content-api-models dependency adding campaignColour and isMandatory

## 9.3
* add `internalShortId`
* add missing circe decoders

## 9.0
* Bump the content-api-models dependency to a new major version.

## 8.12
* Bump the content-api-models dependency

## 8.11
* Add a new show-section parameter
* Bump the content-api-models dependency

## 8.10
* Bump the content-api-models dependency

## 8.9
* Add `thumbnailImageUrl` to fields.
* Improve JSON and Thrift deserialisation
* Add podcasts categories

## 8.8
* Bump the content-api-models dependency

## 8.7
* Bump the content-api-models dependency
* Downgrade libthrift from 0.9.3 to 0.9.1

## 8.6
* Use content-api-models-json for JSON parsing

## 8.5
* Revert the temporary fix for handling error responses when using Thrift
* Update the models to add new optional fields to the `Blocks` model

## 8.4
* Fix the error responses when using Thrift

## 8.3
* Add Video Stats query type

## 8.2
* Update Scrooge from 3.16.3 to 4.6.0
* Split the models into a separate library.

## 8.1 
* Add `contains-element` filter. e.g. contains-element=video
* Add `commentable` filter.
* Thrift now supported by the Content API.

## 8.0
* Add optional support (client-side) for processing thrift responses, together with json.
  However, thrift is NOT supported at the moment by Content Api; an upgrade of the Scala client will follow.
* The following fields for item responses are now Scala Options: `results`, `relatedContent`,
  `editorsPicks`, `mostViewed`, and `leadContent`.

## 7.30
* Add show-stats parameter.

## 7.29
* Add star-rating and membership-access filters

## 7.28
* Fix the `package` field on the content response. It is actually called `packages` and is a list.

## 7.27
* Update content-atom version to 0.2.6. This adds `id` and `bucket` fields to the `quiz` model.
* Update story-package version to 1.0.2. This adds the `packageName` field to the `package` model.
* Add the `tweet` asset type.

## 7.26
* Add tracking tag type

## 7.25
* Add embed elements support

## 7.24
* Add viewpoints support
* Change quiz (single object) to quizzes (list of objects)

## 7.23
* Add show-atoms filter
* Add quiz support

## 7.22
* Add show-packages filter

## 7.19
* Add serializer for story package group
* Add lang filter

## 7.18
* Add "audio" content type
* Add "lang" field to content
* Add "pinned" block attribute

## 7.17
* Add helper method to JsonParser

## 7.16
* Add models for new story packages implementation

## 7.15
* Split the model classes into a separate artifact called `content-api-models`. They are versioned in sync with the Scala client.

## 7.14

DO NOT USE. This was a botched release. It was only released for Scala 2.11.x

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
