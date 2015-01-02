package com.gu.contentapi.client

import java.net.URLEncoder

import com.gu.contentapi.client.model._
import com.gu.contentapi.client.parser.JsonParser
import dispatch.{FunctionHandler, Http}
import org.joda.time.ReadableInstant
import org.joda.time.format.ISODateTimeFormat

import scala.concurrent.{ExecutionContext, Future}

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

  def item = new ItemQuery
  def search = new SearchQuery
  def tags = new TagsQuery
  def sections = new SectionsQuery
  def collection = new CollectionQuery

  case class HttpResponse(body: String, statusCode: Int, statusMessage: String)

  protected[client] def url(location: String, parameters: Map[String, String]): String = {
    require(!location.contains('?'), "must not specify parameters in URL")

    def encodeParameter(p: Any): String = p match {
      case dt: ReadableInstant => URLEncoder.encode(ISODateTimeFormat.dateTimeNoMillis.print(dt), "UTF-8")
      case other => URLEncoder.encode(other.toString, "UTF-8")
    }

    val queryString = {
      val pairs = (parameters + ("api-key" -> apiKey)) map {
        case (k, v) => k + "=" + encodeParameter(v)
      }
      pairs mkString "&"
    }

    location + "?" + queryString
  }

  protected def fetch(location: String, parameters: Map[String, String]): Future[String] = {
    val headers = Map("User-Agent" -> "scala-client", "Accept" -> "application/json")

    for (response <- get(url(location, parameters), headers))
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

  def getResponse(itemQuery: ItemQuery): Future[ItemResponse] = {
    val location = itemQuery.id.getOrElse(throw new Exception("No API URL provided to item query, ensure apiUrl/itemId is called"))
    fetch(s"$targetUrl/$location", itemQuery.parameters) map JsonParser.parseItem
  }
  def getResponse(searchQuery: SearchQuery): Future[SearchResponse] =
    fetch(s"$targetUrl/search", searchQuery.parameters) map JsonParser.parseSearch
  def getResponse(tagsQuery: TagsQuery): Future[TagsResponse] =
    fetch(s"$targetUrl/tags", tagsQuery.parameters) map JsonParser.parseTags
  def getResponse(sectionsQuery: SectionsQuery): Future[SectionsResponse] =
    fetch(s"$targetUrl/sections", sectionsQuery.parameters) map JsonParser.parseSections
  def getResponse(collectionQuery: CollectionQuery): Future[CollectionResponse] = {
    val location = collectionQuery.collectionId.getOrElse(throw new Exception("No API URL provided to collection query, ensure apiUrl/collectionId is called"))
    fetch(s"$targetUrl/collections/$location", collectionQuery.parameters) map JsonParser.parseCollection
  }
}

class GuardianContentClient(val apiKey: String) extends ContentApiClientLogic
