package com.gu.openplatform.contentapi.model

import org.joda.time.DateTime


case class Content(
        // the id of this item of content: this should always be the path
        // to the item on www.guardian.co.uk
        id: String,
        // section is usually provided: some content (such as user help information)
        // does not belong to any section so this will be None
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


case class Section(
        id: String,
        webTitle: String,
        webUrl: String,
        apiUrl: String
        )


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
        count: Int,
        refinedUrl: String,
        displayName: String,
        id: String,
        apiUrl: String
        )
