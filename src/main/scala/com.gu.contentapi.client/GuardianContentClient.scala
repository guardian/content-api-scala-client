package com.gu.contentapi.client

import com.gu.contentapi.client.model._
import com.gu.contentapi.client.parser.JsonParser
import com.gu.contentapi.client.utils.{QueryStringParams, Futures}
import dispatch.{FunctionHandler, Http}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class GuardianContentApiError(httpStatus: Int, httpMessage: String) extends Exception(httpMessage)

trait ContentApiClientLogic {

  val apiKey: String

  implicit def executionContext = ExecutionContext.global

  protected val http = Http configure { _
    .setAllowPoolingConnection(true)
    .setMaximumConnectionsPerHost(10)
    .setMaximumConnectionsTotal(10)
    .setConnectionTimeoutInMs(1000)
    .setRequestTimeoutInMs(2000)
    .setCompressionEnabled(true)
    .setFollowRedirects(true)
  }

  val targetUrl = "http://content.guardianapis.com"

  def item(id: String) = new ItemQuery(id)
  def search = new SearchQuery
  def tags = new TagsQuery
  def sections = new SectionsQuery
  def collection = new CollectionQuery

  case class HttpResponse(body: String, statusCode: Int, statusMessage: String)

  protected[client] def url(location: String, parameters: Map[String, String]): String = {
    require(!location.contains('?'), "must not specify parameters in URL")
    location + "?" + QueryStringParams(parameters + ("api-key" -> apiKey))
  }

  protected def fetch(url: String): Future[String] = {
    val headers = Map("User-Agent" -> "scala-client", "Accept" -> "application/json")

    for (response <- get(url, headers))
    yield {
      if (List(200, 302) contains response.statusCode) response.body
      else throw new GuardianContentApiError(response.statusCode, response.statusMessage)
    }
  }

  protected def get(url: String, headers: Map[String, String]): Future[HttpResponse] = {
    val req = dispatch.url(url)
    headers foreach {
      case (name, value) => req.setHeader(name, value)
    }
    val request = req.toRequest
    def handler = new FunctionHandler(r => HttpResponse(r.getResponseBody("utf-8"), r.getStatusCode, r.getStatusText))
    http(request, handler)
  }

  def getUrl(contentApiQuery: ContentApiQuery): Try[String] = Try {
    contentApiQuery match {
      case itemQuery: ItemQuery =>
        val location = itemQuery.id
        url(s"$targetUrl/$location", itemQuery.parameters)

      case searchQuery: SearchQuery =>
        url(s"$targetUrl/search", searchQuery.parameters)

      case tagsQuery: TagsQuery =>
        url(s"$targetUrl/tags", tagsQuery.parameters)

      case sectionsQuery: SectionsQuery =>
        url(s"$targetUrl/sections", sectionsQuery.parameters)

      case collectionQuery: CollectionQuery =>
        val location = collectionQuery.collectionId.getOrElse(throw new Exception("No API URL provided to collection query, ensure apiUrl/collectionId is called"))
        url(s"$targetUrl/collections/$location", collectionQuery.parameters)
    }
  }

  private def fetchResponse(contentApiQuery: ContentApiQuery): Future[String] = for {
    url <- Futures.fromTry(getUrl(contentApiQuery))
    body <- fetch(url)
  } yield body

  def getResponse(itemQuery: ItemQuery): Future[ItemResponse] =
    fetchResponse(itemQuery) map JsonParser.parseItem

  def getResponse(searchQuery: SearchQuery): Future[SearchResponse] =
    fetchResponse(searchQuery) map JsonParser.parseSearch

  def getResponse(tagsQuery: TagsQuery): Future[TagsResponse] =
    fetchResponse(tagsQuery) map JsonParser.parseTags

  def getResponse(sectionsQuery: SectionsQuery): Future[SectionsResponse] =
    fetchResponse(sectionsQuery) map JsonParser.parseSections

  def getResponse(collectionQuery: CollectionQuery): Future[CollectionResponse] =
    fetchResponse(collectionQuery) map JsonParser.parseCollection
}

class GuardianContentClient(val apiKey: String) extends ContentApiClientLogic
