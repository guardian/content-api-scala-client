Content API Scala Client
========================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.gu/content-api-client_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.gu/content-api-client_2.11)

A Scala client for the Guardian's [Content API](http://explorer.capi.gutools.co.uk/).


## Setup

Add the following line to your SBT build definition, and set the version number to be the latest from the [releases page](https://github.com/guardian/content-api-scala-client/releases):

```scala
libraryDependencies += "com.gu" %% "content-api-client-default" % "x.y"
```

Please note, as of version 7.0, the content api scala client no longer supports java 7.

If you don't have an API key, go to [open-platform.theguardian.com/access/](http://open-platform.theguardian.com/access/) to get one. You will then need to create a new instance of the client and set the key:

```scala
val client = new GuardianContentClient("your-api-key")
```

### Setup with custom Http layer

As of version 12.0, the core module does not provide an http implementation. This is to accomodate use cases where people want to use their existing infrastructure, rather than relying on an extra dependency on OkHttp (the client used in the default module above). First, add the following line to your SBT definition:

```scala
libraryDependencies += "com.gu" %% "content-api-client" % "x.y"
```

Then, create your own client by extending the `ContentApiClient` trait and implementing the `get` method, e.g. using Play's ScalaWS client library

Note that as of version 15.1 an instance of `ContentApiClient` now requires a `Backoff` declaration. This allows for some classes of HTTP errors to be automatically retried. The most basic implementation is shown below and is explained in more detail later in this readme.

```scala
import play.api.libs.ws.WSClient

class ContentApiClient(ws: WSClient) extends ContentApiClient
  // new in version 15.1: Supply an automatic backoff and retry declaration
  override val backoffStrategy: Backoff = Backoff()
 
  def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse] =
    ws.url(url).withHttpHeaders(headers: _*).get.map(r => HttpResponse(r.bodyAsBytes, r.status, r.statusText))
}
```

## Usage

There are then four different types of query that can be performed: for a single item, or to filter through content, tags, or sections. You make a request of the Content API by creating a query and then using the client to get a response, which will come back in a `Future`.

Use these imports for the following code samples (substituting your own execution context for real code):

```scala
import com.gu.contentapi.client.GuardianContentClient
import scala.concurrent.ExecutionContext.Implicits.global
```

### Single item

Every item on http://www.theguardian.com/ can be retrieved on the same path at https://content.guardianapis.com/. They can be either content items, tags, or sections. For example:

```scala
// query for a single content item and print its web title
val itemQuery = ContentApiLogic.item("commentisfree/2013/jan/16/vegans-stomach-unpalatable-truth-quinoa")
client.getResponse(itemQuery).foreach { itemResponse =>
  println(itemResponse.content.get.webTitle)
}

// print web title for a tag
val tagQuery = ContentApiLogic.item("music/metal")
client.getResponse(tagQuery).foreach { tagResponse =>
  println(tagResponse.tag.get.webTitle)
}

// print web title for a section
val sectionQuery = ContentApiLogic.item("environment")
client.getResponse(sectionQuery).foreach { sectionResponse =>
  println(sectionResponse.section.get.webTitle)
}
```

Individual content items contain information not available from the `/search` endpoint described below. For example:

```scala
// print the body of a given content item
val itemBodyQuery = ContentApiLogic.item("politics/2014/sep/15/putin-bad-as-stalin-former-defence-secretary")
  .showFields("body")
client.getResponse(itemBodyQuery) map { response =>
  for (fields <- response.content.get.fields) println(fields.body)
}

// print the web title of every tag a content item has
val itemWebTitleQuery = ContentApiLogic.item("environment/2014/sep/14/invest-in-monitoring-and-tagging-sharks-to-prevent-attacks")
  .showTags("all")
client.getResponse(itemWebTitleQuery) map { response =>
  for (tag <- response.content.get.tags) println(tag.webTitle)
}

// print the web title of the most viewed content items from the world section
val mostViewedTitleQuery = ContentApiLogic.item("world").showMostViewed()
client.getResponse(mostViewedTitleQuery) map { response =>
  for (result <- response.mostViewed.get) println(result.webTitle)
}
```

### Content

Filtering or searching for multiple content items happens at https://content.guardianapis.com/search. For example:

```scala
// print the total number of content items
val allContentSearch = ContentApiLogic.search
client.getResponse(allContentSearch) map { response =>
  println(response.total)
}

// print the web titles of the 15 most recent content items
val lastFifteenSearch = ContentApiLogic.search.pageSize(15)
client.getResponse(lastFifteenSearch) map { response =>
  for (result <- response.results) println(result.webTitle)
}

// print the web titles of the 10 most recent content items matching a search term
val toastSearch = ContentApiLogic.search.q("cheese on toast")
client.getResponse(toastSearch) map { response =>
  for (result <- response.results) println(result.webTitle)
}

// print the web titles of the 10 (default page size) most recent content items with certain tags
val tagSearch = ContentApiLogic.search.tag("lifeandstyle/cheese,type/gallery")
client.getResponse(tagSearch) map { response =>
  for (result <- response.results) println(result.webTitle)
}

// print the web titles of the 10 most recent content items in the world section
val sectionSearch = ContentApiLogic.search.section("world")
client.getResponse(sectionSearch) map { response =>
  for (result <- response.results) println(result.webTitle)
}

// print the web titles of the last 10 content items published a week ago
import java.time.temporal.ChronoUnit
import java.time.Instant
val timeSearch = ContentApiLogic.search.toDate(Instant.now().minus(7, ChronoUnit.DAYS))
client.getResponse(timeSearch) map { response =>
  for (result <- response.results) println(result.webTitle)
}

// print the web titles of the last 10 content items published whose type is article
val typeSearch = ContentApiLogic.search.contentType("article")
client.getResponse(typeSearch) map { response =>
  for (result <- response.results) println(result.webTitle)
}
```

### Tags

Filtering or searching for multiple tags happens at http://content.guardianapis.com/tags. For example:

```scala
// print the total number of tags
val allTagsQuery = ContentApiLogic.tags
client.getResponse(allTagsQuery) map { response =>
  println(response.total)
}

// print the web titles of the first 50 tags
val fiftyTagsQuery = ContentApiLogic.tags.pageSize(50)
client.getResponse(fiftyTagsQuery) map { response =>
  for (result <- response.results) println(result.webTitle)
}

// print the web titles and bios of the first 10 contributor tags which have them
val contributorTagsQuery = ContentApiLogic.tags.tagType("contributor")
client.getResponse(contributorTagsQuery) map { response =>
  for (result <- response.results.filter(_.bio.isDefined)) {
    println(result.webTitle + "\n" + result.bio.get + "\n")
  }
}

// print the web titles and numbers of the first 10 books tags with ISBNs
val isbnTagsSearch = ContentApiLogic.tags
  .section("books")
  .referenceType("isbn")
  .showReferences("isbn")
client.getResponse(isbnTagsSearch) map { response =>
  for (result <- response.results) {
    println(result.webTitle + " -- " + result.references.head.id)
  }
}
```

### Sections

Filtering or searching for multiple sections happens at http://content.guardianapis.com/sections. For example:

```scala
// print the web title of each section
val allSectionsQuery = ContentApiLogic.sections
client.getResponse(allSectionsQuery) map { response =>
  for (result <- response.results) println(result.webTitle)
}

// print the web title of each section with 'network' in the title
val networkSectionsQuery = ContentApiLogic.sections.q("network")
client.getResponse(networkSectionsQuery) map { response =>
  for (result <- response.results) println(result.webTitle)
}
```

### Editions

Filtering or searching for multiple Editions happens at http://content.guardianapis.com/editions. For example:

```scala
// print the apiUrl of each edition
val allEditionsQuery = ContentApiLogic.editions
client.getResponse(allEditionsQuery) map { response =>
  for (result <- response.results) println(result.apiUrl)
}

// print the webUrl of the edition with 'US' in edition field.
val usEditionsQuery = ContentApiLogic.editions.q("US")
client.getResponse(usEditionsQuery) map { response =>
  for (result <- response.results) println(result.webUrl)
}
```

### Removed Content

Filtering or searching for removed content happens at http://content.guardianapis.com/content/removed. For example:

```scala
// print the id of all removed content items
val removedContentQuery = ContentApiLogic.removedContent
client.getResponse(removedContentQuery) map { response =>
  for (result <- response.results) println(result)
}

// print the id of all expired content
val expiredContentQuery = ContentApiLogic.removedContent.reason("expired")
client.getResponse(expiredContentQuery ) map { response =>
  for (result <- response.results) println(result)
}
```

### Pagination
The client allows you to paginate through results in the following ways:
* `paginate(query)(f)` unfolds a query until there are no more page results to process. `f` is a pure function processing a CAPI response and `paginate` returns a list of processed responses (wrapped in a `Future`)
* `paginateAccum(query)(f, g)` folds over the results and accumulates into a final result. `f` transforms a response into an accumulated result, `g` [multiplies](https://en.wikipedia.org/wiki/Semigroup) two results together
* `paginateFold(query)(f, m)` folds over the results by accumulating a final result. `f` takes two parameters: a response and the accumulated result so far.

E.g. the following simply sums the number of results:

```
val result: Future[Int] = client.paginateFold(query)(0){ (r: SearchResponse, t: Int) => r.results.length + t }
```

## Backoff
Sometimes the backend services that the client relies on can return HTTP failure results, and some of these are potentially recoverable within a relatively short period of time.
Rather than always fail these requests by default, we have made it possible to automatically retry those failures that may yield a successful result on a subsequent attempt. 
As of version 15.1 of the client we have made it compulsory to provide a declaration of a `Backoff` class when an implementation of `ContentApiClient` is declared.
In its most basic form this is just a case of adding the following line to your implementation of a `ContentApiClient` descended class:

```scala
  override val backoffStrategy: Backoff = Backoff()
``` 

The above will give you a delay-doubling backoff strategy starting at 250ms and retrying the request up to 3 times.

Other backoff types are available, and may be accessed by calling one of the following:

```scala
  override val backoffStrategy: Backoff = Backoff.exponential()
  override val backoffStrategy: Backoff = Backoff.multiple(factor = 3.0)
``` 

These definitions give you a default implementation of an exponential strategy (an initial 100ms delay, with 3 retries where each delay is incremented by a power of 2 after each failure), 
and a multiple strategy (an initial delay of 250ms, with three retries, each failure multiplying, in this example, the delay time by 3) respectively.

Note that `Backoff.multiple` is the only instance where a parameter value for the multiplication factor is required, however you can provide parameters to the
`exponential` and `doubling` strategies if you wish. These must be a retry interval expressed as a `scala.concurrent.duration.Duration` and a retry count as a regular `Int`.

e.g.
```scala
class MyApiClient(ws: WSClient) extends ContentApiClient
  val retrySchedule: Duration = Duration(500L, TimeUnit.MILLISECONDS)
  val retryCount: Int = 5
  override val backoffStrategy: Backoff = Backoff.exponential(retrySchedule, retryCount)
  .
  . 
}
```
or
```scala
class MyApiClient(ws: WSClient) extends ContentApiClient
  val retrySchedule: Duration = Duration(250L, TimeUnit.MILLISECONDS)
  val retryCount: Int = 10
  val factor: Double = 4.0
  override val backoffStrategy: Backoff = Backoff.multiple(retrySchedule, retryCount, factor)
  .
  . 
}
```

Note that the parameters passed are subject to limit caps on the upper and lower end, to prevent using values that either result in excessively long or short waits, or attempts to circumvent the backoff strategy entirely by forcing immediate retries.

`exponential` requires a minimum delay of 100ms, and a max of 5 retry attempts.

`doubling` requires a minimum of 250ms delay, and a maximum of 10 retry attempts.

`multiple` also requires a minimum 250ms delay, and a multiplication factor of at least 2.0. As with the doubling strategy, this is limited to 10 retries.


If any values are omitted defaults will be applied automatically thus;

`exponential`: 100ms interval, 3 retries

`doubling`: 250ms interval, 3 retries

`multiple`: 250ms interval, 3 retries, but a multiplication factor must be supplied (there is no upper limit for the factor value)

Actually, `doubling` is merely an implementation of the `multiple` strategy with a factor fixed at 2.0.

### Which response codes will be retried by the backoff strategy

Initially we have specified the following codes as potentially recoverable:

* 408 - Request Timeout
* 429 - Too Many Requests
* 503 - Service Unavailable
* 504 - Gateway Timeout
* 509 - Bandwidth Limit Exceeded

This list may be subject to change over time.

## Explore in the REPL

One easy way to get started with the client is to try it in the Scala REPL.

First clone this repo, then run `sbt console` from the `client` directory. This will start a REPL with a few useful things imported for you, so you can get started quickly:

```
scala> val client = new GuardianContentClient("YOUR API KEY HERE")
client: com.gu.contentapi.client.GuardianContentClient = com.gu.contentapi.client.GuardianContentClient@3eb2a60

scala> val query = ContentApiLogic.search.showTags("all")
query: com.gu.contentapi.client.model.SearchQuery = SearchQuery(/search?show-tags=all)

scala> val response = Await.result(client.getResponse(query), 5.seconds)
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
response: com.gu.contentapi.client.model.v1.SearchResponse =
SearchResponse(ok,developer,1853997,1,10,1,185400,newest,List(Content(politics/blog/live/2016/may/17/corbyn-more-popular-than-ever-with-labour-members-poll-suggests-politics-live,Liveblog,Some(politics),Some(Politics),Some(CapiDateTime(1463487146000,2016-05-17T12:12:26.000Z)),EU referendum: Boris Johnson accuses Cameron of making UK look like 'banana republic' - Politics live,https://www.theguardian.com/politics/blog/live/2016/may/17/corbyn-more-popular-than-ever-with-labour-members-poll-suggests-politics-live,https://content.guardianapis.com/politics/blog/live/2016/may/17/corbyn-more-popular-than-ever-with-labour-members-poll-suggests-politics-live,None,List(Tag(politics/series/politics-live-with-andrew-sparrow,Series,Some(po...

scala> client.shutdown()
```

## Running Tests

Some tests require access to the API. See [Setup](#setup) for details on how to get one.

The key needs to be passed to the tests either as a system property or an environment variable.
```sh
$ cd client
$ sbt -DCAPI_TEST_KEY=your_api_key test
# or
$ env CAPI_TEST_KEY=your_api_key sbt test
```

## Troubleshooting

If you have any problems you can speak to other developers at the [Guardian API talk group] (http://groups.google.com/group/guardian-api-talk).
