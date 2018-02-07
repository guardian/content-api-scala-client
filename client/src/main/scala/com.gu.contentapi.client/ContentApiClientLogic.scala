package com.gu.contentapi.client

import com.gu.computation.{MonadError, Monoid}
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
  private val headers = Map("User-Agent" -> userAgent, "Accept" -> "application/x-thrift")
  
  private def contentApiError(response: HttpResponse): GuardianContentApiError = {
    val errorResponse = Try(ThriftDeserializer.deserialize(response.body, ErrorResponse)).toOption
    GuardianContentApiError(response.statusCode, response.statusMessage, errorResponse)
  }

  private def isValidResponse(r: HttpResponse): Boolean = 
    List(200, 302) contains r.statusCode

  private def fetchResponse(contentApiQuery: ContentApiQuery): F[Array[Byte]] = 
    M.flatMap(getUrl(contentApiQuery))(fetch)

  protected[client] def url(location: String, parameters: Map[String, String]): F[String] = Try {
    require(!location.contains('?'), "must not specify parameters in URL")

    M.pure(location + QueryStringParams(parameters + ("api-key" -> apiKey) + ("format" -> "thrift")))
  }.recover { 
    case t: Throwable => M.throwError(t): F[String]
  }.get

  protected def fetch(url: String): F[Array[Byte]] =
    M.map(M.flatMap(get(url, headers)) { r =>
      if (!isValidResponse(r))
        M.throwError(contentApiError(r)): F[HttpResponse]
      else
        M.pure(r)
    })(_.body)

  protected def get(url: String, headers: Map[String, String]): F[HttpResponse]

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

  def getUrl(contentApiQuery: ContentApiQuery): F[String] =
    url(s"$targetUrl/${contentApiQuery.pathSegment}", contentApiQuery.parameters)

  def getResponse[Q <: ContentApiQuery](q: Q)(implicit codec: Codec[Q]): F[codec.R] =
    M.map(fetchResponse(q))(codec.decode)
  
  def paginate[Q <: ContentApiQuery](q: Q)(f: SearchResponse => F[Unit])(implicit codec: Codec[Q] { type R = SearchResponse }): F[Unit] =
    M.flatMap(getResponse(q))(paginate2(q, f))

  private def paginate2[Q <: ContentApiQuery](q: Q, f: SearchResponse => F[Unit])(r: SearchResponse): F[Unit] =
    M.flatMap(f(r)){ _ =>
      (r.pages == r.currentPage, r.results.lastOption.map(_.id)) match {
        case (false, Some(id)) => M.flatMap(getResponse(NextQuery(q, id)))(paginate2(q, f))
        case _                 => M.pure(())
      }
    }

  def paginateMap[Q <: ContentApiQuery, RR : Monoid](q: Q)(f: SearchResponse => F[RR])(implicit codec: Codec[Q] { type R = SearchResponse }): F[RR] =
    M.flatMap(getResponse(q))(paginateMap2(q, f))

  private def paginateMap2[Q <: ContentApiQuery, RR](q: Q, f: SearchResponse => F[RR])(r: SearchResponse)(implicit MO: Monoid[RR]): F[RR] = 
    M.flatMap(f(r)){ ra =>
      M.map((r.pages == r.currentPage, r.results.lastOption.map(_.id)) match {
        case (false, Some(id)) => M.flatMap(getResponse(NextQuery(q, id)))(paginateMap2(q, f))
        case _                 => M.pure(MO.mempty)
      }) { rb =>
        MO.append(ra, rb)
      }
    }
}

