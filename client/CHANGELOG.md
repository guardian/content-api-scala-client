## 17.6

* Bump CAPI model to 15.9.7 (adds additional fields to RetrievableEvent)

## 17.5

* Bump CAPI model to 15.9.6 (adds block element role fields)

## 17.2

* Bump CAPI models to 15.9.2 (adds optional `placeholderUrl` field to interactive atoms)

## 17.1

* Removed 503 from list of retryable response codes

## 17.0

* Removed `backoffStrategy` from `ContentApiClient`. Introduced `RetryableContentApiClient` to mix-in a retry backoff strategy. More in README.

## 16.0

* Add a `Request-Attempt` header that is set to zero for any initial request and incremented by one for each retry - so that we can see the behaviour of the backoff code in Kibana

## 15.8

* Bump CAPI models to 15.6 (removed content no longer supported)

## 15.7

* Add AdvertisementFeature design type

## 15.6

* Require clients to implement a ContentApiBackoff retry strategy along with an implicitly declared ScheduledExecutor. See the README for more info.

## 15.5

* Upgrade model to v15.5 (adds acast ID to podcast metadata)

## 15.4

* Upgrade model to v15.3 (removes storyquestions)

## 15.1–15.3

Broken tag releases

## 15.0

* Remove stories-related APIs
* Upgrade model to v15.0

## 14.2

* Version 14.1 of the models dependency was empty

## 14.0 (13.0 is skipped)

* Upgrades to Scrooge 19.3, which uses libthrift 0.10

## 12.15
* Add support for `use-date` parameter in atoms queries

## 12.14
* Bump version of content-api-models to 12.14 (adds support for the audio atom).

## 12.10
* Bump version of content-api-models to 12.10 (fixes namespacing of the new chart atom).

## 12.9
* Fix a bug in the new atom usage API where the URL contained uppercase characters

## 12.8
* [#277](https://github.com/guardian/content-api-scala-client/pull/277) Add support for atom usage queries
* bump content-api-models to 12.8 (adds `ChartAtom`)

## 12.7
* bump content-api-models to 12.7 (adds `googlePodcastsUrl` and `spotifyUrl` to the Tag Podcast model)

## 12.6
* [#272](https://github.com/guardian/content-api-scala-client/pull/272) Adds ids parameter to RemovedContentQuery

## 12.5
* bump content-api-models to 12.5 (adds `resultsWithLastModified` to `RemovedContentResponse`)

## 12.2

### New features

* `paginate(query)(f)` unfolds a query until there are no more page results to process. `f` is a pure function processing a CAPI response and `paginate` returns a list of processed responses (wrappied in a `Future`)
* `paginateAccum(query)(f, g)` folds over the results and accumulates into a final result. `f` transforms a response into an accumulated result, `g` [multiplies](https://en.wikipedia.org/wiki/Semigroup) two results together
* `paginateFold(query)(f, m)` folds over the results by accumulating a final result. `f` takes two parameters: a response and the accumulated result so far.

## 12.1
* Update content-api-models to 12.1 (new `campaign` tag type)
* Allow OkHttp client to be overridden in the default client class

## 12.0

### Removed the dependency on OkHttp

The content-api-client project now lacks a concrete implementation of the HTTP communication. That is the first step we take in cleaning up the client and making it more amenable to purely functional settings. As a result, the `ContentApiClient` trait is provided and users are required to provide a concrete implementation based on their preferred HTTP client library. The following method must be implemented:

```scala
def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse]
```

For convenience, the previous default implementation is provided in a separate project, content-api-client-default. Versioning of that package will follow the one of content-api-client and thus will start at version 12.

### Utility functions moved in companion

The `item`, `search`, `tags`, `sections`, `editions`, `removedContent`, `atoms`, `recipes`, `reviews`, `gameReviews`, `restaurantReviews`, `filmReviews`, `videoStats` and `stories` methods have been moved into a separate trait, `ContentApiQueries`, from which the companion object `ContentApiClient` inherits.

The previous behaviour can be replicated very simply:

```scala
val client = new GuardianContentClient(...) with ContentApiQueries
```

## 11.54
* Update content-api-models to 11.51 (to get `showAffiliateLinks` field)

## 11.53
* Support `SearchQueryBase` in `GuardianContentClient#getResponse`.

## 11.52
* Extract a `SearchQueryBase` as the parent of `SearchQuery`.
  This helps with customising search queries (e.g. using a different path or more parameters).
* Upgrade patch versions for Scala (2.12.4 and 2.11.12) and okhttp (3.9.1).
* Set the target JVM to 1.8.
* Fix regression of read and connect timeouts being 1000 and 2000 instead of 1 and 2 seconds

## 11.51
* Encode query param spaces as %20

## 11.50
* Use cats 1.0 (via content-api-models 11.50)

## 11.49
* Update designType algorithm to prioritise immersive over everything else

## 11.48
* Update content-api-models to 11.48 (new atom image asset fields and internalCommissionedWordcount)

## 11.46
* Update content-api-models to 11.46 (commonsdivision in response)

## 11.45
* Update content-api-models to 11.45 (commonsdivision atom)
* Support new `DesignType`s

## 11.43 
* Update content-api-models to 11.43 (new embargo field on atoms)
* Adds an implicit designType field to Content model

## 11.42
* Update content-api-models to 11.42 (timeline atom flexible date formats)

## 11.40
* Update content-api-models to 11.40 (atom image credit field)

## 11.39
* Use https
* Update content-api-models to 11.39 (scheduledLaunch field in atoms)

## 11.38
Update content-api-models to 11.38 (add Pillars models)

## 11.37
Add sponsorship-type filter to tags endpoint.

## 11.36
Update content-api-models to 11.36 (add podcast type information)

## 11.35
* Update content-api-models to 11.35 (add sponsorship information to rich link fields)

## 11.33
* Move to using OkHTTP over Dispatch

## 11.32
* Update content-api-models to 11.32
* Replace Joda data types with their Java 8 equivalent

## 11.30
* Update content-api-models to 11.30 (add `description` to Timeline atoms for context)

## 11.29
* Update content-api-models to 11.29 (add `commissioningDesks` to atoms and `answers` to readers questions)

## 11.26
* Update content-api-models to 11.28 (add validTo and validFrom dates to Sponsorship, present in both tags and sections.)
* Add supported `sponsorship-type` filter for Section.

## 11.25
* Update content-api-models to 11.25 (add Atom updates to the crier event model)

## 11.24
* Update content-api-models to 11.24 (scala 2.12.3)
* This is the first version available for both scala `2.12.x` and scala `2.11.x`

## 11.23
* Update content-api-models to 11.23 (add closeDate field to story questions atom)

## 11.22
* Update content-api-models to 11.22 (add originating system in debug fields)

## 11.21
* Update content-api-models to 11.21 (add email provider in story questions and update fezziwig)
* Add StoriesQuery

## 11.19
* Update content-api-models to 11.19 (rename qanda fields)

## 11.17
* Update content-api-models to 11.17 (add shouldHideReaderRevenue field)

## 11.15
* Update content-api-models to 11.16 (add Snippet models)

## 11.14
* Update content-api-models to 11.14 (add StoriesResponse)

## 11.12
* Update content-api-models to 11.12 (add EntitiesResponse model)

## 11.10
* Update content-api-models to 11.9 (add block membership placeholder attribute)

## 11.9
* Update content-api-models to 11.8 (organisation and place entity models)

## 11.7
* Include show-stats parameter on search query.

## 11.6
* Update content-api-models to 11.6 (adds categories + entityIds fields to Tag model)

## 11.5
* Add storyquestions field to item response.

## 11.4
* Provide support for storyquestions atoms.

## 11.3
* Provide ability to query recipe, review and atom endpoints for internal clients.

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
