package com.gu.contentapi.client

import concurrent.{Future, ExecutionContext}
import java.net.URLEncoder

import org.joda.time.format.ISODateTimeFormat
import org.joda.time.ReadableInstant

import connection.{DispatchAsyncHttp, Http}
import com.gu.contentapi.client.parser.JsonParser
import model._

// thrown when an "expected" error is thrown by the api
case class ApiError(httpStatus: Int, httpMessage: String) extends Exception(httpMessage)

class Api(apiKey: String) extends DispatchAsyncHttp {
  implicit def executionContext = ExecutionContext.global

  val targetUrl = "http://content.guardianapis.com"

  def sections = new SectionsQuery
  def tags = new TagsQuery
  def search = new SearchQuery
  def item = new ItemQuery
  def collection = new CollectionQuery

  case class SectionsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
    extends GeneralParameters[SectionsQuery]
    with FilterParameters[SectionsQuery] {

    lazy val response: Future[SectionsResponse] = fetch(targetUrl + "/sections", parameters) map JsonParser.parseSections

    def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  }

  object SectionsQuery {
    implicit def asResponse(q: SectionsQuery) = q.response
    implicit def asSections(q: SectionsQuery) = q.response map (_.results)
  }

  case class TagsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
    extends GeneralParameters[TagsQuery]
          with PaginationParameters[TagsQuery]
          with FilterParameters[TagsQuery]
          with RefererenceParameters[TagsQuery]
          with ShowReferenceParameters[TagsQuery] {

    lazy val tagType = new StringParameter("type")
    lazy val response: Future[TagsResponse] = fetch(targetUrl + "/tags", parameters) map JsonParser.parseTags

    def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  }

  object TagsQuery {
    implicit def asResponse(q: TagsQuery) = q.response
    implicit def asTags(q: TagsQuery) = q.response map (_.results)
  }

  case class SearchQuery(parameterHolder: Map[String, Parameter] = Map.empty)
    extends GeneralParameters[SearchQuery]
          with PaginationParameters[SearchQuery]
          with ShowParameters[SearchQuery]
          with FilterParameters[SearchQuery]
          with ContentFilterParameters[SearchQuery]
          with RefererenceParameters[SearchQuery]
          with ShowReferenceParameters[SearchQuery] {

    lazy val response: Future[SearchResponse] = fetch(targetUrl + "/search", parameters) map JsonParser.parseSearch

    def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  }

  object SearchQuery {
    implicit def asResponse(q: SearchQuery) = q.response
    implicit def asContent(q: SearchQuery) = q.response map (_.results)
  }

  case class ItemQuery(path: Option[String] = None, parameterHolder: Map[String, Parameter] = Map.empty)
    extends GeneralParameters[ItemQuery]
          with ShowParameters[ItemQuery]
          with FilterParameters[ItemQuery]
          with ContentFilterParameters[ItemQuery]
          with PaginationParameters[ItemQuery]
          with ShowReferenceParameters[ItemQuery] {

    def apiUrl(newContentPath: String): ItemQuery = {
      require(newContentPath startsWith targetUrl, "apiUrl expects a full url; use itemId if you only have an id")
      copy(path = Some(newContentPath))
    }

    def itemId(contentId: String): ItemQuery = apiUrl(targetUrl + "/" + contentId)

    lazy val response: Future[ItemResponse] = fetch(
        path.getOrElse(throw new Exception("No api url provided to item query, ensure withApiUrl is called")),
        parameters) map JsonParser.parseItem

    def withParameters(parameterMap: Map[String, Parameter]) = copy(path, parameterMap)

  }

  object ItemQuery {
    implicit def asResponse(q: ItemQuery) = q.response
  }

  object CollectionQuery {
    implicit def asResponse(q: CollectionQuery) = q.response
    implicit def asCollection(q: CollectionQuery) = q.response map (_.collection)
  }

  case class CollectionQuery(path: Option[String] = None, parameterHolder: Map[String, Parameter] = Map.empty)
    extends GeneralParameters[CollectionQuery]
    with ShowParameters[CollectionQuery]
    with FilterParameters[CollectionQuery]
    with PaginationParameters[CollectionQuery]
    with ShowReferenceParameters[CollectionQuery] {

    def apiUrl(newContentPath: String): CollectionQuery = {
      require(newContentPath startsWith targetUrl, "apiUrl expects a full url; use itemId if you only have an id")
      copy(path = Some(newContentPath))
    }

    def itemId(collectionId: String): CollectionQuery = apiUrl(targetUrl + "/collections/" + collectionId)

    lazy val response: Future[CollectionResponse] = fetch(
        path.getOrElse(throw new Exception("No api url provided to collection query, ensure withApiUrl is called")),
        parameters) map JsonParser.parseCollection

    def withParameters(parameterMap: Map[String, Parameter]) = copy(path, parameterMap)
  }

  trait GeneralParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    override def parameters = super.parameters + ("api-key" -> apiKey)
  }

  trait PaginationParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def pageSize = IntParameter("page-size")
    def page = IntParameter("page")
  }

  trait FilterParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def q = StringParameter("q")
    def section = StringParameter("section")
    def ids = StringParameter("ids")
    def tag = StringParameter("tag")
  }

  trait ContentFilterParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def orderBy = StringParameter("order-by")
    def fromDate = DateParameter("from-date")
    def toDate = DateParameter("to-date")
    def useDate = StringParameter("use-date")
   }

  trait ShowParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def showFields = StringParameter("show-fields")
    def showTags = StringParameter("show-tags")
    def showElements = StringParameter("show-elements")
    def showRelated = BoolParameter("show-related")
    def showEditorsPicks = BoolParameter("show-editors-picks")
    def edition = StringParameter("edition")
    def showMostViewed = BoolParameter("show-most-viewed")
    def showStoryPackage = BoolParameter("show-story-package")
  }

  trait RefererenceParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def reference = StringParameter("reference")
    def referenceType = StringParameter("reference-type")
  }

  trait ShowReferenceParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def showReferences = StringParameter("show-references")
  }


  protected def fetch(url: String, parameters: Map[String, String]): Future[String] = {
    require(!url.contains('?'), "must not specify parameters in url")

    def encodeParameter(p: Any): String = p match {
      case dt: ReadableInstant => URLEncoder.encode(ISODateTimeFormat.dateTimeNoMillis.print(dt), "UTF-8")
      case other => URLEncoder.encode(other.toString, "UTF-8")
    }

    val queryString = parameters.map {case (k, v) => k + "=" + encodeParameter(v)}.mkString("&")
    val target = url + "?" + queryString

    for {
      response <- GET(target, List("User-Agent" -> "scala-api-client", "Accept" -> "application/json"))
    } yield if (List(200, 302) contains response.statusCode)
        response.body
      else
        throw new ApiError(response.statusCode, response.statusMessage)
  }
}
