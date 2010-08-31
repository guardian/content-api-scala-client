package com.gu.openplatform.contentapi


import connection.{ApacheHttpClient, Http}
import java.net.URLEncoder
import com.gu.openplatform.contentapi.parser.JsonParser
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.ReadableInstant


// thrown when an "expected" error is thrown by the api
case class ApiError(val httpStatus: Int, val httpMessage: String)
        extends Exception(httpMessage)


abstract class Api extends Http {
  val targetUrl = "http://content.guardianapis.com"
  var apiKey: Option[String] = None

  def sectionsQuery = new SectionsQuery
  def tagsQuery = new TagsQuery
  def searchQuery = new SearchQuery
  def itemQuery = new ItemQuery


  trait Parameters {
    def parameters: Map[String, Any] = Map.empty
  }



  trait GeneralParameters extends Parameters {
     override def parameters = super.parameters ++
            apiKey.map("api-key" -> _)
  }



  trait PaginationParameters extends Parameters {
    var _pageSize: Option[String] = None
    var _page: Option[Int] = None

    def pageSize(newPageSize: Int): this.type = {
      _pageSize = Some(newPageSize.toString); this
    }

    def pageSize(newPageSize: String): this.type  = {
      _pageSize = Some(newPageSize); this
    }

    def page(newPage: Int): this.type  = {
      _page = Some(newPage); this
    }

    override def parameters = super.parameters ++
            _pageSize.map("page-size" -> _) ++
            _page.map("page" -> _)
  }


  trait FilterParameters extends Parameters {
    var _q: Option[String] = None
    var _section: Option[String] = None
    var _ids: Option[String] = None
    var _tag: Option[String] = None

    def section(s: String): this.type = {
      _section = Some(s)
      this
    }

    def q(newQ: String): this.type = {
      _q = Some(newQ)
      this
    }

    def tags(newTagTerm: String): this.type = {
      _tag = Some(newTagTerm)
      this
    }

    def ids(newIds: String): this.type= {
      _ids = Some(newIds)
      this
    }

    override def parameters = super.parameters ++
            _q.map("q" -> _) ++
            _section.map("section" -> _) ++
            _ids.map("ids" -> _) ++
            _tag.map("tag" -> )
            Map("ids" -> _ids, "tag" -> _tag)
  }



  trait ContentFilterParamters extends FilterParameters {
    var _orderBy: Option[String] = None
    var _fromDate: Option[ReadableInstant] = None
    var _toDate: Option[ReadableInstant] = None

    def orderBy(s: String): this.type = {
      _orderBy = Some(s); this
    }

    // most likely, you'll want to use DateMidnight
    // to pass to this
    def fromDate(d: ReadableInstant): this.type = {
      _fromDate = Some(d); this
    }

    // most likely, you'll want to use DateMidnight
    // to pass to this
    def toDate(d: ReadableInstant): this.type = {
      _toDate = Some(d); this
    }

    override def parameters = super.parameters ++
            _orderBy.map("order-by" -> _) ++
            _fromDate.map("from-date" -> _) ++
            _toDate.map("to-date" -> _)

  }



  trait ShowParameters extends Parameters {
    var _showFields: Option[String] = None
    var _showTags: Option[String] = None
    var _showFactboxes: Option[String] = None
    var _showMedia: Option[String] = None

    def showFields(newFields: String): this.type  = {
      _showFields = Some(newFields)
      this
    }

    def showTags(newShowTags: String): this.type  = {
      _showTags = Some(newShowTags)
      this
    }

    def showFactboxes(newShowFactboxes: String): this.type  = {
      _showFactboxes = Some(newShowFactboxes)
      this
    }

    def showMedia(newShowMediaTypes: String): this.type  = {
      _showMedia = Some(newShowMediaTypes)
      this
    }

    override def parameters = super.parameters ++
            _showFields.map("show-fields" -> _) ++
            _showTags.map("show-tags" -> _) ++
            _showFactboxes.map("show-factboxes" -> _) ++
            _showMedia.map("show-media" -> _)

  }

  trait RefinementParameters extends Parameters {
    var _showRefinements: Option[String] = None
    var _refinementSize: Option[Int] = None

    def showRefinements(newShowRefinements: String): this.type = {
      _showRefinements = Some(newShowRefinements)
      this
    }

    def refinementSize(newRefinementSize: Int): this.type = {
      _refinementSize = Some(newRefinementSize)
      this
    }

    override def parameters = super.parameters ++
            _showRefinements.map("show-refinements" -> _) ++
            _refinementSize.map("refinement-size" -> _)
  }



  class SectionsQuery
          extends GeneralParameters
                  with FilterParameters
                  with JsonParser {
    def sections = parseSections(fetch(targetUrl + "/sections", parameters))
  }



  class TagsQuery extends GeneralParameters
          with PaginationParameters
          with FilterParameters
          with JsonParser {
    var _tagType: Option[String] = None

    def tagType(newTypeTerm: String) = {
      _tagType = Some(newTypeTerm)
      this
    }

    def tags = parseTags(fetch(targetUrl + "/tags", parameters))

    override def parameters = super.parameters ++
            _tagType.map("type" -> _)

  }

  class SearchQuery extends GeneralParameters
          with PaginationParameters
          with ShowParameters
          with RefinementParameters
          with FilterParameters
          with ContentFilterParamters
          with JsonParser {
    def search = parseSearch(fetch(targetUrl + "/search", parameters))
  }

  class ItemQuery extends GeneralParameters
          with ShowParameters
          with ContentFilterParamters
          with PaginationParameters
          with JsonParser {
    var _apiUrl: Option[String] = None

    def apiUrl(newContentPath: String): this.type = {
      require(newContentPath startsWith targetUrl, "apiUrl expects a full url; use itemId if you only have an id")
      _apiUrl = Some(newContentPath)
      this
    }

    def itemId(contentId: String): this.type = apiUrl(targetUrl + "/" + contentId)

    def query = parseItem(fetch(_apiUrl.getOrElse(throw new Exception("No api url provided to item query, ensure withApiUrl is called")), parameters))

  }


  protected def fetch(url: String, parameters: Map[String, Any] = Map.empty): String = {
    require(!url.contains('?'), "must not specify paramaters on query string")

    def encodeParameter(p: Any): String = p match {
      case list: List[_] => list.map(encodeParameter(_)).mkString(",")
      case dt: ReadableInstant => ISODateTimeFormat.yearMonthDay.print(dt)
      case other => URLEncoder.encode(other.toString, "UTF-8")
    }

    val queryString = parameters.map {case (k, v) => k + "=" + encodeParameter(v)}.mkString("&")
    val target = url + "?" + queryString

    println("target=" + target)

    val response = GET(target, List("User-Agent" -> "scala-api-client", "Accept" -> "application/json"))

    if (List(200, 302) contains response.statusCode) {
      response.body
    } else {
      throw new ApiError(response.statusCode, response.statusMessage)
    }
  }


}

object Api extends Api with ApacheHttpClient