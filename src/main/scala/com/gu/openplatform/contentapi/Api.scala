package com.gu.openplatform.contentapi

import connection.{Http, JavaNetSyncHttp}
import java.net.URLEncoder
import com.gu.openplatform.contentapi.parser.JsonParser
import model._
import model.FoldersResponse
import model.SearchResponse
import model.SectionsResponse
import model.TagsResponse
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.ReadableInstant
import util.{Monad, MonadOps, Id}
import MonadOps._


// thrown when an "expected" error is thrown by the api
case class ApiError(httpStatus: Int, httpMessage: String)
        extends Exception(httpMessage)

trait SyncApi extends Api[Id]

abstract class Api[F[_] : Monad] extends Http[F] with JsonParser {
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

    lazy val response: F[FoldersResponse] = fetch(targetUrl + "/folders", parameters) map parseFolders
    lazy val results: F[List[Folder]] = response map (_.results)
  }


  class SectionsQuery
    extends GeneralParameters[SectionsQuery]
    with FilterParameters[SectionsQuery] {

    lazy val response: F[SectionsResponse] = fetch(targetUrl + "/sections", parameters) map parseSections
    lazy val results: F[List[Section]] = response map (_.results)
  }

  class TagsQuery extends GeneralParameters[TagsQuery]
          with PaginationParameters[TagsQuery]
          with FilterParameters[TagsQuery]
          with RefererenceParameters[TagsQuery]
          with ShowReferenceParameters[TagsQuery] {

    lazy val tagType = new StringParameter(self, "type")
    lazy val response: F[TagsResponse] = fetch(targetUrl + "/tags", parameters) map parseTags
    lazy val results: F[List[Tag]] = response map (_.results)
  }


  class SearchQuery extends GeneralParameters[SearchQuery]
          with PaginationParameters[SearchQuery]
          with ShowParameters[SearchQuery]
          with RefinementParameters[SearchQuery]
          with FilterParameters[SearchQuery]
          with ContentFilterParameters[SearchQuery]
          with RefererenceParameters[SearchQuery]
          with ShowReferenceParameters[SearchQuery] {

    lazy val response: F[SearchResponse] = fetch(targetUrl + "/search", parameters) map parseSearch
    lazy val results: F[List[Content]] = response map (_.results)
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

    lazy val response: F[ItemResponse] = fetch(
        _apiUrl.getOrElse(throw new Exception("No api url provided to item query, ensure withApiUrl is called")),
        parameters) map parseItem
    lazy val results: F[List[Content]] = response map (_.results)
  }

  trait GeneralParameters[Owner] extends Parameters[Owner] { this: Owner =>
    final def self = this
    override def parameters = super.parameters ++ apiKey.map("api-key" -> _)
  }

  trait PaginationParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] {
    lazy val pageSize = new IntParameter(self, "page-size")
    lazy val page = new IntParameter(self, "page")
  }

  trait FilterParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] {
    lazy val q = new StringParameter(self, "q")
    lazy val section = new StringParameter(self, "section")
    lazy val ids = new StringParameter(self, "ids")
    lazy val tag = new StringParameter(self, "tag")
    lazy val folder = new StringParameter(self, "folder")
  }

  trait ContentFilterParameters[Owner <: Parameters[Owner]] extends FilterParameters[Owner] {
    lazy val orderBy = new StringParameter(self, "order-by")
    lazy val fromDate = new DateParameter(self, "from-date")
    lazy val toDate = new DateParameter(self, "to-date")
    lazy val dateId = new StringParameter(self, "date-id")
    lazy val useDate = new StringParameter(self, "use-date")
   }

  trait ShowParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] {
    lazy val showFields = new StringParameter(self, "show-fields")
    lazy val showSnippets = new StringParameter(self, "show-snippets")
    lazy val showTags = new StringParameter(self, "show-tags")
    lazy val showFactboxes = new StringParameter(self, "show-factboxes")
    lazy val showMedia = new StringParameter(self, "show-media")
    lazy val showRelated = new BoolParameter(self, "show-related")
    lazy val showEditorsPicks = new BoolParameter(self, "show-editors-picks")
    lazy val edition = new StringParameter(self, "edition")
    lazy val showMostViewed = new BoolParameter(self, "show-most-viewed")
    lazy val showStoryPackage = new BoolParameter(self, "show-story-package")
    lazy val showBestBets = new BoolParameter(self, "show-best-bets")
    lazy val snippetPre = new StringParameter(self, "snippet-pre")
    lazy val snippetPost = new StringParameter(self, "snippet-post")
    lazy val showInlineElements = new StringParameter(self, "show-inline-elements")
    lazy val showExpired = new BoolParameter(self, "show-expired")
  }

  trait RefinementParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] {
    lazy val showRefinements = new StringParameter(self, "show-refinements")
    lazy val refinementSize = new IntParameter(self, "refinement-size")
  }

  trait RefererenceParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] {
    lazy val reference = new StringParameter(self, "reference")
    lazy val referenceType = new StringParameter(self, "reference-type")
  }

  trait ShowReferenceParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] {
    lazy val showReferences = new StringParameter(self, "show-references")
  }



  protected def fetch(url: String, parameters: Map[String, Any] = Map.empty): F[String] = {
    require(!url.contains('?'), "must not specify parameters in url")

    def encodeParameter(p: Any): String = p match {
      case dt: ReadableInstant => URLEncoder.encode(ISODateTimeFormat.dateTimeNoMillis.print(dt), "UTF-8")
      case other => URLEncoder.encode(other.toString, "UTF-8")
    }

    val queryString = parameters.map {case (k, v) => k + "=" + encodeParameter(v)}.mkString("&")
    val target = url + "?" + queryString

    for {
      response <- GET(target, List("User-Agent" -> "scala-api-client", "Accept" -> "application/json"))
      body <-
        if (List(200, 302) contains response.statusCode) point(response.body)
        else fail(new ApiError(response.statusCode, response.statusMessage))
    } yield body

  }
}

// Default client instance, based on java.net client
object Api extends SyncApi with JavaNetSyncHttp
