package com.gu.contentapi.client

import com.gu.contentapi.client.model.Direction.{Next, Previous}
import com.gu.contentapi.client.model._
import com.gu.contentapi.client.model.v1.{Content, SearchResponse}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}

import java.time.Instant
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class ContentApiClientTest extends FlatSpec with Matchers with ScalaFutures with OptionValues with BeforeAndAfterAll with Inside with Inspectors {
  private val api = new ContentApiClient {
    val retryDuration = Duration(250L, TimeUnit.MILLISECONDS)
    val maxRetries = 5

    val apiKey = "TEST-API-KEY"

    def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse] =
      Future.successful(HttpResponse(Array(), 500, "status"))
  }

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))
  it should "correctly add API key to request" in {
    api.url(ContentApiClient.search) should include(s"api-key=${api.apiKey}")
  }

  it should "generate the correct path segments for following a query" in {
    val queryForNext = FollowingSearchQuery(
      ContentApiClient.search,
      "commentisfree/picture/2022/may/15/nicola-jennings-boris-johnson-northern-ireland-cartoon",
      Next
    )

    api.url(queryForNext) should include(s"content/commentisfree/picture/2022/may/15/nicola-jennings-boris-johnson-northern-ireland-cartoon/next")

    val queryForPrevious = queryForNext.copy(direction = Previous)
    api.url(queryForPrevious) should include(s"content/commentisfree/picture/2022/may/15/nicola-jennings-boris-johnson-northern-ireland-cartoon/prev")
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

  def stubContent(capiId: String): Content = Content(capiId, webTitle="", webUrl="", apiUrl="")
  def stubContents(num: Int) = (1 to num).map(i => stubContent(s"blah-$i"))
  def stubSearchResponse(pageSize: Int, orderBy: String, results: Seq[Content]): SearchResponse = SearchResponse(
    status = "", userTier="", total = -1, startIndex = -1, currentPage = -1, pages= -1, orderBy = orderBy,
    pageSize = pageSize, // Needed for deciding next query
    results = results)

  it should "produce next urls for 10 results ordered by relevance" in {
    val query = ContentApiClient.search.q("brexit")
    val next = query.followingQueryGiven(stubSearchResponse(
      pageSize = 10,
      orderBy = "relevance",
      stubContents(9) :+ stubContent("hello")
    ), Next).value

    testPaginatedQuery("content/hello/next", 10, "relevance", Some("brexit"))(next)
  }

  it should "produce next urls for 20 results order by newest" in {
    val query = ContentApiClient.search.pageSize(20)
    val next = query.followingQueryGiven(stubSearchResponse(
      pageSize = 20,
      orderBy = "newest",
      stubContents(19) :+ stubContent("hello")
    ), Next).value

    testPaginatedQuery("content/hello/next", 20, "newest")(next)
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

  def testPaginatedQuery(pt: String, page: Int, ob: String, q: Option[String] = None)(query: ContentApiQuery[_]) = {
    val ps = query.parameters
    query.pathSegment should startWith (pt)
    ps.get("page-size") should be (Some(page.toString))
    ps.get("order-by") should be (Some(ob))
    q.map(q => ps.get("q") should be (Some(q))).getOrElse(succeed)
  }
}
