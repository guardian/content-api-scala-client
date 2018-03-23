package com.gu.contentapi.client

import java.io.IOException
import java.util.concurrent.TimeUnit
import com.gu.contentapi.client.model._
import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.utils.QueryStringParams
import com.gu.contentapi.buildinfo.CapiBuildInfo
import okhttp3._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try
import com.gu.contentapi.client.thrift.ThriftDeserializer

case class GuardianContentApiError(httpStatus: Int, httpMessage: String, errorResponse: Option[ErrorResponse] = None) extends Exception(httpMessage)

trait ContentApiClientLogic {
  import Decoder._
  import PaginatedApiResult._

  val apiKey: String

  protected val userAgent = "content-api-scala-client/"+CapiBuildInfo.version

  protected lazy val http = new OkHttpClient.Builder()
    .connectTimeout(1, TimeUnit.SECONDS)
    .readTimeout(2, TimeUnit.SECONDS)
    .followRedirects(true)
    .connectionPool(new ConnectionPool(10, 60, TimeUnit.SECONDS))
    .build()

  val targetUrl = "https://content.guardianapis.com"

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

  case class HttpResponse(body: Array[Byte], statusCode: Int, statusMessage: String)

  protected[client] def url(location: String, parameters: Map[String, String]): String = {
    require(!location.contains('?'), "must not specify parameters in URL")

    location + QueryStringParams(parameters + ("api-key" -> apiKey) + ("format" -> "thrift"))
  }

  protected def fetch(url: String)(implicit context: ExecutionContext): Future[Array[Byte]] = {
    val headers = Map("User-Agent" -> userAgent, "Accept" -> "application/x-thrift")

    for (response <- get(url, headers)) yield {
      if (List(200, 302) contains response.statusCode) response.body
      else throw contentApiError(response)
    }
  }

  private def contentApiError(response: HttpResponse): GuardianContentApiError = {
    val errorResponse = Try(ThriftDeserializer.deserialize(response.body, ErrorResponse)).toOption
    GuardianContentApiError(response.statusCode, response.statusMessage, errorResponse)
  }

  protected def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse] = {

    val reqBuilder = new Request.Builder().url(url)
    val req = headers.foldLeft(reqBuilder) {
      case (r, (name, value)) => r.header(name, value)
    }

    val promise = Promise[HttpResponse]()

    http.newCall(req.build()).enqueue(new Callback() {
      override def onFailure(call: Call, e: IOException): Unit = promise.failure(e)
      override def onResponse(call: Call, response: Response): Unit = {
        promise.success(HttpResponse(response.body().bytes, response.code(), response.message()))
      }
    })

    promise.future
  }

  def getUrl(contentApiQuery: ContentApiQuery): String =
    url(s"$targetUrl/${contentApiQuery.pathSegment}", contentApiQuery.parameters)

  private def fetchResponse(contentApiQuery: ContentApiQuery)(implicit context: ExecutionContext): Future[Array[Byte]] =
    fetch(getUrl(contentApiQuery))


  private def paginate2[Q <: PaginatedApiQuery[Q], R](q: Q, f: R => Future[Unit])(r: R)(
    implicit
    decoder: Decoder.Aux[Q, R],
    pager: PaginatedApiResult[R],
    context: ExecutionContext): Future[Unit] =
    f(r).flatMap { _ =>
      pager.getNextId(r) match {
        case Some(id) => getResponse(next(q, id)).flatMap(paginate2(q, f)(_))
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
    * @tparam Q the type of a Content API query with paging parameters
    * @tparam R the type of response corresponding to `Q`
    * @param query the initial query
    * @param f the side-effecting function applied to each page of results
    * @return a future resolving to the process of going through all pages
    */
  def paginate[Q <: PaginatedApiQuery[Q], R](query: Q)(f: R => Future[Unit])(
    implicit 
    decoder: Decoder.Aux[Q, R],
    pager: PaginatedApiResult[R],
    context: ExecutionContext
  ): Future[Unit] =
    getResponse(query).flatMap(paginate2(query, f))

  /** Shutdown the client and clean up all associated resources.
    *
    * Note: behaviour is undefined if you try to use the client after calling this method.
    */
  def shutdown(): Unit = http.dispatcher().executorService().shutdown()

}

class GuardianContentClient(val apiKey: String) extends ContentApiClientLogic

