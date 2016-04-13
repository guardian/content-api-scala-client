package com.gu.contentapi.client

import java.nio.charset.StandardCharsets

import com.gu.contentapi.client.model._
import com.gu.contentapi.client.model.v1.SearchResponse
import com.gu.contentapi.client.model.v1.ErrorResponse
import com.gu.contentapi.client.model.v1.ItemResponse
import com.gu.contentapi.client.model.v1.TagsResponse
import com.gu.contentapi.client.model.v1.EditionsResponse
import com.gu.contentapi.client.model.v1.SectionsResponse
import com.gu.contentapi.client.model.v1.RemovedContentResponse
import com.gu.contentapi.client.model.v1.VideoStatsResponse

import com.gu.contentapi.client.parser.JsonParser

import com.gu.contentapi.client.utils.QueryStringParams
import com.ning.http.client.AsyncHttpClientConfig.Builder
import com.ning.http.client.AsyncHttpClient
import dispatch.{FunctionHandler, Http}
import com.gu.contentapi.buildinfo.CapiBuildInfo
import org.apache.thrift.transport.TTransportException

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import com.gu.contentapi.client.parser.ThriftDeserializer

case class GuardianContentApiError(httpStatus: Int, httpMessage: String, errorResponse: Option[ErrorResponse] = None) extends Exception(httpMessage)

trait ContentApiClientLogic {
  val apiKey: String
  def useThrift: Boolean

  protected val userAgent = "content-api-scala-client/"+CapiBuildInfo.version

  protected lazy val http = {
    /*
    Warning: do not call `Http.configure(...)` because it leaks resources!
    See https://github.com/dispatch/reboot/pull/115
     */
    val config = new Builder()
      .setAllowPoolingConnections(true)
      .setMaxConnectionsPerHost(10)
      .setMaxConnections(10)
      .setConnectTimeout(1000)
      .setRequestTimeout(2000)
      .setCompressionEnforced(true)
      .setFollowRedirect(true)
      .setUserAgent(userAgent)
      .setConnectionTTL(60000) // to respect DNS TTLs
      .build()
    Http(new AsyncHttpClient(config))
  }

  val targetUrl = "http://content.guardianapis.com"

  def item(id: String) = ItemQuery(id)
  val search = SearchQuery()
  val tags = TagsQuery()
  val sections = new SectionsQuery()
  val editions = EditionsQuery()
  val removedContent = RemovedContentQuery()

  case class HttpResponse(body: Array[Byte], statusCode: Int, statusMessage: String)

  protected[client] def url(location: String, parameters: Map[String, String]): String = {
    require(!location.contains('?'), "must not specify parameters in URL")

    val format = if (useThrift) "thrift" else "json"
    location + QueryStringParams(parameters + ("api-key" -> apiKey) + ("format" -> format))
  }

  protected def fetch(url: String)(implicit context: ExecutionContext): Future[Array[Byte]] = {

      val contentType = if (useThrift) Map("Accept" -> "application/x-thrift") else Map("Accept" -> "application/json")
      val headers = Map("User-Agent" -> userAgent) ++ contentType

    for (response <- get(url, headers)) yield {
      if (List(200, 302) contains response.statusCode) response.body
      else throw contentApiError(response)
    }
  }

  private def contentApiError(response: HttpResponse): GuardianContentApiError = {
    if (useThrift) {
      val errorResponse = Try(ThriftDeserializer.deserialize(response.body, ErrorResponse)).toOption
      GuardianContentApiError(response.statusCode, response.statusMessage, errorResponse)
    }
    else GuardianContentApiError(response.statusCode, response.statusMessage, JsonParser.parseErrorThrift(new String(response.body, "UTF-8")))
  }

  protected def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse] = {

    val req = headers.foldLeft(dispatch.url(url)) {
      case (r, (name, value)) => r.setHeader(name, value)
    }
    def handler = new FunctionHandler(r => HttpResponse(r.getResponseBodyAsBytes, r.getStatusCode, r.getStatusText))
    http(req.toRequest, handler)
  }

  def getUrl(contentApiQuery: ContentApiQuery): String =
    url(s"$targetUrl/${contentApiQuery.pathSegment}", contentApiQuery.parameters)

  private def fetchResponse(contentApiQuery: ContentApiQuery)(implicit context: ExecutionContext): Future[Array[Byte]] =
    fetch(getUrl(contentApiQuery))


  /* Exposed API */

  def getResponse(itemQuery: ItemQuery)(implicit context: ExecutionContext): Future[ItemResponse] =
    fetchResponse(itemQuery) map { response =>
      if (useThrift) ThriftDeserializer.deserialize(response, ItemResponse)
      else JsonParser.parseItemThrift(new String(response, StandardCharsets.UTF_8))
    }

  def getResponse(searchQuery: SearchQuery)(implicit context: ExecutionContext): Future[SearchResponse] =
    fetchResponse(searchQuery) map { response =>
      if (useThrift) ThriftDeserializer.deserialize(response, SearchResponse)
      else JsonParser.parseSearchThrift(new String(response, StandardCharsets.UTF_8))
    }

  def getResponse(tagsQuery: TagsQuery)(implicit context: ExecutionContext): Future[TagsResponse] =
    fetchResponse(tagsQuery) map { response =>
      if (useThrift) ThriftDeserializer.deserialize(response, TagsResponse)
      else JsonParser.parseTagsThrift(new String(response, StandardCharsets.UTF_8))
    }

  def getResponse(sectionsQuery: SectionsQuery)(implicit context: ExecutionContext): Future[SectionsResponse] =
    fetchResponse(sectionsQuery) map { response =>
      if (useThrift) ThriftDeserializer.deserialize(response, SectionsResponse)
      else JsonParser.parseSectionsThrift(new String(response, StandardCharsets.UTF_8))
    }

  def getResponse(editionsQuery: EditionsQuery)(implicit context: ExecutionContext): Future[EditionsResponse] =
    fetchResponse(editionsQuery) map { response =>
      if (useThrift) ThriftDeserializer.deserialize(response, EditionsResponse)
      else JsonParser.parseEditionsThrift(new String(response, StandardCharsets.UTF_8))
    }

  def getResponse(removedContentQuery: RemovedContentQuery)(implicit context: ExecutionContext): Future[RemovedContentResponse] =
    fetchResponse(removedContentQuery) map { response =>
      if (useThrift) ThriftDeserializer.deserialize(response, RemovedContentResponse)
      else JsonParser.parseRemovedContentThrift(new String(response, StandardCharsets.UTF_8))
    }

  def getResponse(videoStatsQuery: VideoStatsQuery)(implicit context: ExecutionContext): Future[VideoStatsResponse] =
    fetchResponse(videoStatsQuery) map { response =>
      if (useThrift) ThriftDeserializer.deserialize(response, VideoStatsResponse)
      else JsonParser.parseVideoStatsThrift(new String(response, StandardCharsets.UTF_8))
    }

  /**
   * Shutdown the client and clean up all associated resources.
   *
   * Note: behaviour is undefined if you try to use the client after calling this method.
   */
  def shutdown(): Unit = http.shutdown()

}

class GuardianContentClient(val apiKey: String, val useThrift: Boolean = false) extends ContentApiClientLogic


