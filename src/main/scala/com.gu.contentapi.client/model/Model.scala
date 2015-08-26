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
    `type`: String = "article",
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
    isExpired: Option[Boolean] = None,
    blocks: Option[Blocks] = None,
    rights: Option[Rights] = None,
    crossword: Option[Crossword] = None
    ) extends ContentType {

    // This is here for backwards compatibility. For the vast majority of use cases
    // there WILL be a webPublicationDate. If this causes problems you should be using
    // webPublicationDateOption
    lazy val webPublicationDate: DateTime = webPublicationDateOption.head
}

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
    podcast: Option[Podcast] = None,

    /**
     * If the tag is a contributor it may have a first name, a last name and a twitter handle.
     */
    firstName: Option[String] = None,

    lastName: Option[String] = None,

    emailAddress: Option[String] = None,

    twitterHandle: Option[String] = None)

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


case class NetworkFront(

    /**
     * The id of the network front, e.g. 'au'
     */
    id: String,

    /**
     * The path of the network front, e.g. 'au'
     */
    path: String,

    /**
     * The edition code of the network front, e.g. 'AU'
     */
    edition: String,

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
    apiUrl: String)

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

case class Podcast(
    linkUrl: String,
    copyright: String,
    author: String,
    subscriptionUrl: Option[String],
    explicit: Boolean)

/**
 * The blocks that make up a piece of content.
 * @param main The main block, which will include the main image and other furniture
 * @param body The block(s) that make up the body of the content.
 *             For a liveblog there may be multiple blocks. Any other content will have only one block.
 */
case class Blocks(main: Option[Block],
                  body: Option[Seq[Block]])

/**
 * A block of content.
 * @param id a unique ID
 * @param bodyHtml the HTML body of the block
 * @param bodyTextSummary the textual content of the block, with HTML tags stripped.
 *                        This will not include any non-textual content such as pullquotes, tweet embeds, etc.
 * @param title the block's title, if it has one
 * @param attributes metadata about the block
 *                   e.g. this will contain "keyEvent" -> "true" if the block is a key event,
 *                   or "summary" -> "true" if it is a summary
 * @param published whether this block is currently live
 * @param createdDate the first time this block was created
 * @param firstPublishedDate the first time this block was published
 * @param publishedDate the last time this block was published
 * @param lastModifiedDate the last time this block was modified
 * @param contributors people who contributed to this block
 * @param createdBy person who created this block
 * @param lastModifiedBy person who last modified this block
 */
case class Block(id: String,
                 bodyHtml: String,
                 bodyTextSummary: String,
                 title: Option[String],
                 attributes: Map[String, String],
                 published: Boolean,
                 createdDate: Option[DateTime],
                 firstPublishedDate: Option[DateTime],
                 publishedDate: Option[DateTime],
                 lastModifiedDate: Option[DateTime],
                 contributors: Seq[String],
                 createdBy: Option[User],
                 lastModifiedBy: Option[User],
                 elements: Seq[BlockElement] = Nil)

case class User(
  email: String,
  firstName: Option[String],
  lastName: Option[String])


case class BlockElement(
  `type`: String,
  assets: Seq[BlockAsset] = Nil,
  textTypeData: Option[TextTypeData] = None,
  videoTypeData: Option[VideoTypeData] = None,
  tweetTypeData: Option[TweetTypeData] = None,
  imageTypeData: Option[ImageTypeData] = None,
  audioTypeData: Option[AudioTypeData] = None,
  pullquoteTypeData: Option[PullquoteTypeData] = None
)

case class BlockAsset(
 `type`: String,
  mimeType: String,
  file: String,
  typeData: AssetTypeData
)

case class TextTypeData(
  html: Option[String]
)

case class PullquoteTypeData(
 html: Option[String],
 attribution: Option[String]
)

case class TweetTypeData(
  source: Option[String],
  url: Option[String],
  id: Option[String],
  html: Option[String],
  originalUrl: Option[String]
)

case class AudioTypeData(
  html: Option[String],
  source: Option[String],
  description: Option[String],
  title: Option[String],
  credit: Option[String],
  caption: Option[String]
)

case class VideoTypeData(
  url: Option[String],
  description: Option[String],
  title: Option[String],
  html: Option[String]
)

case class ImageTypeData(
  caption: Option[String],
  copyright: Option[String],
  displayCredit: Option[Boolean],
  credit: Option[String],
  source: Option[String],
  photographer: Option[String],
  alt: Option[String],
  mediaId: Option[String],
  mediaApiUri: Option[String],
  picdarUrn: Option[String],
  suppliersReference: Option[String],
  imageType: Option[String],
  isMaster: Option[Boolean]
)

case class AssetTypeData(
  aspectRatio: Option[String],
  altText: Option[String],
  isInappropriateForAdverts: Option[Boolean],
  caption: Option[String],
  credit: Option[String],
  embeddable: Option[Boolean],
  photographer: Option[String],
  source: Option[String],
  stillImageUrl: Option[String],
  width: Option[Int],
  height: Option[Int],
  name: Option[String],
  secureFile: Option[String]
)

case class Rights(subscriptionDatabases: Boolean = false,
                  developerCommunity: Boolean = false,
                  syndicatable: Boolean = false)

case class Crossword(
  name: String,
  `type`: String,
  number: Int,
  date: String, // yyyy-mm-dd
  dimensions: CrosswordDimensions,
  entries: Seq[CrosswordEntry],
  solutionAvailable: Boolean,
  hasNumbers: Boolean,
  randomCluesOrdering: Boolean,
  instructions: Option[String],
  creator: Option[CrosswordCreator],
  pdf: Option[String],
  annotatedSolution: Option[String])

case class CrosswordDimensions(cols: Int, rows: Int)

case class CrosswordEntry(
  id: String,
  number: Option[Int],
  humanNumber: Option[String],
  direction: Option[String],
  position: Option[CrosswordPosition],
  separatorLocations: Option[Map[String, Seq[Int]]],
  length: Option[Int],
  clue: Option[String],
  group: Option[Seq[String]],
  solution: Option[String],
  format: Option[String])

case class CrosswordCreator(name: String, webUrl: String)

case class CrosswordPosition(x: Int, y: Int)

