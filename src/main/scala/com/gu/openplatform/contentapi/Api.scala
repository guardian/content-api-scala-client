package com.gu.openplatform.contentapi


import connection.{JavaNetHttp, Http}
import java.net.URLEncoder
import com.gu.openplatform.contentapi.parser.JsonParser
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.ReadableInstant


// thrown when an "expected" error is thrown by the api
case class ApiError(httpStatus: Int, httpMessage: String)
        extends Exception(httpMessage)


abstract class Api extends Http with JsonParser {
  val targetUrl = "http://content.guardianapis.com"
  var apiKey: Option[String] = None

  def sections = new SectionsQuery
  def tags = new TagsQuery
  def folders = new FoldersQuery
  def search = new SearchQuery
  def item = new ItemQuery

  class FoldersQuery
    extends GeneralParameters[FoldersQuery]
    with FilterParameters[FoldersQuery] {
    lazy val response = parseFolders(fetch(targetUrl + "/folders", parameters))
  }

  object FoldersQuery {
    implicit def asResponse(q: FoldersQuery) = q.response
    implicit def asSections(q: FoldersQuery) = q.response.results
  }


  class SectionsQuery
    extends GeneralParameters[SectionsQuery]
    with FilterParameters[SectionsQuery] {
    lazy val response = parseSections(fetch(targetUrl + "/sections", parameters))
  }

  object SectionsQuery {
    implicit def asResponse(q: SectionsQuery) = q.response
    implicit def asSections(q: SectionsQuery) = q.response.results
  }



  class TagsQuery extends GeneralParameters[TagsQuery]
          with PaginationParameters[TagsQuery]
          with FilterParameters[TagsQuery]
          with RefererenceParameters[TagsQuery]
          with ShowReferenceParameters[TagsQuery] {
    val tagType = stringParameter("type")
    lazy val response = parseTags(fetch(targetUrl + "/tags", parameters))
  }

  object TagsQuery {
    implicit def asResponse(q: TagsQuery) = q.response
    implicit def asTags(q: TagsQuery) = q.response.results
  }



  class SearchQuery extends GeneralParameters[SearchQuery]
          with PaginationParameters[SearchQuery]
          with ShowParameters[SearchQuery]
          with RefinementParameters[SearchQuery]
          with FilterParameters[SearchQuery]
          with ContentFilterParameters[SearchQuery]
          with RefererenceParameters[SearchQuery]
          with ShowReferenceParameters[SearchQuery] {
    lazy val response = parseSearch(fetch(targetUrl + "/search", parameters))
  }

  object SearchQuery {
    implicit def asResponse(q: SearchQuery) = q.response
    implicit def asContent(q: SearchQuery) = q.response.results
  }




  class ItemQuery extends GeneralParameters[ItemQuery]
          with ShowParameters[ItemQuery]
          with ContentFilterParameters[ItemQuery]
          with PaginationParameters[ItemQuery]
          with ShowReferenceParameters[ItemQuery] {
    var _apiUrl: Option[String] = None

    def apiUrl(newContentPath: String): this.type = {
      require(newContentPath startsWith targetUrl, "apiUrl expects a full url; use itemId if you only have an id")
      _apiUrl = Some(newContentPath)
      this
    }

    def itemId(contentId: String): this.type = apiUrl(targetUrl + "/" + contentId)

    lazy val response = parseItem(
      fetch(
        _apiUrl.getOrElse(throw new Exception("No api url provided to item query, ensure withApiUrl is called")),
        parameters))
  }

  object ItemQuery {
    implicit def asResponse(q: ItemQuery) = q.response
  }



  trait GeneralParameters[OwnerType <: ParameterHolder] extends Parameters[OwnerType] {
    override def parameters = super.parameters ++ apiKey.map("api-key" -> _)
  }

  trait PaginationParameters[OwnerType <: ParameterHolder] extends Parameters[OwnerType] {
    val pageSize = intParameter("page-size")
    val page = intParameter("page")
  }

  trait FilterParameters[OwnerType <: ParameterHolder] extends Parameters[OwnerType] {
    val q = stringParameter("q")
    val section = stringParameter("section")
    val ids = stringParameter("ids")
    val tag = stringParameter("tag")
    val folder = stringParameter("folder")
  }

  trait ContentFilterParameters[OwnerType <: ParameterHolder] extends FilterParameters[OwnerType] {
    val orderBy = stringParameter("order-by")
    val fromDate = dateParameter("from-date")
    val toDate = dateParameter("to-date")
    val dateId = stringParameter("date-id")
    val useDate = stringParameter("use-date")
   }

  trait ShowParameters[OwnerType <: ParameterHolder] extends Parameters[OwnerType] {
    val showFields = stringParameter("show-fields")
    val showSnippets = stringParameter("show-snippets")
    val showTags = stringParameter("show-tags")
    val showFactboxes = stringParameter("show-factboxes")
    val showMedia = stringParameter("show-media")
    val showRelated = boolParameter("show-related")
    val showEditorsPicks = boolParameter("show-editors-picks")
    val edition = stringParameter("edition")
    val showMostViewed = boolParameter("show-most-viewed")
    val showStoryPackage = boolParameter("show-story-package")
    val showBestBets = boolParameter("show-best-bets")
    val snippetPre = stringParameter("snippet-pre")
    val snippetPost = stringParameter("snippet-post")
    val showInlineElements = stringParameter("show-inline-elements")
  }

  trait RefinementParameters[OwnerType <: ParameterHolder] extends Parameters[OwnerType] {
    val showRefinements = stringParameter("show-refinements")
    val refinementSize = intParameter("refinement-size")
  }

  trait RefererenceParameters[OwnerType <: ParameterHolder] extends Parameters[OwnerType] {
    val reference = stringParameter("reference")
    val referenceType = stringParameter("reference-type")
  }

  trait ShowReferenceParameters[OwnerType <: ParameterHolder] extends Parameters[OwnerType] {
    val showReferences = stringParameter("show-references")
  }



  protected def fetch(url: String, parameters: Map[String, Any] = Map.empty): String = {
    require(!url.contains('?'), "must not specify parameters in url")

    def encodeParameter(p: Any): String = p match {
      case dt: ReadableInstant => URLEncoder.encode(ISODateTimeFormat.dateTimeNoMillis.print(dt), "UTF-8")
      case other => URLEncoder.encode(other.toString, "UTF-8")
    }

    val queryString = parameters.map {case (k, v) => k + "=" + encodeParameter(v)}.mkString("&")
    val target = url + "?" + queryString

    val response = GET(target, List("User-Agent" -> "scala-api-client", "Accept" -> "application/json"))

    if (List(200, 302) contains response.statusCode) {
      response.body
    } else {
      throw new ApiError(response.statusCode, response.statusMessage)
    }
  }
}

object Api extends Api with JavaNetHttp
