package com.gu.contentapi.client

import com.gu.contentapi.client.model.ContentApiQuery
import java.time.Instant
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Inside, Inspectors, Matchers, OptionValues}
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global

class ContentApiClientTest extends FlatSpec with Matchers with ScalaFutures with OptionValues with BeforeAndAfterAll with Inside with Inspectors {
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

    forEvery(Map(next -> "next", prev -> "prev"))(testPaginatedQuery("content/hello/", 10, "relevance", Some("brexit")))
  }

  it should "produce next/prev urls for 20 results order by newest" in {
    val query = ContentApiClient.search.pageSize(20)
    val next = ContentApiClient.next(query, "hello")
    val prev = ContentApiClient.prev(query, "hello")

    forEvery(Map(next -> "next", prev -> "prev"))(testPaginatedQuery("content/hello/", 20, "newest"))
  }

  def testPaginatedQuery(pt: String, page: Int, ob: String, q: Option[String] = None)(params: (ContentApiQuery, String)) = {
    val ps = params._1.parameters
    params._1.pathSegment should startWith (pt + params._2)
    ps.get("page-size") should be (Some(page.toString))
    ps.get("order-by") should be (Some(ob))
    q.map(q => ps.get("q") should be (Some(q))).getOrElse(succeed)
  }
}
