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

trait ContentApiClient {
  def apiKey: String
  def userAgent: String
  def targetUrl: String

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

  def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse]

  def getUrl(contentApiQuery: ContentApiQuery): String =
    url(s"$targetUrl/${contentApiQuery.pathSegment}", contentApiQuery.parameters)

  private def fetchResponse(contentApiQuery: ContentApiQuery)(implicit context: ExecutionContext): Future[Array[Byte]] =
    fetch(getUrl(contentApiQuery))


  /* Exposed API */

  def getResponse(itemQuery: ItemQuery)(implicit context: ExecutionContext): Future[ItemResponse] =
    fetchResponse(itemQuery) map { response =>
      ThriftDeserializer.deserialize(response, ItemResponse)
    }

  def getResponse(searchQuery: SearchQueryBase[_])(implicit context: ExecutionContext): Future[SearchResponse] =
    fetchResponse(searchQuery) map { response =>
      ThriftDeserializer.deserialize(response, SearchResponse)
    }

  def getResponse(tagsQuery: TagsQuery)(implicit context: ExecutionContext): Future[TagsResponse] =
    fetchResponse(tagsQuery) map { response =>
      ThriftDeserializer.deserialize(response, TagsResponse)
    }

  def getResponse(sectionsQuery: SectionsQuery)(implicit context: ExecutionContext): Future[SectionsResponse] =
    fetchResponse(sectionsQuery) map { response =>
      ThriftDeserializer.deserialize(response, SectionsResponse)
    }

  def getResponse(editionsQuery: EditionsQuery)(implicit context: ExecutionContext): Future[EditionsResponse] =
    fetchResponse(editionsQuery) map { response =>
      ThriftDeserializer.deserialize(response, EditionsResponse)
    }

  def getResponse(removedContentQuery: RemovedContentQuery)(implicit context: ExecutionContext): Future[RemovedContentResponse] =
    fetchResponse(removedContentQuery) map { response =>
      ThriftDeserializer.deserialize(response, RemovedContentResponse)
    }

  def getResponse(videoStatsQuery: VideoStatsQuery)(implicit context: ExecutionContext): Future[VideoStatsResponse] =
    fetchResponse(videoStatsQuery) map { response =>
      ThriftDeserializer.deserialize(response, VideoStatsResponse)
    }

  def getResponse(atomsQuery: AtomsQuery)(implicit context: ExecutionContext): Future[AtomsResponse] =
    fetchResponse(atomsQuery) map { response =>
      ThriftDeserializer.deserialize(response, AtomsResponse)
    }

  def getResponse(recipesQuery: RecipesQuery)(implicit context: ExecutionContext): Future[AtomsResponse] =
    fetchResponse(recipesQuery) map { response =>
      ThriftDeserializer.deserialize(response, AtomsResponse)
    }

  def getResponse(reviewsQuery: ReviewsQuery)(implicit context: ExecutionContext): Future[AtomsResponse] =
    fetchResponse(reviewsQuery) map { response =>
      ThriftDeserializer.deserialize(response, AtomsResponse)
    }

  def getResponse(gameReviewsQuery: GameReviewsQuery)(implicit context: ExecutionContext): Future[AtomsResponse] =
    fetchResponse(gameReviewsQuery) map { response =>
      ThriftDeserializer.deserialize(response, AtomsResponse)
    }

  def getResponse(restaurantReviewsQuery: RestaurantReviewsQuery)(implicit context: ExecutionContext): Future[AtomsResponse] =
    fetchResponse(restaurantReviewsQuery) map { response =>
      ThriftDeserializer.deserialize(response, AtomsResponse)
    }

  def getResponse(filmReviewsQuery: FilmReviewsQuery)(implicit context: ExecutionContext): Future[AtomsResponse] =
    fetchResponse(filmReviewsQuery) map { response =>
      ThriftDeserializer.deserialize(response, AtomsResponse)
    }

  def getResponse(storiesQuery: StoriesQuery)(implicit context: ExecutionContext): Future[StoriesResponse] =
    fetchResponse(storiesQuery) map { response =>
      ThriftDeserializer.deserialize(response, StoriesResponse)
    }

}

class GuardianContentClient(val apiKey: String) extends ContentApiClient {

  val userAgent = "content-api-scala-client/"+CapiBuildInfo.version
  val targetUrl = "https://content.guardianapis.com"

  private val http = new OkHttpClient.Builder()
    .connectTimeout(1, TimeUnit.SECONDS)
    .readTimeout(2, TimeUnit.SECONDS)
    .followRedirects(true)
    .connectionPool(new ConnectionPool(10, 60, TimeUnit.SECONDS))
    .build()

  def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse] = {

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

  /** Shutdown the client and clean up all associated resources.
    *
    * Note: behaviour is undefined if you try to use the client after calling this method.
    */
  def shutdown(): Unit = http.dispatcher().executorService().shutdown()

}

