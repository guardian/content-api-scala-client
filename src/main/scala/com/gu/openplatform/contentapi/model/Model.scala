package com.gu.openplatform.contentapi.model

import org.joda.time.DateTime
import java.net.URL


class Response(
        val format: String,
        val status: String,
        val userTier: String
        )

class PagedResponse(
        format: String,
        status: String,
        userTier: String,
        val startIndex: Int,
        val currentPage: Int,
        val pages: Int,
        val pageSize: Int,
        val total: Int
        ) extends Response (format, status, userTier)

// "/search" endpoint
case class SearchResponse(
        override val format: String,
        override val status: String,
        override val userTier: String,
        override val startIndex: Int,
        override val currentPage: Int,
        override val pages: Int,
        override val pageSize: Int,
        override val total: Int,
        val orderBy: String,
        val results: List[Content],
        val refinementGroups: List[RefinementGroup]
        ) extends PagedResponse(format, status, userTier, startIndex, currentPage, pages, pageSize, total)

// "/tags"
case class TagsResponse(
        override val format: String,
        override val status: String,
        override val userTier: String,
        override val startIndex: Int,
        override val currentPage: Int,
        override val pages: Int,
        override val pageSize: Int,
        override val total: Int,
        val results: List[Tag]
        ) extends PagedResponse(format, status, userTier, startIndex, currentPage, pages, pageSize, total)

// "/sections"
case class SectionsResponse(
        override val format: String,
        override val status: String,
        override val userTier: String,
        val total: Int,
        val results: List[Section]
        ) extends Response(format, status, userTier)

// "/<id>" look up a single item by id endpoint
case class ItemResponse(
        override val format: String,
        override val status: String,
        override val userTier: String,
        val startIndex: Option[Int],
        val currentPage: Option[Int],
        val pages: Option[Int],
        val pageSize: Option[Int],
        val total: Option[Int],
        val content: Option[Content],
        val tag: Option[Tag],
        val section: Option[Section],
        val results: List[Content]
        ) extends Response (format, status, userTier)

case class Content(
        val id: String,
        val sectionId: Option[String],
        val sectionName: Option[String],
        val webPublicationDate: DateTime,
        val webTitle: String,
        val webUrl: URL,
        val apiUrl: URL,
        val fields: Map[String, String],
        val tags : List[Tag],
        val factboxes : List[Factbox],
        val mediaAssets : List[MediaAsset]
        )

case class Tag(
        val id: String,
        val tagType: String, // "type" in xml and json
        val sectionId: Option[String],
        val sectionName: Option[String],
        val webTitle: String, // external name
        val webUrl: URL,
        val apiUrl: URL
        )

case class Section(
        val id: String,
        val webTitle: String, // external name
        val webUrl: URL,
        val apiUrl: URL
        )

case class Factbox(
        val heading : Option[String],
        val factboxType : String,
        val picture : Option[String],
        val fields: Map[String, String]
        )

case class MediaAsset(
        val mediaType : String,
        val relationship : String,
        val index : Int,
        val file : String,
        val fields: Map[String, String]
        )

case class RefinementGroup(
        val refinementType: String, // "type" in xml and json
        val refinements: List[Refinement]
        )

case class Refinement(
        val count: Int,
        val refinedUrl: URL,
        val displayName: String,
        val id: String,
        val apiUrl: URL
        )
