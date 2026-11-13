Facia Scala Client [![fapi-client-play30 Scala version support](https://index.scala-lang.org/guardian/facia-scala-client/fapi-client-play30/latest-by-scala-version.svg)](https://index.scala-lang.org/guardian/facia-scala-client/fapi-client-play30) [![Release](https://github.com/guardian/facia-scala-client/actions/workflows/release.yml/badge.svg)](https://github.com/guardian/facia-scala-client/actions/workflows/release.yml)
==================

Facia's Scala client is split into two parts.

## The Fronts API client

The `fapi-client` project contains the main Fronts API client. This provides ways to fetch Fronts
data and includes high-level logic to convert Facia's low-level internal data structures into more
easily-used types.

### Adding the dependency to SBT

[![fapi-client-play30 Scala version support](https://index.scala-lang.org/guardian/facia-scala-client/fapi-client-play30/latest-by-scala-version.svg)](https://index.scala-lang.org/guardian/facia-scala-client/fapi-client-play30)

[![fapi-client-play29 Scala version support](https://index.scala-lang.org/guardian/facia-scala-client/fapi-client-play29/latest-by-scala-version.svg)](https://index.scala-lang.org/guardian/facia-scala-client/fapi-client-play29)

[![fapi-client-play28 Scala version support](https://index.scala-lang.org/guardian/facia-scala-client/fapi-client-play28/latest-by-scala-version.svg)](https://index.scala-lang.org/guardian/facia-scala-client/fapi-client-play28)

[![fapi-client-play27 Scala version support](https://index.scala-lang.org/guardian/facia-scala-client/fapi-client-play27/latest-by-scala-version.svg)](https://index.scala-lang.org/guardian/facia-scala-client/fapi-client-play27)

Add the following line to your [SBT build file](https://www.scala-sbt.org/1.0/docs/Basic-Def.html):

    libraryDependencies += "com.gu" %% "fapi-client-play30" % "x.y.z"

### Using the library

To use the Fronts API client you will need instances of:

* the content API client
* the underlying Facia JSON library

These instances will need to be configured with your access/API keys. The `FAPI` class contains
the public interface to the behaviour in the library. Examples of how to use the client can be
found in the `demo.sc` file, an IntelliJ worksheet.

## Low-level client for Facia JSON API.

The low-level Facia API client is designed to fetch and manipulate Facia's internal JSON structures.
This library provides underlying behaviour for the main Fronts API client.

### Adding the dependency to SBT

[![facia-json-play30 Scala version support](https://index.scala-lang.org/guardian/facia-scala-client/facia-json-play30/latest-by-scala-version.svg)](https://index.scala-lang.org/guardian/facia-scala-client/facia-json-play30)

[![facia-json-play29 Scala version support](https://index.scala-lang.org/guardian/facia-scala-client/facia-json-play29/latest-by-scala-version.svg)](https://index.scala-lang.org/guardian/facia-scala-client/facia-json-play29)

[![facia-json-play28 Scala version support](https://index.scala-lang.org/guardian/facia-scala-client/facia-json-play28/latest-by-scala-version.svg)](https://index.scala-lang.org/guardian/facia-scala-client/facia-json-play28)

[![facia-json-play27 Scala version support](https://index.scala-lang.org/guardian/facia-scala-client/facia-json-play27/latest-by-scala-version.svg)](https://index.scala-lang.org/guardian/facia-scala-client/facia-json-play27)

Add the following line to your [SBT build file](https://www.scala-sbt.org/1.0/docs/Basic-Def.html):

    libraryDependencies += "com.gu" %% "facia-json-play26" % "3.3.3"

### Making calls to the API

##### Config

```scala
ApiClient.config
```

Returns the current config.

##### Collection

```scala
ApiClient.collection("id goes here")
```

Returns the collection with the given ID.

## Running the tests

To run the tests you need to pass a content api key and facia client target url to the library via your environment.

    export CONTENT_API_KEY="<api-key>"
    export FACIA_CLIENT_TARGET_URL="<target-url>"
    sbt test

## Before releasing a new version

Before releasing, consider whether your changes are likely to be backwards-compatible with older versions of the client -- for example, if the new version introduces new serialisations that old versions won't be able to read. Where possible, we should aim to maintain backwards compatibility.

At the time of writing, Tools, CAPI, Dotcom, Mobile teams (Mapi) and Ophan all use `facia-scala-client` -- take care to understand who is using the library and communicate changes where appropriate. In particular remember the Apple News feed uses this client, we've forgotten that twice before.

## Building a release

This project uses the [`gha-scala-library-release-workflow`](https://github.com/guardian/gha-scala-library-release-workflow)
to release to Maven Central. To release a new version, execute the
[Release](https://github.com/guardian/facia-scala-client/actions/workflows/release.yml)
workflow in the Actions tab on GitHub:

![RunReleaseWorkflow](https://github.com/guardian/facia-scala-client/assets/52038/23920a58-80c6-4e6d-b5bc-6f58bf78f41d)

_You'll need to refresh the page to see the new workflow run._

https://github.com/guardian/facia-scala-client/assets/52038/dfc014d9-98f9-4d20-8977-0a20340083d1
