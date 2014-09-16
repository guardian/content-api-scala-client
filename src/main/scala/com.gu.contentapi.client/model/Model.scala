package com.gu.contentapi.client.model

import org.joda.time.DateTime

sealed trait ContentType {

  /**
  * The id of this item of content: this should always be the path
  * to the item on www.theguardian.com
  */
  val id: String

  /**
  * Section is usually provided: some content (such as user help information)
  * does not belong to any section so this will be None
  */
  val sectionId: Option[String]

  /**
  * The display name of the section.  Will be None iff sectionId is None.
  */
  val sectionName: Option[String]

  /**
  * The date and time when this content was published to the web. Note that
  * editors can set this field manually so does not necessarily exactly match
  * when it actually appeared on the web. Current convention is that when
  * "significant updates" are made to a story the web publication date is
  * updated.
  */
  val webPublicationDate: DateTime

  /**
  * Short description of this item of content.
  */
  val webTitle: String

  /**
  * Full url on which the content can be found on www.theguardian.com
  */
  val webUrl: String

  /**
  * Full url on which full information about this content can be found on
  * the content api. You need to access this to find, e.g. related content
  * for the item.
  */
  val apiUrl: String

  /**
  * Optional field list containing other variable information about this
  * content. (see safeFields for a more usable accessor).
  * Fields are only returned if you specify showFields("xxx") on the request
  * with either a comma separated list of fields or "all".
  *
  * Note that the set of fields returned vary per item of content, and may
  * vary over time as the api evolves (although we will make every effort
  * to maintain compatibility, we do not promise it).
  */
  val fields: Option[Map[String, String]]

  /**
  * List of tags associated with this content.
  *
  * Only returned if you specify showTags("xxx") on the request
  * with either a comma separated list of tag types or "all".
  *
  * The order of tags is significant; tags towards the top of the list
  * are considered editorially more important than those towards the end.
  */
  val tags: List[Tag]

  /**
  * New representation to elements (assets lists) only returns if show-elements("all") or show-elements("image") is specified
  */
  val elements: Option[List[Element]]

  /**
  * List of references associated with the content. References are
  * strings that identify things beyond the content api. A good example
  * is an isbn number, which associates a piece of content with a book.
  *
  * Use showReferences passing in the the type of reference you want to
  * see or 'all' to see all references.
  */
  val references: List[Reference]

  /**
  * Set to true if the rights to this content have expired. Expired
  * content is only available to internal users.
  */
  val isExpired: Option[Boolean]

  // Unfortunately lift-json (as of 2.1) requires a Map to be wrapped in an
  // Option if the json field that contains it is optional. (This is unlike a list
  // where if the field itself is missing it just returns Nil.)
  //
  // Use these accessors to ignore the Optionability of the Map itself.
  def safeFields = fields getOrElse Map()

}

case class Content(
    id: String,
    sectionId: Option[String],
    sectionName: Option[String],
    webPublicationDateOption: Option[DateTime],
    webTitle: String,
    webUrl: String,
    apiUrl: String,
    fields: Option[Map[String, String]] = None,
    tags: List[Tag] = Nil,
    elements: Option[List[Element]],
    references: List[Reference] = Nil,
    isExpired: Option[Boolean] = None) extends ContentType {

    // This is here for backwards compatibility. For the vast majority of use cases
    // there WILL be a webPublicationDate. If this causes problems you should be using
    // webPublicationDateOption
    lazy val webPublicationDate: DateTime = webPublicationDateOption.head
}

case class CuratedContent(
    id: String,
    sectionId: Option[String],
    sectionName: Option[String],
    webPublicationDate: DateTime,
    webTitle: String,
    webUrl: String,
    apiUrl: String,
    fields: Option[Map[String, String]] = None,
    tags: List[Tag] = Nil,
    elements: Option[List[Element]],
    references: List[Reference] = Nil,
    isExpired: Option[Boolean] = None,
    metadata: Option[CuratedMetadata]) extends ContentType

case class SupportingContent(
    id: String,
    sectionId: Option[String],
    sectionName: Option[String],
    webPublicationDate: DateTime,
    webTitle: String,
    webUrl: String,
    apiUrl: String,
    fields: Option[Map[String, String]] = None,
    tags: List[Tag] = Nil,
    elements: Option[List[Element]],
    references: List[Reference] = Nil,
    isExpired: Option[Boolean] = None,
    metadata: Option[SupportingMetadata]) extends ContentType

case class Tag(

    /**
    * The id of this tag: this should always be the path
    * to the tag page on www.theguardian.com
    */
    id: String,

    /**
    * The type of this tag
    */
    `type` : String,

    /**
    * Section is usually provided: some tags (notably contributor tags)
    * does not belong to any section so this will be None
    */
    sectionId: Option[String] = None,

    /**
    * The display name of the section.  Will be None iff sectionId is None.
    */
    sectionName: Option[String] = None,

    /**
    * Short description of this tag.
    */
    webTitle: String,

    /**
    * Full url on which tag page can be found on www.theguardian.com
    */
    webUrl: String,

    /**
    * Full url on which full information about this tag can be found on
    * the content api.
    *
    * For tags, this allows access to the editorsPicks for the tag,
    * and automatically shows the most recent content for the tag.
    */
    apiUrl: String,

    /**
    * List of references associated with the tag. References are
    * strings that identify things beyond the content api. A good example
    * is an isbn number, which associates the tag with a book.
    *
    * Use showReferences passing in the the type of reference you want to
    * see or 'all' to see all references.
    */
    references: List[Reference] = Nil,

    /**
     * A tag *may* have a description field.
     *
     * Contributor tags never have a description field. They may
     * instead have a 'bio' field.
     */
    description: Option[String] = None,

    /**
    * If this tag is a contributor then we *may* have a small bio
    * for the contributor.
    *
    * This field is optional in all cases, even contributors are not
    * guaranteed to have one.
    */
    bio: Option[String] = None,

    /**
    * If this tag is a contributor then we *may* have a small byline
    * picturefor the contributor.
    *
    * This field is optional in all cases, even contributors are not
    * guaranteed to have one.
    */
    bylineImageUrl: Option[String] = None,

    /**
    * If this tag is a contributor then we *may* have a large byline
    * picture for the contributor.
    */
    bylineLargeImageUrl: Option[String] = None,

    /**
    * If this tag is a series it could be a podcast.
    */
    podcast: Option[Podcast] = None)

case class Edition(

    /**
    * The path of the edition, e.g. 'au/business'
    */
    id: String,

    /**
    * Short description of the edition
    */
    webTitle: String,

    /**
    * Edition URL for the main Guardian website
    */
    webUrl: String,

    /**
    * Path from which the edition is available in the Content API
    */
    apiUrl: String,

    /**
    * The edition code, e.g. 'uk' or 'default'.
    */
    code: String)

case class Section(

    /**
    * The id of this section: this should always be the path
    * to the section front on www.theguardian.com
    */
    id: String,

    /**
    * Short description of this section.
    */
    webTitle: String,

    /**
    * Full url on which section front can be found on www.theguardian.com
    */
    webUrl: String,

    /**
    * Full url on which full information about this section can be found on
    * the content api.
    *
    * For sections, this allows access to the editorsPicks for the section,
    * mostRead content in the section,
    * and automatically shows the most recent content for the section.
    */
    apiUrl: String,

    /**
    * List of available editions for this section
    */
    editions: List[Edition])

case class Collection(
    id : String,
    `type`: String,
    title: Option[String],
    groups: List[String],
    lastModified: DateTime,
    modifiedBy: String,
    curatedContent: List[CuratedContent],
    backfill: Option[String])

case class MediaEncoding(
    format: String,
    file: String)

case class Reference(
    `type`: String,
    id: String)

case class Element(
    id: String,
    relation: String,
    `type`: String,
    galleryIndex : Option[Int] = None,
    assets: List[Asset])

case class Asset(
    `type`: String,
    mimeType: Option[String],
    file: Option[String],
    typeData: Map[String, String])

sealed trait Metadata {
  val trailText: Option[String]
  val headline: Option[String]
  val imageAdjustment: Option[String]
}

case class CuratedMetadata(
    trailText: Option[String],
    headline: Option[String],
    imageAdjustment: Option[String],
    group: Option[Int],
    supportingContent: List[SupportingContent]) extends Metadata

case class SupportingMetadata(
    trailText: Option[String],
    headline: Option[String],
    imageAdjustment: Option[String]) extends Metadata

case class Podcast(
    linkUrl: String,
    copyright: String,
    author: String,
    subscriptionUrl: Option[String],
    explicit: Boolean)
