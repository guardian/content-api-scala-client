package com.gu.openplatform.contentapi


import connection.{ApacheHttpClient, Http}
import java.net.URLEncoder
import com.gu.openplatform.contentapi.parser.JsonParser
import scala.collection

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


  trait ApiQuery[T] extends JsonParser {
    def mandatoryParameters = "?"

    def optionalParameters = apiKey.map("&api-key=" + _).getOrElse("")

  }

  trait PaginatedQuery[T] {
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

    def paginationParameters = {
      var stringBuilder = new StringBuilder

      pageSize.foreach(i => stringBuilder.append("&page-size=").append(i))
      page.foreach(i => stringBuilder.append("&page=").append(i))

      stringBuilder.toString
    }
  }

  trait ConfigurableItemDisplay[T] {
    var fields: Option[String] = None
    var showTags: Option[String] = None
    var showFactboxes: Option[String] = None
    var showMediaTypes: Option[String] = None

    def withFields(newFields: String) = {
      fields = Some(newFields)
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
      showMediaTypes = Some(newShowMediaTypes)
      this.asInstanceOf[T]
    }

    def itemDisplayParameters :String = {
      var stringBuilder = new StringBuilder

      fields.foreach(s => stringBuilder.append("&show-fields=").append(s))
      showTags.foreach(s => stringBuilder.append("&show-tags=").append(s))
      showFactboxes.foreach(s => stringBuilder.append("&show-factboxes=").append(s))
      showMediaTypes.foreach(s => stringBuilder.append("&show-media=").append(s))

      stringBuilder.toString
    }
  }

  trait RefineableQuery[T] {
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

    def refinementDisplayParameters = {
      var stringBuilder = new StringBuilder

      showRefinements.foreach(s => stringBuilder.append("&show-refinements=").append(s))
      refinementSize.foreach(s => stringBuilder.append("&refinement-size=").append(s))

      stringBuilder.toString
    }
  }

  trait SearchTermQuery[T] {
    var queryTerm: Option[String] = None

    def withQueryTerm(newQueryTerm: String) = {
      queryTerm = Some(URLEncoder.encode(newQueryTerm, "UTF-8"))
      this.asInstanceOf[T]
    }

    def queryTermParameters = {
      var stringBuilder = new StringBuilder
      queryTerm.foreach(s => stringBuilder.append("&q=").append(s))
      stringBuilder.toString
    }
  }

  trait FilterableResultsQuery[T] {
    var sectionTerm: Option[String] = None
    var tagTerm: Option[String] = None
    var orderBy: Option[String] = None
    var fromDate: Option[String] = None
    var toDate: Option[String] = None

    def withSectionTerm(newSectionTerm: String) = {
      sectionTerm = Some(newSectionTerm)
      this.asInstanceOf[T]
    }

    def withTagTerm(newTagTerm: String) = {
      tagTerm = Some(newTagTerm)
      this.asInstanceOf[T]
    }

    def orderBy(newOrderBy: String): T = {
      orderBy = Some(newOrderBy)
      this.asInstanceOf[T]
    }

    def withFromDate(newFromDate: String) = {
      fromDate = Some(newFromDate)
      this.asInstanceOf[T]
    }

    def withToDate(newToDate: String) = {
      toDate = Some(newToDate)
      this.asInstanceOf[T]
    }

    def filterableResultsParameters = {
      var stringBuilder = new StringBuilder

      sectionTerm.foreach(s => stringBuilder.append("&section=").append(s))
      tagTerm.foreach(s => stringBuilder.append("&tag=").append(s))
      orderBy.foreach(s => stringBuilder.append("&order-by=").append(s))
      fromDate.foreach(s => stringBuilder.append("&from-date=").append(s))
      toDate.foreach(s => stringBuilder.append("&to-date=").append(s))

      stringBuilder.toString
    }
  }

  class SectionsQuery
          extends ApiQuery[SectionsQuery]
          with SearchTermQuery[SectionsQuery] {

    def sections = parseSections(fetch(buildUrl))

    def buildUrl = new StringBuilder()
        .append(targetUrl)
        .append("/sections")
        .append(mandatoryParameters)
        .append(optionalParameters)
        .append(queryTermParameters)
        .toString
  }

  class TagsQuery extends ApiQuery[TagsQuery]
          with PaginatedQuery[TagsQuery]
          with ConfigurableItemDisplay[TagsQuery]
          with SearchTermQuery[TagsQuery] {

    var sectionTerm: Option[String] = None
    var typeTerm: Option[String] = None

    def withSectionTerm(newSectionTerm: String) = {
      sectionTerm = Some(newSectionTerm)
      this
    }

    def withTypeTerm(newTypeTerm: String) = {
      typeTerm = Some(newTypeTerm)
      this
    }

    def tags = parseTags(fetch(buildUrl))

    def buildUrl = {
      var urlBuilder = new StringBuilder

      urlBuilder
        .append(targetUrl)
        .append("/tags")
        .append(mandatoryParameters)
        .append(optionalParameters)
        .append(paginationParameters)
        .append(itemDisplayParameters)
        .append(queryTermParameters)

      sectionTerm.foreach(s => urlBuilder.append("&section=").append(s))
      typeTerm.foreach(s => urlBuilder.append("&type=").append(s))

      urlBuilder.toString
    }
  }

  class SearchQuery extends ApiQuery[SearchQuery]
          with PaginatedQuery[SearchQuery] with ConfigurableItemDisplay[SearchQuery]
          with RefineableQuery[SearchQuery] with SearchTermQuery[SearchQuery]
          with FilterableResultsQuery[SearchQuery] {

    def search = parseSearch(fetch(buildUrl))

    def buildUrl = {
      var urlBuilder = new StringBuilder

      urlBuilder
        .append(targetUrl)
        .append("/search")
        .append(mandatoryParameters)
        .append(optionalParameters)
        .append(paginationParameters)
        .append(itemDisplayParameters)
        .append(refinementDisplayParameters)
        .append(queryTermParameters)
        .append(filterableResultsParameters)

      urlBuilder.toString
    }
  }

  class ItemQuery extends ApiQuery[ItemQuery] with ConfigurableItemDisplay[ItemQuery]
        with FilterableResultsQuery[ItemQuery] with PaginatedQuery[ItemQuery]
        with SearchTermQuery[ItemQuery]{

    var apiUrl: Option[String] = None

    def withApiUrl(newContentPath: String) = {
      apiUrl = Some(newContentPath)
      this
    }

    def query = parseItem(fetch(buildUrl))

    def buildUrl = {
      var urlBuilder = new StringBuilder

      urlBuilder
        .append(apiUrl.getOrElse(throw new Exception("No api url provided to item query, ensure withApiUrl is called")).toString)
        .append(mandatoryParameters)
        .append(optionalParameters)
        .append(itemDisplayParameters)
        .append(filterableResultsParameters)
        .append(paginationParameters)
        .append(queryTermParameters)

      urlBuilder.toString
    }
  }


  def fetch(url: String, parameters: Map[String, String] = Map.empty): String = {
    require(parameters.isEmpty || !url.contains('?'), "must either specifiy paramters or append to query string, not both")


    val response = GET(url, List("User-Agent" -> "scala-api-client", "Accept" -> "application/json"))

    if (List(200, 302) contains response.statusCode) {
      response.body
    } else {
      throw new ApiError(response.statusCode, response.statusMessage)
    }

  }
}

object Api extends Api with ApacheHttpClient