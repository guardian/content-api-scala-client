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
  def search = new SearchQuery
  def item = new ItemQuery

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
          with FilterParameters[TagsQuery] {
    object tagType extends StringParameter(self, "type")
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
          with ContentFilterParameters[SearchQuery] {
    lazy val response = parseSearch(fetch(targetUrl + "/search", parameters))
  }

  object SearchQuery {
    implicit def asResponse(q: SearchQuery) = q.response
    implicit def asContent(q: SearchQuery) = q.response.results
  }




  class ItemQuery extends GeneralParameters[ItemQuery]
          with ShowParameters[ItemQuery]
          with ContentFilterParameters[ItemQuery]
          with PaginationParameters[ItemQuery] {
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
    object pageSize extends IntParameter(self, "page-size")
    object page extends IntParameter(self, "page")
  }

  trait FilterParameters[OwnerType <: ParameterHolder] extends Parameters[OwnerType] {
    object q extends StringParameter(self, "q")
    object section extends StringParameter(self, "section")
    object ids extends StringParameter(self, "ids")
    object tag extends StringParameter(self, "tag")
  }

  trait ContentFilterParameters[OwnerType <: ParameterHolder] extends FilterParameters[OwnerType] {
    object orderBy extends StringParameter(self, "order-by")
    // most likely, you'll want to use DateMidnight
    // to pass to these
    object fromDate extends DateParameter(self, "from-date")
    object toDate extends DateParameter(self, "to-date")
   }

  trait ShowParameters[OwnerType <: ParameterHolder] extends Parameters[OwnerType] {
    object showFields extends StringParameter(self, "show-fields")
    object showSnippets extends StringParameter(self, "show-snippets")
    object showTags extends StringParameter(self, "show-tags")
    object showFactboxes extends StringParameter(self, "show-factboxes")
    object showMedia extends StringParameter(self, "show-media")
    object showRelated extends BoolParameter(self, "show-related")
    object showEditorsPicks extends BoolParameter(self, "show-editors-picks")
    object showMostViewed extends BoolParameter(self, "show-most-viewed")
    object showBestBets extends BoolParameter(self, "show-best-bets")
    object snippetPre extends StringParameter(self, "snippet-pre")
    object snippetPost extends StringParameter(self, "snippet-post")
  }

  trait RefinementParameters[OwnerType <: ParameterHolder] extends Parameters[OwnerType] {
    object showRefinements extends StringParameter(self, "show-refinements")
    object refinementSize extends IntParameter(self, "refinement-size")
  }



  protected def fetch(url: String, parameters: Map[String, Any] = Map.empty): String = {
    require(!url.contains('?'), "must not specify parameters in url")

    def encodeParameter(p: Any): String = p match {
      case dt: ReadableInstant => ISODateTimeFormat.yearMonthDay.print(dt)
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