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



  trait GeneralParameters[T] extends Parameters {
     override def parameters: Map[String, Any] = super.parameters ++
            apiKey.map("api-key" -> _)
  }



  trait PaginationParameters[T] extends Parameters {
    var pageSize: Option[String] = None
    var page: Option[Int] = None

    def withPageSize(newPageSize: Int) = {
      pageSize = Some(newPageSize.toString)
      this.asInstanceOf[T]
    }

    def withPageSize(newPageSize: String) = {
      pageSize = Some(newPageSize)
      this.asInstanceOf[T]
    }

    def withPage(newPage: Int) = {
      page = Some(newPage)
      this.asInstanceOf[T]
    }

    override def parameters = super.parameters ++
            pageSize.map("page-size" -> _) ++
            page.map("page" -> _)
  }


  trait FilterParameters[T] extends Parameters {
    var q: Option[String] = None
    var section: Option[String] = None
    var ids: List[String] = Nil
    var tag: List[String] = Nil

    def withSection(s: String) = {
      section = Some(s)
      this.asInstanceOf[T]
    }

    def withQ(newQ: String) = {
      q = Some(newQ)
      this.asInstanceOf[T]
    }

    def withTag(newTagTerm: String) = {
      tag = newTagTerm :: tag
      this.asInstanceOf[T]
    }

    def withTags(tags: List[String]) = {
      tag = tags
      this.asInstanceOf[T]
    }

    def withId(id: String) = {
      ids = id :: ids
      this.asInstanceOf[T]
    }

    def withIds(newIds: List[String]) = {
      ids = newIds
      this.asInstanceOf[T]
    }

    override def parameters = super.parameters ++
            q.map("q" -> _) ++
            section.map("section" -> _) ++
            Map("ids" -> ids, "tag" -> tag)
  }


  trait ContentFilterParamters[T] extends FilterParameters[T] {
    var orderBy: Option[String] = None
    var fromDate: Option[ReadableInstant] = None
    var toDate: Option[ReadableInstant] = None

    def withOrderBy(s: String): this.type = {
      orderBy = Some(s); this
    }

    def withFromDate(d: ReadableInstant): this.type = {
      fromDate = Some(d); this
    }

    def withToDate(d: ReadableInstant): this.type = {
      toDate = Some(d); this
    }

    override def parameters = super.parameters ++
            orderBy.map("order-by" -> _) ++
            fromDate.map("from-date" -> _) ++
            toDate.map("to-date" -> _)

  }

  trait ShowParameters[T] extends Parameters {
    var showFields: Option[String] = None
    var showTags: Option[String] = None
    var showFactboxes: Option[String] = None
    var showMedia: Option[String] = None

    def withShowFields(newFields: String) = {
      showFields = Some(newFields)
      this.asInstanceOf[T]
    }

    def withShowTags(newShowTags: String) = {
      showTags = Some(newShowTags)
      this.asInstanceOf[T]
    }

    def withShowFactboxes(newShowFactboxes: String) = {
      showFactboxes = Some(newShowFactboxes)
      this.asInstanceOf[T]
    }

    def withShowMedia(newShowMediaTypes: String) = {
      showMedia = Some(newShowMediaTypes)
      this.asInstanceOf[T]
    }

    override def parameters = super.parameters ++
            showFields.map("show-fields" -> _) ++
            showTags.map("show-tags" -> _) ++
            showFactboxes.map("show-factboxes" -> _) ++
            showMedia.map("show-media" -> _)

  }

  trait RefinementParameters[T] extends Parameters {
    var showRefinements: Option[String] = None
    var refinementSize: Option[Int] = None

    def withShowRefinements(newShowRefinements: String) = {
      showRefinements = Some(newShowRefinements)
      this.asInstanceOf[T]
    }

    def withRefinementSize(newRefinementSize: Int) = {
      refinementSize = Some(newRefinementSize)
      this.asInstanceOf[T]
    }

    override def parameters = super.parameters ++
            showRefinements.map("show-refinements" -> _) ++
            refinementSize.map("refinement-size" -> _)
  }



  class SectionsQuery
          extends GeneralParameters[SectionsQuery]
                  with FilterParameters[SectionsQuery]
                  with JsonParser {
    def sections = parseSections(fetch(targetUrl + "/sections", parameters))
  }



  class TagsQuery extends GeneralParameters[TagsQuery]
          with PaginationParameters[TagsQuery]
          with FilterParameters[TagsQuery]
          with JsonParser {
    var tagType: Option[String] = None

    def withType(newTypeTerm: String) = {
      tagType = Some(newTypeTerm)
      this
    }

    def tags = parseTags(fetch(targetUrl + "/tags", parameters))

    override def parameters = super.parameters ++
            tagType.map("type" -> _)

  }

  class SearchQuery extends GeneralParameters[SearchQuery]
          with PaginationParameters[SearchQuery]
          with ShowParameters[SearchQuery]
          with RefinementParameters[SearchQuery]
          with FilterParameters[SearchQuery]
          with ContentFilterParamters[SearchQuery]
          with JsonParser {
    def search = parseSearch(fetch(targetUrl + "/search", parameters))
  }

  class ItemQuery extends GeneralParameters[ItemQuery]
          with ShowParameters[ItemQuery]
          with ContentFilterParamters[ItemQuery]
          with PaginationParameters[ItemQuery]
          with JsonParser
  {
    var apiUrl: Option[String] = None

    def withApiUrl(newContentPath: String) = {
      apiUrl = Some(newContentPath)
      this
    }

    def query = parseItem(fetch(apiUrl.getOrElse(throw new Exception("No api url provided to item query, ensure withApiUrl is called")), parameters))

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