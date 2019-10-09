package com.gu.contentapi.client

import com.gu.contentapi.buildinfo.CapiBuildInfo
import com.gu.contentapi.client.model._
import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.thrift.ThriftDeserializer
import com.gu.contentapi.client.utils.QueryStringParams
import com.gu.contentatom.thrift.AtomType
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import scala.util.{Failure, Success}

trait ContentApiClient {
  import Decoder._
  import PaginatedApiResponse._

  /**  **/
  val scheduledExecutor: ScheduledExecutor = new ScheduledExecutor(1)

  /** Your API key */
  def apiKey: String

  /** The user-agent identifier */
  def userAgent: String = "content-api-scala-client/"+CapiBuildInfo.version
  
  /** The url of the CAPI endpoint */
  def targetUrl: String = "https://content.guardianapis.com"

  /** Queries CAPI.
    *
    * This method must make a GET request to the CAPI endpoint
    * and streamline the response into an HttpResponse object.
    * 
    * It is a design decision that this method is virtual.
    * Any implementation would have to rely on a specific
    * technology stack, e.g. an HTTP client. Fundamentally,
    * the responsibility of making these implementation
    * choices should be pushed out to the end of the world.
    *
    * @param url The CAPI REST url 
    * @param headers Custom HTTP parameters
    * @return an HttpResponse holding the response in the form of an array of bytes 
    */
  def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse]

  /** Some HTTP headers sent along each CAPI request */
  private def headers = Map("User-Agent" -> userAgent, "Accept" -> "application/x-thrift")

  /** Authentication and format parameters appended to each query */
  private def parameters = Map("api-key" -> apiKey, "format" -> "thrift")

  val maxRetries: Int = 3 // max wait time ~1.5s

  /** Streamlines the handling of a valid CAPI response */
  private def fetchResponse(contentApiQuery: ContentApiQuery, attempt: Int = 1)(implicit context: ExecutionContext): Future[Array[Byte]] =
    fetchResponseWithBackoff(contentApiQuery, attempt)(context).flatMap { _.flatMap { HttpResponse.check }}

  private def fetchResponseWithBackoff(contentApiQuery: ContentApiQuery, attempt: Int)(implicit context: ExecutionContext): CancellableFuture[Future[HttpResponse]] = {
    val waitDuration = scheduledExecutor.getWaitDuration(attempt)
    // To test this - uncomment the following line
    // println(s"fetchResponseWithBackoff attempt: ${attempt} waitDuration: ${waitDuration}")
    // and add 400 to the HttpResponse failedButMaybeRecoverable set
    // and something like 400 -> "Not a real recoverable - just for testing" to the recoverableErrorMessages list, then
    // run the tests, and the should "handle error responses" test will cause these printlns to be output:
    //    fetchResponseWithBackoff attempt: 1 waitDuration: 0 milliseconds
    //    fetchResponseWithBackoff attempt: 2 waitDuration: 500 milliseconds
    //    fetchResponseWithBackoff attempt: 3 waitDuration: 1000 milliseconds
    // and ultimately fail with the message
    //    [info] - should handle error responses *** FAILED ***
    //    [info]   The future returned an exception of type: org.scalatest.exceptions.TestFailedException, with message: com.gu.contentapi.client.model.ContentApiRecoverableException: Not a real recoverable - just for testing was not equal to com.gu.contentapi.client.model.ContentApiError: Bad Request. (GuardianContentClientTest.scala:53)

    val op = get(url(contentApiQuery), headers)(context)
    val delayed = scheduledExecutor.delayExecution(op)(by = waitDuration)
    delayed.onComplete({
      case Success(t) =>
        t.flatMap{ r =>
          try {
            HttpResponse.check(r)
          } catch {
            case e: ContentApiRecoverableException if attempt < maxRetries =>
              fetchResponseWithBackoff(contentApiQuery, attempt + 1)
          }
        }
      case _ =>
    })
    delayed
  }

  private def unfoldM[A, B](f: B => (A, Option[Future[B]]))(fb: Future[B])(implicit ec: ExecutionContext): Future[List[A]] =
    fb.flatMap { b =>
      f(b) match {
        case (a, None) => Future.successful(a :: Nil)
        case (a, Some(b)) => unfoldM(f)(b).map(a :: _)
      }
    }

  def url(contentApiQuery: ContentApiQuery): String =
    contentApiQuery.getUrl(targetUrl, parameters)
    
  /** Runs the query against the Content API.
    * 
    * @tparam Q the type of a Content API query
    * @param query the query
    * @return a future resolving to an unmarshalled response
    */
  def getResponse[Q <: ContentApiQuery](query: Q)(
    implicit 
    decoder: Decoder[Q],
    context: ExecutionContext): Future[decoder.Response] =
    fetchResponse(query) map decoder.decode

  /** Unfolds a query to its results, page by page
    * 
    * @tparam Q the type of a Content API query with pagination parameters
    * @tparam R the type of response expected for `Q`
    * @param query the initial query
    * @param f a result-processing function
    * @return a future of a list of result-processed results
    */
  def paginate[Q <: PaginatedApiQuery[Q], R, M](query: Q)(f: R => M)(
    implicit 
    decoder: Decoder.Aux[Q, R],
    pager: PaginatedApiResponse[R],
    context: ExecutionContext
  ): Future[List[M]] =
    unfoldM { r: R =>
      val nextQuery = pager.getNextId(r).map { id => getResponse(ContentApiClient.next(query, id)) }
      (f(r), nextQuery)      
    }(getResponse(query))

  /** Unfolds a query by accumulating its results
    * 
    * @tparam Q the type of a Content API query with pagination parameters
    * @tparam R the type of response expected for `Q`
    * @param query the initial query
    * @param f a result-processing function
    * @return a future of an accumulated value
    */
  def paginateAccum[Q <: PaginatedApiQuery[Q], R, M](query: Q)(f: R => M, g: (M, M) => M)(
    implicit 
    decoder: Decoder.Aux[Q, R],
    pager: PaginatedApiResponse[R],
    context: ExecutionContext
  ): Future[M] =
    paginate(query)(f).map {
      case Nil => throw new RuntimeException("Something went wrong with the query")
      case m :: Nil => m
      case ms => ms.reduce(g)
    }

  /** Unfolds a query by accumulating its results
    * 
    * @tparam Q the type of a Content API query with pagination parameters
    * @tparam R the type of response expected for `Q`
    * @param query the initial query
    * @param f a result-processing function
    * @return a future of an accumulated value
    */
  def paginateFold[Q <: PaginatedApiQuery[Q], R, M](query: Q)(m: M)(f: (R, M) => M)(
    implicit 
    decoder: Decoder.Aux[Q, R],
    decoderNext: Decoder.Aux[NextQuery[Q], R],
    pager: PaginatedApiResponse[R],
    context: ExecutionContext
  ): Future[M] = {
    def paginateFoldIn(nextQuery: Option[NextQuery[Q]])(m: M): Future[M] = {
      val req = nextQuery.map(getResponse(_)).getOrElse(getResponse(query))
      req.flatMap { r: R =>
        pager.getNextId(r) match {
          case None => Future.successful(f(r, m))
          case Some(id) => paginateFoldIn(Some(ContentApiClient.next(query, id)))(f(r, m))
        }
      }
    }

    paginateFoldIn(None)(m)
  }
}

object ContentApiClient extends ContentApiQueries

/** Utility functions to instantiate each type of query */
trait ContentApiQueries {
  def item(id: String) = ItemQuery(id)
  val search = SearchQuery()
  val tags = TagsQuery()
  val sections = SectionsQuery()
  val editions = EditionsQuery()
  val removedContent = RemovedContentQuery()
  val atoms = AtomsQuery()
  def atomUsage(atomType: AtomType, atomId: String) = AtomUsageQuery(atomType, atomId)
  val recipes = RecipesQuery()
  val reviews = ReviewsQuery()
  val gameReviews = GameReviewsQuery()
  val restaurantReviews = RestaurantReviewsQuery()
  val filmReviews = FilmReviewsQuery()
  val videoStats = VideoStatsQuery()
  def next[Q <: PaginatedApiQuery[Q]](q: Q, id: String) = NextQuery(normalize(q), id)

  private def normalize[Q <: PaginatedApiQuery[Q]]: Q => Q =
    normalizePageSize andThen normalizeOrder

  private def normalizePageSize[Q <: PaginatedApiQuery[Q]]: Q => Q = 
    q => if (q.has("page-size")) q else q.pageSize(10)

  private def normalizeOrder[Q <: PaginatedApiQuery[Q]]: Q => Q = 
    q => if (q.has("order-by")) 
      q 
    else if (q.has("q"))
      q.orderBy("relevance")
    else
      q.orderBy("newest")
}