package com.gu.openplatform.contentapi.model.json

import org.joda.time.DateTime

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
        total: Long,
        startIndex: Long,
        pageSize: Long,
        currentPage: Long,
        pages: Long,
        orderBy: String,
        results: List[Content],
        refinementGroups: List[RefinementGroup]
)

// /tags
case class TagsResponse(
        status: String,
        userTier: String,
        total: Long,
        startIndex: Long,
        pageSize: Long,
        currentPage: Long,
        pages: Long,
        results: List[Tag]
)





// Model classes
case class Content(
        // the id of this item of content: this should always be the path
        // to the item on www.guardian.co.uk
        id: String,
        // section is usually provided: some content (such as user help information)
        // does not belong to any section
        sectionId: Option[String],
        sectionName: Option[String],
        webPublicationDate: DateTime,
        webTitle: String,
        webUrl: String,
        apiUrl: String,
        fields: Option[Map[String, String]],
        tags: List[Tag],
        factboxes: List[Factbox],
        mediaAssets: List[MediaAsset]
)


case class Tag(
        id: String,
        `type` : String,
        sectionId: Option[String],
        sectionName: Option[String],
        webTitle: String,
        webUrl: String,
        apiUrl: String
        ) {
  // for those that don't like backticks
  def tagType = `type`
}

case class Factbox(
        `type`: String,
        heading: Option[String],
        picture: Option[String],
        fields: Option[Map[String, String]]
        ) {
  def factboxType = `type`
}

case class MediaAsset(
        `type`: String,
        rel: String,
        index: Int,
        file: String,
        fields: Option[Map[String, String]]
        ) {
  def mediaAssetType = `type`
}

case class RefinementGroup(
        `type`: String,
        refinements: List[Refinement]
        ) {
  def refinementType = `type`
}

case class Refinement(
        count: Long,
        refinedUrl: String,
        displayName: String,
        id: String,
        apiUrl: String
        )
