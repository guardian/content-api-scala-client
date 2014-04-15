package com.gu.openplatform.contentapi.queries

import com.gu.openplatform.contentapi.parameters._
import scala.concurrent.Future
import com.gu.openplatform.contentapi.model._

sealed trait Query

case class FoldersQuery(filterParameters: FilterParameters = FilterParameters.empty) extends Query

case class SectionsQuery(filterParameters: FilterParameters = FilterParameters.empty) extends Query

case class TagsQuery(
  paginationParameters: PaginationParameters = PaginationParameters.empty,
  filterParameters: FilterParameters = FilterParameters.empty,
  referenceParameters: ReferenceParameters = ReferenceParameters.empty,
  showReferenceParameters: ReferenceParameters = ReferenceParameters.empty
) extends Query {
  //lazy val tagType = new StringParameter("type")  why was this here???
  //lazy val response: Future[TagsResponse] = ??? //fetch(targetUrl + "/tags", parameters) map parseTags
}

case class FrontsQuery(paginationParameters: PaginationParameters = PaginationParameters.empty) extends Query

case class SearchQuery(
  paginationParameters: PaginationParameters = PaginationParameters.empty,
  showParameters: ShowParameters = ShowParameters.empty,
  refinementParameters: RefinementParameters = RefinementParameters.empty,
  filterParameters: FilterParameters = FilterParameters.empty,
  contentFilterParameters: ContentFilterParameters = ContentFilterParameters.empty,
  referenceParameters: ReferenceParameters = ReferenceParameters.empty,
  showReferenceParameters: ReferenceParameters = ReferenceParameters.empty
) extends Query {
  //lazy val response: Future[SearchResponse] = ??? //fetch(targetUrl + "/search", parameters) map parseSearch
}

case class ItemQuery(
  id: String,
  showParameters: ShowParameters = ShowParameters.empty,
  filterParameters: FilterParameters = FilterParameters.empty,
  contentFilterParameters: ContentFilterParameters = ContentFilterParameters.empty,
  paginationParameters: PaginationParameters = PaginationParameters.empty,
  showReferenceParameters: ReferenceParameters = ReferenceParameters.empty
) extends Query

case class CollectionQuery(
  path: Option[String] = None,
  showParameters: ShowParameters = ShowParameters.empty,
  filterParameters: FilterParameters = FilterParameters.empty,
  paginationParameters: PaginationParameters = PaginationParameters.empty,
  showReferenceParameters: ReferenceParameters = ReferenceParameters.empty
) extends Query {
  def itemId(collectionId: String): CollectionQuery = copy(path = Some(collectionId)) //apiUrl(targetUrl + "/collections/" + collectionId)

  lazy val response: Future[CollectionResponse] = ??? /*fetch(
        path.getOrElse(throw new Exception("No api url provided to collection query, ensure withApiUrl is called")),
        parameters) map parseCollection*/
}
