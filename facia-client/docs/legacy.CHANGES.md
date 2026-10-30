# Latest information in GitHub Releases

Check out **https://github.com/guardian/facia-scala-client/releases**
for info on all releases from v4.0.0 onwards. For historical reference
purposes, information on prior releases is included below.

### Releases prior to v4.0.0...


#### 3.3.6
  
  - Bump CAPI client to `v17.21`
  - Push special report logic into CAPI client

#### 3.3.5

  - Bump CAPI client to `v17.20`

#### 3.2.2

  - Bump CAPI client to `v17.11`
  - Implement the `Format` type for `CuratedContent`, `SupportingCuratedContent` and `LatestSnap`.

#### 3.0.19

  - Adds a `blockId` property to trail meta. Intent: allow liveblog block IDs to be stored by the breaking news tool (`facia-tool`).

#### 3.0.5

  - Removes Scala 2.11 and Play 2.5

#### 3.0.4

  - Populates displayName from collectionConfig rather than collectionJson

#### 3.0.0

  - Upgrades all CAPI dependencies using the newest version of the Scrooge/Thrift library

#### 2.6.8

  - CAPI client update: brings it up to date with other libraries to avoid inconsistencies and fix a leaking dependency version mismatch

#### 2.6.7

  - Bug fix: Compare tag `type` field with `TagType`s rather than `String`s

#### 2.6.6 

  - adds support for userVisibiity value

#### 2.6
  
  - Upgrade Content API dependency to `12.0`

#### 2.4
  
  - Upgrade Content API dependency to `11.33`
  - Remove deprecated `http` package
#### 2.3
  
  - Correctly parse [timezone offset](https://github.com/guardian/facia-scala-client/pull/196) 


#### 2.2
  
  - Upgrade AWS dependency and use the new AmazonS3 API.

#### 2.1.3
  
  - Improvement: recover from failing capi lookup for link snaps

#### 2.1.2
  
  - Improvement: filter snap links pointing at tag-combiner pages to avoid unnecessary capi lookups

#### 2.1.1
  
  - Bug fix: filter snap links to avoid unnecessary capi lookups (which can bring the frontend facia press down)

#### 2.1.0
  
  - Enable branding of `FaciaContent`.  
  __Breaking change:__
      - This release has changes that require the addition of a field to implementations of `FaciaContent`.

##### 2.0.14
  
  - Label paid content as `paid` instead of `news`

##### 2.0.13
  
  - Update capi client to v11.12

##### 2.0.12

  - Add the special report tone to the tag the-new-arrivals.

##### 2.0.11

  - Adds `displayHint:maxItemsToDisplay` to `CollectionConfigJson` and `CollectionConfig`

##### 2.0.10

  - adds `displayHint:maxItemsToDisplay` to `CollectionConfigJson`

##### 2.0.9

  - adds `wordCount` to `FaciaContentUtils`

##### 2.0.8

  - adds `EmailPriority` to `FrontPriority`

##### 2.0.7

  - set `showMainVideo` default to `true` for all video content type

##### 2.0.6

  - set `showMainVideo` to `false` for Video Atoms

##### 2.0.5

  - Update capi client to v10.17

#### 2.0.0

  - Revert `ImageReplace` changes introduced to `FaciaImage`.
  - Update capi client to v8.12
  - Update aws to v1.11.7

#### 1.6.2

  - Add `UnknownMetadata` to `Metadata` ADT to handle cases of new future types in old libraries
  - Add logging via `SLF4J` and `scala-logging`

#### 1.6.1

  - Expose branding metadata in CollectionConfig.

#### 1.6.0

  - Support the marking of collections to indicate that they should have commercial branding.

#### 1.5.3

  - Add sbt project that compiles against play-json 2.5.x

#### 1.5.2

  - Add homan-square tag to special report card style

#### 1.5.1

  - Changed FAPI object methods with implicit content api parameter to use ContentApiClientLogic instead of GuardianContentClient

#### 1.5.0

  - Update capi client to v.8.2
  - Added ImageReplace to FaciaImage

#### 1.3.0

  - Use hashed strings to check for special report card style

#### 1.2.0

  - Returns an empty list of backfill collections if collection backfill id is invalid
  - Return an empty backfill if backfill type is invalid
  - Returns the api query of a parent backfill collections

#### 1.1.0

  - Add metadata to collections

#### 1.0.0

  __Non backward compatible release__

  - Stop cross compiling, the client is now only available on scala 2.11
  - Remove `backfill` deprecated in 0.67
  - Update to play 2.4.6
  - Update aws to v1.10.58
  - Update capi client to v.7.29

#### 0.70

  - Update aws to v1.10.49
  - Update capi client to v.7.22

#### 0.69

  - Update aws to v1.10.47
  - Update capi client to v.7.16

#### 0.68

  - Change the signature of `FAPI.backfillFromConfig` from ~~`FAPI.backfillFromConfig(collection: Collection)`~~ to `FAPI.backfillFromConfig(collectionConfig: CollectionConfig)`

#### 0.67

  - Add `backfill` in collectiong config. This is meant to replace `apiQuery` in future releases.
  - The method `FAPI.backfill(apiQuery: String, collection: Collection)` has been deprecated, `FAPI.backfillFromConfig(collection: Collection)` should be used instead as it automatically handles the backfill query and returns an empty list in case no backfill is set.


#### 0.66

  - Invalid release, don't use

#### 0.65

  - Add This Is The NHS Tag to Special Report

#### 0.64

  - Upgrade capi client to v7.13

#### 0.63

  - Upgrade capi client to v7.12

#### 0.62

  - Upgrade capi client to v7.11

#### 0.61

  - Upgrade capi client to v7.3

#### 0.60

  - Fix the batching so that it does not request more than 50 at a time in a search query. There is a restriction in CAPI of 50 items in a search query.

#### 0.59

  - Adds a publishedBy to Trail

#### 0.58

  - Adds Collections.hideShowMore

#### 0.57

  - Adds Front.group meta
  - Adds Collections.hideShowMore

#### 0.56

  - Adds a convenience function: ResolvedMetaData.toMap
  - Changes ResolvedMetaData.fromContent to a non-private methods
  - Adds CardStyle.fromContent convenience method for CardStyle

#### 0.55

  - Shows the breaking news kicker even when tonal kickers are supressed

#### 0.54

  - Changes queries to not add editorsPicks to the head of the list of backfill by default


#### 0.53

  - Adds showLivePlayable content meta


#### 0.52

  - Adds "australia-news/series/healthcare-in-detention" tag to resolve to SpecialReport style
