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

trait Api[F[+_]] extends Http[F] with JsonParser {
  import MonadOps._

  /** Proof that we can call point, map, flatMap and error for type F */
  implicit def M: Monad[F]

  val targetUrl = "http://content.guardianapis.com"
  val apiKey: Option[String] = None

  def sections = new SectionsQuery
  def tags = new TagsQuery
  def folders = new FoldersQuery
  def search = new SearchQuery
  def item = new ItemQuery
  def fronts = new FrontsQuery
  def collection = new CollectionQuery

  case class FoldersQuery(parameterHolder: Map[String, Parameter] = Map.empty)
    extends GeneralParameters[FoldersQuery]
    with FilterParameters[FoldersQuery] {

    lazy val response: F[FoldersResponse] = fetch(targetUrl + "/folders", parameters) map parseFolders

    def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)
  }

  object FoldersQuery {
    implicit def asResponse(q: FoldersQuery) = q.response
    implicit def asFolders(q: FoldersQuery) = q.response map (_.results)
  }

  case class SectionsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
    extends GeneralParameters[SectionsQuery]
    with FilterParameters[SectionsQuery] {

    lazy val response: F[SectionsResponse] = fetch(targetUrl + "/sections", parameters) map parseSections

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
    lazy val response: F[TagsResponse] = fetch(targetUrl + "/tags", parameters) map parseTags

    def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  }

  object FrontsQuery {
    implicit def asResponse(q: FrontsQuery) = q.response
    implicit def asFronts(q: FrontsQuery) = q.response map (_.results)
  }

  case class FrontsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
    extends GeneralParameters[FrontsQuery]
    with PaginationParameters[FrontsQuery] {

    lazy val response: F[FrontsResponse] = fetch(targetUrl + "/fronts", parameters) map parseFronts

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
          with RefinementParameters[SearchQuery]
          with FilterParameters[SearchQuery]
          with ContentFilterParameters[SearchQuery]
          with RefererenceParameters[SearchQuery]
          with ShowReferenceParameters[SearchQuery] {

    lazy val response: F[SearchResponse] = fetch(targetUrl + "/search", parameters) map parseSearch

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

    lazy val response: F[ItemResponse] = fetch(
        path.getOrElse(throw new Exception("No api url provided to item query, ensure withApiUrl is called")),
        parameters) map parseItem

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

    lazy val response: F[CollectionResponse] = fetch(
        path.getOrElse(throw new Exception("No api url provided to collection query, ensure withApiUrl is called")),
        parameters) map parseCollection

    def withBackfill: F[(CollectionResponse, ContentResultsResponse)] = response flatMap { collectionResponse =>
      // val query = collectionResponse.backfillQuery

      // some code here that magically turns this into either an ItemQuery or a SearchQuery, modelled on this thingy--
      //
      // https://github.com/guardian/frontend/blob/master/common/app/services/ParseCollection.scala#L206
      //
      // It tries to automatically do the 'right thing' with the backfill. Basically, whatever show parameters you've
      // passed to the collection query are going to be the exact same show parameters that you want to pass to the
      // backfill. This will figure that out, so you get back what you expect.

      val query: Either[ItemQuery, SearchQuery] = ???

      query.fold[F[ContentResultsResponse]](_.response, _.response) map { backfillResponse: ContentResultsResponse =>
        (collectionResponse, backfillResponse)
      }
    }

    def withParameters(parameterMap: Map[String, Parameter]) = copy(path, parameterMap)
  }

  trait GeneralParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
    override def parameters = super.parameters ++ apiKey.map("api-key" -> _)
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

  trait ContentFilterParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
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
    def showElements = StringParameter("show-elements")
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
