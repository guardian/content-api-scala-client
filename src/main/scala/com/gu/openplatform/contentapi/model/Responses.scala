package com.gu.openplatform.contentapi.model

sealed trait WithResults {
  val results: List[Content]
}

case class SearchResponse(
    status: String,
    userTier: String,
    total: Int,
    startIndex: Int,
    pageSize: Int,
    currentPage: Int,
    pages: Int,
    orderBy: String,
    didYouMean: Option[String],
    results: List[Content],
    refinementGroups: List[RefinementGroup],
    bestBets: List[BestBet]) extends WithResults

case class TagsResponse(
    status: String,
    userTier: String,
    total: Int,
    startIndex: Int,
    pageSize: Int,
    currentPage: Int,
    pages: Int,
    results: List[Tag])

case class SectionsResponse(
    status: String,
    userTier: String,
    total: Int,
    results: List[Section])

case class FrontsResponse(
    status: String,
    userTier: String,
    total: Int,
    results: List[Front])

case class FoldersResponse(
    status: String,
    userTier: String,
    total: Int,
    startIndex: Int,
    pageSize: Int,
    currentPage: Int,
    pages: Int,
    results: List[Folder])

case class ItemResponse(
    status: String,
    userTier: String,
    total: Option[Int],
    startIndex: Option[Int],
    pageSize: Option[Int],
    currentPage: Option[Int],
    pages: Option[Int],
    orderBy: Option[String],
    tag: Option[Tag],
    edition: Option[Edition],
    section: Option[Section],
    content: Option[Content],
    results: List[Content],
    relatedContent: List[Content],
    editorsPicks: List[Content],
    mostViewed: List[Content],
    storyPackage: List[Content],
    leadContent: List[Content]) extends WithResults

case class CollectionResponse(
    status: String,
    userTier: String,
    total: Int,
    startIndex: Int,
    pageSize: Int,
    currentPage: Int,
    pages: Int,
    collection: Collection)