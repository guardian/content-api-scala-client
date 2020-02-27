package com.gu.contentapi.client

import com.gu.contentapi.buildinfo.CapiBuildInfo
import com.gu.contentapi.client.HttpRetry.withRetry
import com.gu.contentapi.client.model.HttpResponse.isSuccessHttpResponse
import com.gu.contentapi.client.model._
import com.gu.contentatom.thrift.AtomType

import scala.concurrent.{ExecutionContext, Future}

trait ContentApiClient {
  import Decoder._

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
  private val headers =
    Map("User-Agent" -> userAgent, "Accept" -> "application/x-thrift", "Accept-Language" -> "*")

  /** Authentication and format parameters appended to each query */
  private def parameters = Map("api-key" -> apiKey, "format" -> "thrift")

  /** Streamlines the handling of a valid CAPI response */

  private def fetchResponse(contentApiQuery: ContentApiQuery)(implicit context: ExecutionContext): Future[Array[Byte]] = get(url(contentApiQuery), headers).flatMap {
    case response if isSuccessHttpResponse(response) => Future.successful(response)
    case response => Future.failed(ContentApiError(response))
  }.map(_.body)

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

trait RetryableContentApiClient extends ContentApiClient {
  def backoffStrategy: BackoffStrategy
  implicit def executor: ScheduledExecutor

  abstract override def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse] = withRetry(backoffStrategy){ retryAttempt =>
    super.get(url, headers + ("Request-Attempt" -> s"$retryAttempt"))
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