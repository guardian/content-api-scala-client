package com.gu.openplatform.contentapi

import concurrent.{Future, ExecutionContext}
import java.net.URLEncoder

import org.joda.time.format.ISODateTimeFormat
import org.joda.time.ReadableInstant

import connection.{DispatchAsyncHttp, Http, JavaNetSyncHttp}
import com.gu.openplatform.contentapi.parser.JsonParser
import model._
import util._


// thrown when an "expected" error is thrown by the api
case class ApiError(httpStatus: Int, httpMessage: String)
        extends Exception(httpMessage)

trait Api[F[_]] extends Http[F] with JsonParser {
  import MonadOps._

  /** Proof that we can call point, map, flatMap and error for type F */
  implicit def M: Monad[F]

  val targetUrl = "http://content.guardianapis.com"
  var apiKey: Option[String] = None

  def sections = new SectionsQuery
  def tags = new TagsQuery
  def folders = new FoldersQuery
  def search = new SearchQuery
  def item = new ItemQuery

  case class FoldersQuery(parameters: Map[String, Parameter] = Map.empty)
    extends GeneralParameters[FoldersQuery]
    with FilterParameters[FoldersQuery] {

    lazy val response: F[FoldersResponse] = fetch(targetUrl + "/folders", queryParameters) map parseFolders

    def updated(parameterMap: Map[String, Parameter]) = copy(parameterMap)
  }

  object FoldersQuery {
    implicit def asResponse(q: FoldersQuery) = q.response
    implicit def asFolders(q: FoldersQuery) = q.response map (_.results)
  }

  case class SectionsQuery(parameters: Map[String, Parameter] = Map.empty)
    extends GeneralParameters[SectionsQuery]
    with FilterParameters[SectionsQuery] {

    lazy val response: F[SectionsResponse] = fetch(targetUrl + "/sections", queryParameters) map parseSections

    def updated(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  }

  object SectionsQuery {
    implicit def asResponse(q: SectionsQuery) = q.response
    implicit def asSections(q: SectionsQuery) = q.response map (_.results)
  }

  case class TagsQuery(parameters: Map[String, Parameter] = Map.empty)
    extends GeneralParameters[TagsQuery]
          with PaginationParameters[TagsQuery]
          with FilterParameters[TagsQuery]
          with RefererenceParameters[TagsQuery]
          with ShowReferenceParameters[TagsQuery] {

    lazy val tagType = new StringParameter("type")
    lazy val response: F[TagsResponse] = fetch(targetUrl + "/tags", queryParameters) map parseTags

    def updated(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  }

  object TagsQuery {
    implicit def asResponse(q: TagsQuery) = q.response
    implicit def asTags(q: TagsQuery) = q.response map (_.results)
  }

  case class SearchQuery(parameters: Map[String, Parameter] = Map.empty)
    extends GeneralParameters[SearchQuery]
          with PaginationParameters[SearchQuery]
          with ShowParameters[SearchQuery]
          with RefinementParameters[SearchQuery]
          with FilterParameters[SearchQuery]
          with ContentFilterParameters[SearchQuery]
          with RefererenceParameters[SearchQuery]
          with ShowReferenceParameters[SearchQuery] {

    lazy val response: F[SearchResponse] = fetch(targetUrl + "/search", queryParameters) map parseSearch

    def updated(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  }

  object SearchQuery {
    implicit def asResponse(q: SearchQuery) = q.response
    implicit def asContent(q: SearchQuery) = q.response map (_.results)
  }

  case class ItemQuery(path: Option[String] = None, parameters: Map[String, Parameter] = Map.empty)
    extends GeneralParameters[ItemQuery]
          with ShowParameters[ItemQuery]
          with ContentFilterParameters[ItemQuery]
          with PaginationParameters[ItemQuery]
          with ShowReferenceParameters[ItemQuery] {

    def apiUrl(newContentPath: String): ItemQuery = {
      require(newContentPath startsWith targetUrl, "apiUrl expects a full url; use itemId if you only have an id")
      copy(path = Some(newContentPath))
    }

    def itemId(contentId: String): ItemQuery = apiUrl(targetUrl + "/" + contentId)

    lazy val response: F[ItemResponse] = fetch(
        path.getOrElse(throw new Exception("No api url provided to item query, ensure withApiUrl is called")),
        queryParameters) map parseItem

    def updated(parameterMap: Map[String, Parameter]) = copy(path, parameterMap)

  }

  object ItemQuery {
    implicit def asResponse(q: ItemQuery) = q.response
  }

  trait GeneralParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    override def queryParameters = super.queryParameters ++ apiKey.map("api-key" -> _)
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
    def folder = StringParameter("folder")
  }

  trait ContentFilterParameters[Owner <: Parameters[Owner]] extends FilterParameters[Owner] { this: Owner =>
    def orderBy = StringParameter("order-by")
    def fromDate = DateParameter("from-date")
    def toDate = DateParameter("to-date")
    def dateId = StringParameter("date-id")
    def useDate = StringParameter("use-date")
   }

  trait ShowParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def showFields = StringParameter("show-fields")
    def showSnippets = StringParameter("show-snippets")
    def showTags = StringParameter("show-tags")
    def showFactboxes = StringParameter("show-factboxes")
    def showMedia = StringParameter("show-media")
    def showRelated = BoolParameter("show-related")
    def showEditorsPicks = BoolParameter("show-editors-picks")
    def edition = StringParameter("edition")
    def showMostViewed = BoolParameter("show-most-viewed")
    def showStoryPackage = BoolParameter("show-story-package")
    def showBestBets = BoolParameter("show-best-bets")
    def snippetPre = StringParameter("snippet-pre")
    def snippetPost = StringParameter("snippet-post")
    def showInlineElements = StringParameter("show-inline-elements")
    def showExpired = BoolParameter("show-expired")
  }

  trait RefinementParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def showRefinements = StringParameter("show-refinements")
    def refinementSize = IntParameter("refinement-size")
  }

  trait RefererenceParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def reference = StringParameter("reference")
    def referenceType = StringParameter("reference-type")
  }

  trait ShowReferenceParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    def showReferences = StringParameter("show-references")
  }


  protected def fetch(url: String, parameters: Map[String, String]): F[String] = {
    require(!url.contains('?'), "must not specify queryParameters in url")

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

/** Base trait for blocking clients */
trait SyncApi extends Api[Id] {
  implicit val M = MonadInstances.idMonad
}

/** Base trait for Future-based async clients */
trait FutureAsyncApi extends Api[Future] {
  implicit def executionContext: ExecutionContext
  implicit def M = MonadInstances.futureMonad(executionContext)
}


// Default client instance, based on java.net client
object Api extends SyncApi with JavaNetSyncHttp

/** Async client instance based on Dispatch
  */
object DispatchAsyncApi extends FutureAsyncApi with DispatchAsyncHttp {
  implicit val executionContext = ExecutionContext.global
}
