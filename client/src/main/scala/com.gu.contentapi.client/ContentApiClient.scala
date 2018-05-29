package com.gu.contentapi.client

import com.gu.contentapi.buildinfo.CapiBuildInfo
import com.gu.contentapi.client.model._
import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.thrift.ThriftDeserializer
import com.gu.contentapi.client.utils.QueryStringParams
import scala.concurrent.{ExecutionContext, Future}

trait ContentApiClient {
  import Decoder._
  import PaginatedApiResponse._
  
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

  /** Streamlines the handling of a valid CAPI response */
  private def fetchResponse(contentApiQuery: ContentApiQuery)(implicit context: ExecutionContext): Future[Array[Byte]] =
    get(url(contentApiQuery), headers).flatMap(HttpResponse.check)

  private def paginate2[Q <: PaginatedApiQuery[Q], R](q: Q, f: R => Future[Unit])(r: R)(
    implicit
    decoder: Decoder.Aux[Q, R],
    pager: PaginatedApiResponse[R],
    context: ExecutionContext): Future[Unit] =
    f(r).flatMap { _ =>
      pager.getNextId(r) match {
        case Some(id) => getResponse(ContentApiClient.next(q, id)).flatMap(paginate2(q, f)(_))
        case _        => Future.successful(())
      }
    }
    
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

  /** Runs a query and process all the pages of results.
    * 
    * @tparam Q the type of a Content API query with pagination parameters
    * @tparam R the type of response corresponding to `Q`
    * @param query the initial query
    * @param f the side-effecting function applied to each page of results
    * @return a future resolving to the process of going through all pages
    */
  def paginate[Q <: PaginatedApiQuery[Q], R](query: Q)(f: R => Future[Unit])(
    implicit 
    decoder: Decoder.Aux[Q, R],
    pager: PaginatedApiResponse[R],
    context: ExecutionContext
  ): Future[Unit] =
    getResponse(query).flatMap(paginate2(query, f))

  def url(contentApiQuery: ContentApiQuery): String =
    contentApiQuery.getUrl(targetUrl, parameters)
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
  val recipes = RecipesQuery()
  val reviews = ReviewsQuery()
  val gameReviews = GameReviewsQuery()
  val restaurantReviews = RestaurantReviewsQuery()
  val filmReviews = FilmReviewsQuery()
  val videoStats = VideoStatsQuery()
  val stories = StoriesQuery()
  def next[Q <: PaginatedApiQuery[Q]](q: Q, id: String) = NextQuery(q, id)
  def prev[Q <: PaginatedApiQuery[Q]](q: Q, id: String) = PrevQuery(q, id)
}