package com.gu.contentapi.client

import scala.concurrent.{Future, ExecutionContext}
import java.net.URLEncoder
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.ReadableInstant
import dispatch.{Http, FunctionHandler}
import com.gu.contentapi.client.parser.JsonParser
import com.gu.contentapi.client.model._

case class GuardianContentApiError(httpStatus: Int, httpMessage: String) extends Exception(httpMessage)

class GuardianContentClient(apiKey: String) {

  implicit def executionContext = ExecutionContext.global

  private val http = Http configure { _
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

  case class ItemQuery(path: Option[String] = None, parameterHolder: Map[String, Parameter] = Map.empty)
      extends KeyParameters[ItemQuery]
      with EditionParameters[ItemQuery]
      with ContentParameters[ItemQuery]
      with ShowParameters[ItemQuery]
      with ShowReferencesParameters[ItemQuery]
      with ShowExtendedParameters[ItemQuery]
      with PaginationParameters[ItemQuery]
      with OrderingParameters[ItemQuery]
      with FilterParameters[ItemQuery]
      with FilterExtendedParameters[ItemQuery]
      with FilterSearchParameters[ItemQuery] {

    def apiUrl(newContentPath: String): ItemQuery = {
      require(newContentPath startsWith targetUrl, "apiUrl expects a full URL, use itemId if you only have an ID")
      copy(path = Some(newContentPath))
    }

    def itemId(contentId: String): ItemQuery = {
      apiUrl(targetUrl + "/" + contentId)
    }

    lazy val response: Future[ItemResponse] = {
      val location = path.getOrElse(throw new Exception("No API URL provided to item query, ensure withApiUrl is called"))
      fetch(location, parameters) map JsonParser.parseItem
    }

    def withParameters(parameterMap: Map[String, Parameter]) = copy(path, parameterMap)

  }

  object ItemQuery {
    implicit def asResponse(q: ItemQuery) = q.response
  }

  case class SearchQuery(parameterHolder: Map[String, Parameter] = Map.empty)
      extends KeyParameters[SearchQuery]
      with ContentParameters[SearchQuery]
      with ShowParameters[SearchQuery]
      with ShowReferencesParameters[SearchQuery]
      with OrderingParameters[SearchQuery]
      with PaginationParameters[SearchQuery]
      with FilterParameters[SearchQuery]
      with FilterExtendedParameters[SearchQuery]
      with FilterSearchParameters[SearchQuery] {

    lazy val response: Future[SearchResponse] = {
      fetch(targetUrl + "/search", parameters) map JsonParser.parseSearch
    }

    def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  }

  object SearchQuery {
    implicit def asResponse(q: SearchQuery) = q.response
    implicit def asContent(q: SearchQuery) = q.response.map(_.results)
  }

  case class TagsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
      extends KeyParameters[TagsQuery]
      with ShowReferencesParameters[TagsQuery]
      with PaginationParameters[TagsQuery]
      with FilterParameters[TagsQuery]
      with FilterTagParameters[TagsQuery]
      with FilterSearchParameters[TagsQuery] {

    lazy val response: Future[TagsResponse] = {
      fetch(targetUrl + "/tags", parameters) map JsonParser.parseTags
    }

    def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  }

  object TagsQuery {
    implicit def asResponse(q: TagsQuery) = q.response
    implicit def asTags(q: TagsQuery) = q.response.map(_.results)
  }

  case class SectionsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
      extends KeyParameters[SectionsQuery]
      with FilterSearchParameters[SectionsQuery] {

    lazy val response: Future[SectionsResponse] = {
      fetch(targetUrl + "/sections", parameters) map JsonParser.parseSections
    }

    def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  }

  object SectionsQuery {
    implicit def asResponse(q: SectionsQuery) = q.response
    implicit def asSections(q: SectionsQuery) = q.response.map(_.results)
  }

  case class CollectionQuery(path: Option[String] = None, parameterHolder: Map[String, Parameter] = Map.empty)
      extends KeyParameters[CollectionQuery]
      with ShowParameters[CollectionQuery]
      with ShowReferencesParameters[CollectionQuery]
      with FilterParameters[CollectionQuery] {

    def apiUrl(newContentPath: String): CollectionQuery = {
      require(newContentPath startsWith targetUrl, "apiUrl expects a full URI: use collectionId if you only have an ID")
      copy(path = Some(newContentPath))
    }

    def collectionId(collectionId: String): CollectionQuery = {
      apiUrl(targetUrl + "/collections/" + collectionId)
    }

    lazy val response: Future[CollectionResponse] = {
      val location = path.getOrElse(throw new Exception("No API URL provided to collection query, ensure withApiUrl is called"))
      fetch(location, parameters) map JsonParser.parseCollection
    }

    def withParameters(parameterMap: Map[String, Parameter]) = copy(path, parameterMap)

  }

  object CollectionQuery {
    implicit def asResponse(q: CollectionQuery) = q.response
    implicit def asCollection(q: CollectionQuery) = q.response.map(_.collection)
  }

  trait KeyParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    override def parameters = super.parameters + ("api-key" -> apiKey)
  }

  trait ContentParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def contentSet = StringParameter("content-set")
  }

  trait EditionParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def edition = StringParameter("edition")
  }

  trait ShowParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def showFields = StringParameter("show-fields")
    def showTags = StringParameter("show-tags")
    def showElements = StringParameter("show-elements")
    def showRights = StringParameter("show-rights")
  }

  trait ShowReferencesParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def showReferences = StringParameter("show-references")
  }

  trait ShowExtendedParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def showStoryPackage = BoolParameter("show-story-package")
    def showRelated = BoolParameter("show-related")
    def showMostViewed = BoolParameter("show-most-viewed")
    def showEditorsPicks = BoolParameter("show-editors-picks")
  }

  trait PaginationParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def page = IntParameter("page")
    def pageSize = IntParameter("page-size")
  }

  trait OrderingParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def orderBy = StringParameter("order-by")
    def useDate = StringParameter("use-date")
  }

  trait FilterParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def section = StringParameter("section")
    def reference = StringParameter("reference")
    def referenceType = StringParameter("reference-type")
    def productionOffice = StringParameter("production-office")
  }

  trait FilterExtendedParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def tag = StringParameter("tag")
    def ids = StringParameter("ids")
    def rights = StringParameter("rights")
    def leadContent = StringParameter("lead-content")
    def fromDate = DateParameter("from-date")
    def toDate = DateParameter("to-date")
  }

  trait FilterTagParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def tagType = StringParameter("tag-type")
  }

  trait FilterSearchParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def q = StringParameter("q")
  }

  case class HttpResponse(body: String, statusCode: Int, statusMessage: String)

  private def fetch(location: String, parameters: Map[String, String]): Future[String] = {
    require(!location.contains('?'), "must not specify parameters in URL")

    def encodeParameter(p: Any): String = p match {
      case dt: ReadableInstant => URLEncoder.encode(ISODateTimeFormat.dateTimeNoMillis.print(dt), "UTF-8")
      case other => URLEncoder.encode(other.toString, "UTF-8")
    }

    val queryString = {
      val pairs = parameters map {
        case (k, v) => k + "=" + encodeParameter(v)
      }
      pairs mkString "&"
    }

    val url = location + "?" + queryString
    val headers = Map("User-Agent" -> "scala-client", "Accept" -> "application/json")

    for (response <- get(url, headers))
    yield if (List(200, 302) contains response.statusCode) response.body
    else throw new GuardianContentApiError(response.statusCode, response.statusMessage)
  }

  private def get(url: String, headers: Map[String, String]): Future[HttpResponse] = {
    val req = dispatch.url(url)
    headers foreach {
      case (name, value) => req.setHeader(name, value)
    }
    val request = req.toRequest
    def handler = new FunctionHandler(r => HttpResponse(r.getResponseBody("utf-8"), r.getStatusCode, r.getStatusText))
    http(request, handler)
  }

}
