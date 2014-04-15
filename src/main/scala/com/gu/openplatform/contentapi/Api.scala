package com.gu.openplatform.contentapi

import connection.Http
import com.gu.openplatform.contentapi.queries._
import parameters.renderParams
import com.gu.openplatform.contentapi.model._
import scala.concurrent.Future
import util._
import parser.JsonParser._

case class ApiError(httpStatus: Int, httpMessage: String) extends Exception(httpMessage)

object Endpoint {
  def default = Endpoint("http://content.guardianapis.com", None)
}

case class Endpoint(targetUrl: String, apiKey: Option[String])

case class Api(endpoint: Endpoint = Endpoint.default, http: Http) {
  import endpoint._

  private val apiKeyParams = apiKey map { key => Map("api-key" -> key) } getOrElse Map.empty

  def item(itemQuery: ItemQuery): Future[ItemResponse] = {
    val parameters =
      renderParams(itemQuery.paginationParameters) ++
        renderParams(itemQuery.contentFilterParameters) ++
        renderParams(itemQuery.filterParameters) ++
        renderParams(itemQuery.showParameters) ++
        renderParams(itemQuery.showReferenceParameters) ++
        apiKeyParams

    http.fetch(targetUrl / itemQuery.id, parameters) map parseItem
  }

  def folders(foldersQuery: FoldersQuery): Future[FoldersResponse] = {
    val parameters =
      renderParams(foldersQuery.filterParameters) ++
      apiKeyParams

    http.fetch(targetUrl / "folders", parameters) map parseFolders
  }

  def sections(sectionsQuery: SectionsQuery): Future[SectionsResponse] = {
    val parameters =
      renderParams(sectionsQuery.filterParameters) ++
      apiKeyParams

    http.fetch(targetUrl / "sections", parameters) map parseSections
  }

  def fronts(frontsQuery: FrontsQuery): Future[FrontsResponse] = {
    val parameters = renderParams(frontsQuery.paginationParameters) ++
      apiKeyParams

    http.fetch(targetUrl / "fronts", parameters) map parseFronts
  }

  def collection(collectionQuery: CollectionQuery): Future[CollectionResponse] = {

  }
}
