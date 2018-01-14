package com.gu.contentapi.client

import com.gu.contentapi.client.model._
import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.utils.QueryStringParams
import com.gu.contentapi.buildinfo.CapiBuildInfo
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try
import com.gu.contentapi.client.thrift.ThriftDeserializer

case class GuardianContentApiError(httpStatus: Int, httpMessage: String, errorResponse: Option[ErrorResponse] = None) extends Exception(httpMessage)
case class HttpResponse(body: Array[Byte], statusCode: Int, statusMessage: String)

trait ContentApiClientLogic {
  val apiKey: String
  val targetUrl: String

  protected val userAgent = "content-api-scala-client/"+CapiBuildInfo.version


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

  protected def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse]

  def getUrl(contentApiQuery: ContentApiQuery): String =
    url(s"$targetUrl/${contentApiQuery.pathSegment}", contentApiQuery.parameters)

  private def fetchResponse(contentApiQuery: ContentApiQuery)(implicit context: ExecutionContext): Future[Array[Byte]] =
    fetch(getUrl(contentApiQuery))


  /* Exposed API */

  import Codec._

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

  def getResponse[Q <: ContentApiQuery](q: Q)(
    implicit 
    context: ExecutionContext,
    codec: Codec[Q]
  ): Future[codec.R] =
    fetchResponse(q) map codec.decode
}

