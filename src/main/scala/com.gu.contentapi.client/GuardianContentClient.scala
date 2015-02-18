package com.gu.contentapi.client

import com.gu.contentapi.client.model._
import com.gu.contentapi.client.parser.JsonParser
import com.gu.contentapi.client.utils.QueryStringParams
import dispatch.{FunctionHandler, Http}
import com.ning.http.client._
import com.ning.http.client.providers.jdk._

import scala.concurrent.{ExecutionContext, Future}

case class GuardianContentApiError(httpStatus: Int, httpMessage: String) extends Exception(httpMessage)

trait ContentApiClientLogic {
  val apiKey: String

  protected val http = {
    val providerConfig = new JDKAsyncHttpProviderConfig()
    providerConfig.addProperty("bufferResponseInMemory", "true")

    val asyncClientConfig = new AsyncHttpClientConfig.Builder()
      .setAllowPoolingConnection(true)
      .setMaximumConnectionsPerHost(10)
      .setMaximumConnectionsTotal(10)
      .setConnectionTimeoutInMs(1000)
      .setRequestTimeoutInMs(2000)
      .setCompressionEnabled(true)
      .setFollowRedirects(true)
      .setAsyncHttpClientProviderConfig(providerConfig)
      .build()
    val asyncClient = new AsyncHttpClient(new JDKAsyncHttpProvider(asyncClientConfig), asyncClientConfig)

    Http(asyncClient)
  }

  val targetUrl = "http://content.guardianapis.com"

  def item(id: String) = new ItemQuery(id)
  def search = new SearchQuery
  def tags = new TagsQuery
  def sections = new SectionsQuery
  def collection(id: String) = new CollectionQuery(id)

  case class HttpResponse(body: String, statusCode: Int, statusMessage: String)

  protected[client] def url(location: String, parameters: Map[String, String]): String = {
    require(!location.contains('?'), "must not specify parameters in URL")
    location + QueryStringParams(parameters + ("api-key" -> apiKey))
  }

  protected def fetch(url: String)(implicit context: ExecutionContext): Future[String] = {
    val headers = Map("User-Agent" -> "scala-client", "Accept" -> "application/json")

    for (response <- get(url, headers)) yield {
      if (List(200, 302) contains response.statusCode) response.body
      else throw new GuardianContentApiError(response.statusCode, response.statusMessage)
    }
  }

  protected def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse] = {
    val req = dispatch.url(url)
    headers foreach {
      case (name, value) => req.setHeader(name, value)
    }
    val request = req.toRequest
    def handler = new FunctionHandler(r => HttpResponse(r.getResponseBody("utf-8"), r.getStatusCode, r.getStatusText))
    http(request, handler)
  }

  def getUrl(contentApiQuery: ContentApiQuery): String =
    url(s"$targetUrl/${contentApiQuery.pathSegment}", contentApiQuery.parameters)

  private def fetchResponse(contentApiQuery: ContentApiQuery)(implicit context: ExecutionContext): Future[String] =
    fetch(getUrl(contentApiQuery))

  def getResponse(itemQuery: ItemQuery)(implicit context: ExecutionContext): Future[ItemResponse] =
    fetchResponse(itemQuery) map JsonParser.parseItem

  def getResponse(searchQuery: SearchQuery)(implicit context: ExecutionContext): Future[SearchResponse] =
    fetchResponse(searchQuery) map JsonParser.parseSearch

  def getResponse(tagsQuery: TagsQuery)(implicit context: ExecutionContext): Future[TagsResponse] =
    fetchResponse(tagsQuery) map JsonParser.parseTags

  def getResponse(sectionsQuery: SectionsQuery)(implicit context: ExecutionContext): Future[SectionsResponse] =
    fetchResponse(sectionsQuery) map JsonParser.parseSections

  def getResponse(collectionQuery: CollectionQuery)(implicit context: ExecutionContext): Future[CollectionResponse] =
    fetchResponse(collectionQuery) map JsonParser.parseCollection
}

class GuardianContentClient(val apiKey: String) extends ContentApiClientLogic
