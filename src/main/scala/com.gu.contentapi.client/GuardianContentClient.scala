package com.gu.contentapi.client

import com.gu.contentapi.client.model._
import com.gu.contentapi.client.parser.JsonParser
import com.gu.contentapi.client.utils.QueryStringParams
import com.gu.contentapi.buildinfo.BuildInfo

import scala.concurrent.{ExecutionContext, Future}

case class GuardianContentApiError(httpStatus: Int, httpMessage: String) extends Exception(httpMessage)

case class HttpResponse(body: String, statusCode: Int, statusMessage: String)

trait HttpClient {
  def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse]
}

class GuardianContentClient(val apiKey: String, httpClient: HttpClient) {

  private val userAgent = "content-api-scala-client/"+BuildInfo.version

  val targetUrl = "http://content.guardianapis.com"

  def item(id: String) = ItemQuery(id)
  val search = SearchQuery()
  val tags = TagsQuery()
  val sections = new SectionsQuery()
  val editions = EditionsQuery()
  val removedContent = RemovedContentQuery()

  protected[client] def url(location: String, parameters: Map[String, String]): String = {
    require(!location.contains('?'), "must not specify parameters in URL")
    location + QueryStringParams(parameters + ("api-key" -> apiKey))
  }

  protected def fetch(url: String)(implicit context: ExecutionContext): Future[String] = {
    val headers = Map("User-Agent" -> userAgent, "Accept" -> "application/json")

    for (response <- httpClient.get(url, headers)) yield {
      if (List(200, 302) contains response.statusCode) response.body
      else throw new GuardianContentApiError(response.statusCode, response.statusMessage)
    }
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

  def getResponse(removedContentQuery: RemovedContentQuery)(implicit context: ExecutionContext): Future[RemovedContentResponse] =
    fetchResponse(removedContentQuery) map JsonParser.parseRemovedContent
}
