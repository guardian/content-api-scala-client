package com.gu.openplatform.contentapi.model

// NB:
//  Due to the way lift-json parses lists and collections
//  we have to be inconsitant with optional lists.
//  Everywhere else where things are optional, we make then Options.
//  But for lists, this doesn't work (you get back Some(Nil) even if
//  the element is not present.
//  So Lists are never Option[List], you will just get back Nil if the
//  the element was not present in the response


// Responses

// /search
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
        bestBets: List[BestBet]
)

// /tags
case class TagsResponse(
        status: String,
        userTier: String,
        total: Int,
        startIndex: Int,
        pageSize: Int,
        currentPage: Int,
        pages: Int,
        results: List[Tag]
)


// /sections
case class SectionsResponse(
        status: String,
        userTier: String,
        total: Int,
        results: List[Section]
)

// /sections
case class FoldersResponse(
        status: String,
        userTier: String,
        total: Int,
        startIndex: Int,
        pageSize: Int,
        currentPage: Int,
        pages: Int,
        results: List[Folder]
        )

// /anythingelse
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
        leadContent: List[Content]
        )