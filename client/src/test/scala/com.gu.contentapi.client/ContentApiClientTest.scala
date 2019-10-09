package com.gu.contentapi.client

import com.gu.contentapi.client.model._
import java.time.Instant

import com.gu.contentapi.client.model.v1.{ErrorResponse, SearchResponse}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class ContentApiClientTest extends FlatSpec with Matchers with ScalaFutures with OptionValues with BeforeAndAfterAll with Inside with Inspectors {
  private val api = new ContentApiClient {

    val apiKey = "TEST-API-KEY"

    def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext) = {
      Future.successful(HttpResponse(Array(), 500, "status"))
    }
  }

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))
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

  it should "produce next urls for 10 results ordered by relevance" in {
    val query = ContentApiClient.search.q("brexit")
    val next = ContentApiClient.next(query, "hello")

    testPaginatedQuery("content/hello/next", 10, "relevance", Some("brexit"))(next)
  }

  it should "produce next urls for 20 results order by newest" in {
    val query = ContentApiClient.search.pageSize(20)
    val next = ContentApiClient.next(query, "hello")

    testPaginatedQuery("content/hello/", 20, "newest")(next)
  }

  it should "recover gracefully from error" in {

    val query = SearchQuery()
    val errorTest = api.paginateFold(query)(Seq(): Seq[SearchResponse]){
      (response: SearchResponse, acc: Seq[SearchResponse]) => acc :+ response
    } recover {
      case graceful: ContentApiError => succeed
      case notGraceful => fail("Threw the wrong exception")
    }
    errorTest.futureValue
  }

  def testPaginatedQuery(pt: String, page: Int, ob: String, q: Option[String] = None)(query: ContentApiQuery) = {
    val ps = query.parameters
    query.pathSegment should startWith (pt)
    ps.get("page-size") should be (Some(page.toString))
    ps.get("order-by") should be (Some(ob))
    q.map(q => ps.get("q") should be (Some(q))).getOrElse(succeed)
  }
}
