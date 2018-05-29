package com.gu.contentapi.client

import java.time.Instant
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Inside, Matchers, OptionValues}
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global

class ContentApiClientTest extends FlatSpec with Matchers with ScalaFutures with OptionValues with BeforeAndAfterAll with Inside {
  private val api = new ContentApiClient {
    val apiKey = "TEST-API-KEY"

    def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext) = Future.failed(new Throwable("This will never get called"))
  }

  it should "correctly add API key to request" in {
    api.url(ContentApiClient.search) should include(s"api-key=${api.apiKey}")
  }

  it should "understand custom parameters" in {
    val now = Instant.now()
    val params = ContentApiClient.search
      .stringParam("aStringParam", "foo")
      .intParam("aIntParam", 3)
      .dateParam("aDateParam", now)
      .boolParam("aBoolParam", true)
      .parameters

    params.get("aStringParam") should be (Some("foo"))
    params.get("aIntParam") should be (Some("3"))
    params.get("aDateParam") should be (Some(now.toString))
    params.get("aBoolParam") should be (Some("true"))
  }

  behavior of "Paginated queries"

  it should "produce next/prev urls for 10 results ordered by relevance" in {
    val query = ContentApiClient.search.q("brexit")
    val next = ContentApiClient.next(query, "hello")
    val prev = ContentApiClient.prev(query, "hello")
    val nextP = next.parameters
    val prevP = prev.parameters

    next.pathSegment should startWith ("content/hello/next")
    prev.pathSegment should startWith ("content/hello/prev")

    nextP.get("q") should be (Some("brexit"))
    prevP.get("q") should be (Some("brexit"))

    nextP.get("page-size") should be (Some("10"))
    prevP.get("page-size") should be (Some("10"))

    nextP.get("order-by") should be (Some("relevance"))
    prevP.get("order-by") should be (Some("relevance"))
  }

  it should "produce next/prev urls for 10 results order by newest" in {
    val query = ContentApiClient.search
    val next = ContentApiClient.next(query, "hello")
    val prev = ContentApiClient.prev(query, "hello")
    val nextP = next.parameters
    val prevP = prev.parameters

    next.pathSegment should startWith ("content/hello/next")
    prev.pathSegment should startWith ("content/hello/prev")

    nextP.get("page-size") should be (Some("10"))
    prevP.get("page-size") should be (Some("10"))

    nextP.get("order-by") should be (Some("newest"))
    prevP.get("order-by") should be (Some("newest"))
  }
}
