package com.gu.openplatform.contentapi.connection


import com.gu.openplatform.contentapi.model._
import com.gu.openplatform.contentapi.parser.XmlParser
import java.net.{URLEncoder, URL}

// thrown when an "expected" error is thrown by the api
case class ApiError(val httpStatus: Int, val httpMessage: String)
        extends Exception(httpMessage)

object Api {

  private val targetUrl = "http://content.guardianapis.com"
  private var apiKey: Option[String] = None

  def setKey(key: String) = { apiKey = Some(key) }

  def sectionsQuery = new SectionsQuery
  def tagsQuery = new TagsQuery
  def searchQuery = new SearchQuery
  def itemQuery = new ItemQuery

  trait ApiQuery[T]{

    def getResponse(endpoint: String, responseString: String) : Response = {
      XmlParser.parseEndpoint(endpoint, responseString)
    }

    def mandatoryParameters :String = {
      "?format=xml"
    }

    def optionalParameters :String = {
      var stringBuilder = new StringBuilder

      apiKey.foreach(s => stringBuilder.append("&api-key=").append(s))

      stringBuilder.toString
    }
  }

  trait PaginatedQuery[T] {
    var pageSize: Option[String] = None
    var page: Option[Int] = None

    def withPageSize(newPageSize: Int) :T = {
      pageSize = Some(newPageSize.toString)
      this.asInstanceOf[T]
    }

    def withPageSize(newPageSize: String) :T = {
      pageSize = Some(newPageSize)
      this.asInstanceOf[T]
    }

    def withPage(newPage: Int) :T = {
      page = Some(newPage)
      this.asInstanceOf[T]
    }

    def paginationParameters :String = {
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

    def withFields(newFields: String) :T = {
      fields = Some(newFields)
      this.asInstanceOf[T]
    }

    def withShowTags(newShowTags: String) :T = {
      showTags = Some(newShowTags)
      this.asInstanceOf[T]
    }

    def withShowFactboxes(newShowFactboxes: String) :T = {
      showFactboxes = Some(newShowFactboxes)
      this.asInstanceOf[T]
    }

    def withShowMedia(newShowMediaTypes: String) :T = {
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

    def withShowRefinements(newShowRefinements: String) :T = {
      showRefinements = Some(newShowRefinements)
      this.asInstanceOf[T]
    }

    def withRefinementSize(newRefinementSize: Int) :T = {
      refinementSize = Some(newRefinementSize)
      this.asInstanceOf[T]
    }

    def refinementDisplayParameters :String = {
      var stringBuilder = new StringBuilder

      showRefinements.foreach(s => stringBuilder.append("&show-refinements=").append(s))
      refinementSize.foreach(s => stringBuilder.append("&refinement-size=").append(s))

      stringBuilder.toString
    }
  }

  trait SearchTermQuery[T] {
    var queryTerm: Option[String] = None

    def withQueryTerm(newQueryTerm: String) :T = {
      queryTerm = Some(URLEncoder.encode(newQueryTerm))
      this.asInstanceOf[T]
    }

    def queryTermParameters: String = {
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

    def withSectionTerm(newSectionTerm: String): T = {
      sectionTerm = Some(newSectionTerm)
      this.asInstanceOf[T]
    }

    def withTagTerm(newTagTerm: String): T = {
      tagTerm = Some(newTagTerm)
      this.asInstanceOf[T]
    }

    def orderBy(newOrderBy: String): T = {
      orderBy = Some(newOrderBy)
      this.asInstanceOf[T]
    }

    def withFromDate(newFromDate: String): T = {
      fromDate = Some(newFromDate)
      this.asInstanceOf[T]
    }

    def withToDate(newToDate: String): T = {
      toDate = Some(newToDate)
      this.asInstanceOf[T]
    }

    def filterableResultsParameters: String = {
      var stringBuilder = new StringBuilder

      sectionTerm.foreach(s => stringBuilder.append("&section=").append(s))
      tagTerm.foreach(s => stringBuilder.append("&tag=").append(s))
      orderBy.foreach(s => stringBuilder.append("&order-by=").append(s))
      fromDate.foreach(s => stringBuilder.append("&from-date=").append(s))
      toDate.foreach(s => stringBuilder.append("&to-date=").append(s))

      stringBuilder.toString
    }
  }

  class SectionsQuery extends ApiQuery[SectionsQuery] with SearchTermQuery[SectionsQuery] {

    def sections: SectionsResponse = parseSectionsResponse(Http GET buildUrl)

    private def parseSectionsResponse(httpResponse: HttpResponse): SectionsResponse = {
      val response = getResponse("sections", httpResponse.body)
      response.asInstanceOf[SectionsResponse]
    }

    def buildUrl = {
      var urlBuilder = new StringBuilder

      urlBuilder
        .append(targetUrl)
        .append("/sections")
        .append(mandatoryParameters)
        .append(optionalParameters)
        .append(queryTermParameters)

      urlBuilder.toString
    }
  }

  class TagsQuery extends ApiQuery[TagsQuery]
          with PaginatedQuery[TagsQuery] with ConfigurableItemDisplay[TagsQuery] with SearchTermQuery[TagsQuery] {

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

    def tags: TagsResponse = parseTagsResponse(Http GET buildUrl)

    private def parseTagsResponse(httpResponse: HttpResponse): TagsResponse = {
      val response = getResponse("tags", httpResponse.body)
      response.asInstanceOf[TagsResponse]
    }

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

    def search: SearchResponse = parseSearchResponse(Http GET buildUrl)

    private def parseSearchResponse(httpResponse: HttpResponse): SearchResponse = {
      val response = getResponse("search", httpResponse.body)
      response.asInstanceOf[SearchResponse]
    }

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

    var apiUrl: Option[URL] = None

    def withApiUrl(newContentPath: URL) = {
      apiUrl = Some(newContentPath)
      this
    }

    def query: ItemResponse = parseItemResponse(Http GET buildUrl)

    private def parseItemResponse(httpResponse: HttpResponse): ItemResponse = {
      val response = getResponse("id", httpResponse.body)
      response.asInstanceOf[ItemResponse]
    }

    def buildUrl = {
      var urlBuilder = new StringBuilder

      urlBuilder
        .append(apiUrl match {
          case Some(s) => s.toString
          case None => throw new Exception("No api url provided to item query, ensure withApiUrl is called")
        })
        .append(mandatoryParameters)
        .append(optionalParameters)
        .append(itemDisplayParameters)
        .append(filterableResultsParameters)
        .append(paginationParameters)
        .append(queryTermParameters)

      urlBuilder.toString
    }
  }
}
