package com.gu.contentapi.client

import cats.MonadError
import cats.implicits._
import com.gu.contentapi.client.model._
import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.utils.QueryStringParams
import com.gu.contentapi.buildinfo.CapiBuildInfo
import scala.util.Try
import com.gu.contentapi.client.thrift.ThriftDeserializer

case class GuardianContentApiError(httpStatus: Int, httpMessage: String, errorResponse: Option[ErrorResponse] = None) extends Exception(httpMessage)
case class HttpResponse(body: Array[Byte], statusCode: Int, statusMessage: String)

abstract class ContentApiClientLogic[F[_]](
  val apiKey: String,
  val targetUrl: String,
  protected val userAgent: String = "content-api-scala-client/"+CapiBuildInfo.version
)(implicit M: MonadError[F, Throwable]) {

  protected[client] def url(location: String, parameters: Map[String, String]): F[String] = {
    require(!location.contains('?'), "must not specify parameters in URL")

    M.pure(location + QueryStringParams(parameters + ("api-key" -> apiKey) + ("format" -> "thrift")))
  }

  protected def fetch(url: String): F[Array[Byte]] = {
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

  protected def get(url: String, headers: Map[String, String]): F[HttpResponse]

  def getUrl(contentApiQuery: ContentApiQuery): F[String] =
    url(s"$targetUrl/${contentApiQuery.pathSegment}", contentApiQuery.parameters)

  private def fetchResponse(contentApiQuery: ContentApiQuery): F[Array[Byte]] = 
    getUrl(contentApiQuery) >>= fetch

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

  def getResponse[Q <: ContentApiQuery](q: Q)(implicit codec: Codec[Q]): F[codec.R] =
    fetchResponse(q) map codec.decode
}

