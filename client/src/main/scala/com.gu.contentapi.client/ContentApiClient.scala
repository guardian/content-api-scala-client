package com.gu.contentapi.client

import com.gu.contentapi.buildinfo.CapiBuildInfo
import com.gu.contentapi.client.HttpRetry.withRetry
import com.gu.contentapi.client.model.Direction.Next
import com.gu.contentapi.client.model.HttpResponse.isSuccessHttpResponse
import com.gu.contentapi.client.model._
import com.gu.contentapi.client.thrift.ThriftDeserializer
import com.gu.contentatom.thrift.AtomType
import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}

import scala.concurrent.{ExecutionContext, Future}

trait ContentApiClient {

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

  private def fetchResponse(contentApiQuery: ContentApiQuery[_])(implicit ec: ExecutionContext): Future[Array[Byte]] = get(url(contentApiQuery), headers).flatMap {
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

  def url(contentApiQuery: ContentApiQuery[_]): String =
    contentApiQuery.getUrl(targetUrl, parameters)
    
  /** Runs the query against the Content API.
    * 
    * @tparam Q the type of a Content API query
    * @param query the query
    * @return a future resolving to an unmarshalled response
    */
  def getResponse[R <: ThriftStruct](query: ContentApiQuery[R])(
    implicit
    decoder: Decoder[R],
    context: ExecutionContext): Future[R] =
    fetchResponse(query) map decoder.decode

  /** Unfolds a query to its results, page by page
    *
    * @param query the initial query
    * @tparam R the response type expected for this query
    * @tparam E the 'element' type for the list of elements returned in the response - eg 'Tag' for 'TagsResponse'
    * @tparam M a type specified by the caller to summarise the results of each response. Eg, might be `Seq[E]`
    * @param f a result-processing function that converts the standard response type to the `M` type
    * @return a future of a list of result-processed results (eg, if `M` = `Seq[E]`, the final result is `List[Seq[E]]`)
    */
  // `R : Decoder` is a Scala 'context-bound', and means "To compile I need an implicit instance of `Decoder[R]`!"
  def paginate[R <: ThriftStruct: Decoder, E, M](query: PaginatedApiQuery[R, E])(f: R => M)(
    implicit
    context: ExecutionContext
  ): Future[List[M]] =
    unfoldM { r: R =>
      (f(r), query.followingQueryGiven(r, Next).map(getResponse(_)))
    }(getResponse(query))

  /** Unfolds a query by accumulating its results - each response is transformed (by function `f`) and then combined
    * (with function `g`) into a single accumulated result object.
    *
    * @param query the initial query
    * @tparam R the response type expected for this query
    * @tparam E the 'element' type for the list of elements returned in the response - eg 'Tag' for 'TagsResponse'
    * @tparam M a type specified by the caller to summarise the results of each response. Eg, might be `Seq[E]`
    * @param f a result-processing function that converts the standard response type to the `M` type
    * @param g a function that squashes together ('reduces') two `M` types - eg concatenates two `Seq[E]`
    * @return a future of the accumulated value
    */
  def paginateAccum[R <: ThriftStruct: Decoder, E, M](query: PaginatedApiQuery[R, E])(f: R => M, g: (M, M) => M)(
    implicit
    context: ExecutionContext
  ): Future[M] =
    paginate(query)(f).map {
      case Nil => throw new RuntimeException("Something went wrong with the query")
      case m :: Nil => m
      case ms => ms.reduce(g)
    }

  /** Unfolds a query by accumulating its results - each response is transformed and added to an accumulator value
    * by a single folding function `f`.
    *
    * @param query the initial query
    * @tparam R the response type expected for this query
    * @tparam E the 'element' type for the list of elements returned in the response - eg 'Tag' for 'TagsResponse'
    * @tparam M a type specified by the caller to summarise the results the responses. Eg, might be `Int`
    * @param m an initial 'empty' starting value to begin the accumulation with. Eg, might be `0`
    * @param f a result-processing function that adds the result of a response to the summary value accumulated so far
    * @return a future of the accumulated value
    */
  def paginateFold[R <: ThriftStruct: Decoder, E, M](query: PaginatedApiQuery[R, E])(m: M)(f: (R, M) => M)(
    implicit
    context: ExecutionContext
  ): Future[M] = {
    def paginateFoldIn(currentQuery: Option[PaginatedApiQuery[R, E]])(m: M): Future[M] = {
      val req = currentQuery.map(getResponse(_)).getOrElse(getResponse(query))
      req.flatMap { r: R =>
        query.followingQueryGiven(r, Next) match {
          case None => Future.successful(f(r, m))
          case Some(nextQuery) => paginateFoldIn(Some(nextQuery))(f(r, m))
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
  @deprecated("Recipe atoms no longer exist and should not be relied upon. No data will be returned and this query will be removed in a future iteration of the library")
  val recipes = RecipesQuery()
  val reviews = ReviewsQuery()
  val gameReviews = GameReviewsQuery()
  val restaurantReviews = RestaurantReviewsQuery()
  val filmReviews = FilmReviewsQuery()
  val videoStats = VideoStatsQuery()
}