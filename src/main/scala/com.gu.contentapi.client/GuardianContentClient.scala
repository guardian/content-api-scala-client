package com.gu.contentapi.client

import com.gu.contentapi.client.model._
import com.gu.contentapi.client.parser.JsonParser
import com.gu.contentapi.client.utils.QueryStringParams
import dispatch.{FunctionHandler, Http}

import scala.concurrent.{ExecutionContext, Future}

case class GuardianContentApiError(httpStatus: Int, httpMessage: String) extends Exception(httpMessage)

trait ContentApiClientLogic {
  val apiKey: String

  protected val http = Http configure { _
    .setAllowPoolingConnection(true)
    .setMaximumConnectionsPerHost(10)
    .setMaximumConnectionsTotal(10)
    .setConnectionTimeoutInMs(1000)
    .setRequestTimeoutInMs(2000)
    .setCompressionEnabled(true)
    .setFollowRedirects(true)
    .setMaxConnectionLifeTimeInMs(60000) // to respect DNS TTLs
  }

  val targetUrl = "http://content.guardianapis.com"

  def item(id: String) = new ItemQuery(id)
  def search = new SearchQuery
  def tags = new TagsQuery
  def sections = new SectionsQuery
  def editions = new EditionsQuery

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
    val req = headers.foldLeft(dispatch.url(url)) {
      case (r, (name, value)) => r.setHeader(name, value)
    }
    def handler = new FunctionHandler(r => HttpResponse(r.getResponseBody("utf-8"), r.getStatusCode, r.getStatusText))
    http(req.toRequest, handler)
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

  def getResponse(editionsQuery: EditionsQuery)(implicit context: ExecutionContext): Future[EditionsResponse] =
    fetchResponse(editionsQuery) map JsonParser.parseEditions
}

class GuardianContentClient(val apiKey: String) extends ContentApiClientLogic
