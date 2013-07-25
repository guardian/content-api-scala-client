package com.gu.openplatform.contentapi.model

import org.joda.time.DateTime


// NB: although I've used javadoc syntax here, alas in 2.8.0 this information
// on case class constructors doesn't get put anywhere useful.
//
// However, it looks like in 2.8.1 or possibly later this will be fixed, see
// http://scala-programming-language.1934581.n4.nabble.com/scaladoc-and-constructor-parameters-td2328652.html
case class Content(

        /**
         * The id of this item of content: this should always be the path
         * to the item on www.guardian.co.uk
         */
        id: String,

        /**
         * Section is usually provided: some content (such as user help information)
         * does not belong to any section so this will be None
         */
        sectionId: Option[String],

        /**
         * The display name of the section.  Will be None iff sectionId is None.
         */
        sectionName: Option[String],

        /**
         * The date and time when this content was published to the web. Note that
         * editors can set this field manually so does not necessarily exactly match
         * when it actually appeared on the web. Current convention is that when
         * "significant updates" are made to a story the web publication date is
         * updated.
         */
        webPublicationDate: DateTime,

        /**
         * Short description of this item of content.
         */
        webTitle: String,

        /**
         * Full url on which the content can be found on www.guardian.co.uk
         */
        webUrl: String,

        /**
         * Full url on which full information about this content can be found on
         * the content api. You need to access this to find, e.g. related content
         * for the item.
         */
        apiUrl: String,

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
        fields: Option[Map[String, String]] = None,

        /**
         * List of tags associated with this content.
         *
         * Only returned if you specify showTags("xxx") on the request
         * with either a comma separated list of tag types or "all".
         *
         * The order of tags is significant; tags towards the top of the list
         * are considered editorially more important than those towards the end.
         */
        tags: List[Tag] = Nil,

        /**
         * List of factboxes associated with this content.
         *
         * Only returned if you specify showFactboxes("xxx") on the request
         * with either a comma separated list of factbox types or "all".
         *
         * Factboxes contain arbitary additional information associated with the content
         * (for example, the ISBN of a reviewed book) and are typically displayed
         * on the right hand side of the content on the website.
         */
        factboxes: List[Factbox] = Nil,

        /**
         * List of media assets associated with this content.
         *
         * Only returned if you specify showMedia("xxx") on the request
         * with either a comma separated list of media types or "all".
         *
         * Note that at time of writing media assets are only available to partners
         * of guardian.co.uk with Bespoke api keys. See
         * http://www.guardian.co.uk/open-platform/faq for information.
         */
        mediaAssets: List[MediaAsset] = Nil,

        /**
         * New representation to elements (assets lists) only returns if show-elements("all") or show-elements("image") is specified
         */
        elements: Option[Map[String, Element]],

        /**
         * List of snippets that matched the requested query parameters.
         * This allows google-style highlighting of matches in results.
         *
         * Only returned if you specify showSnippets("xxx") on the request
         * with either a comma separated list of field names to show snippets
         * for, or "all".
         *
         * Note that at time of writing snippeting is only available
         * internally to us at guardian.co.uk, while we refine its use case
         * and operation. Please post to the open platform mailing
         * list at http://groups.google.com/group/guardian-api-talk/ if
         * you want to use it.
         *
         * (It's included in this client library because we use this
         * client library internally :)
         */
        snippets: Option[Map[String, String]] = None,

        /**
         * List of references associated with the content. References are
         * strings that identify things beyond the content api. A good example
         * is an isbn number, which associates a piece of content with a book.
         *
         * Use showReferences passing in the the type of reference you want to
         * see or 'all' to see all references.
         */
        references: List[Reference] = Nil,

        /**
         * Set to true if the rights to this content have expired. Expired
         * content is only available to internal users.
         */
        isExpired: Option[Boolean] = None
) {
  // Unfortunately lift-json (as of 2.1) requires a Map to be wrapped in an
  // Option if the json field that contains it is optional. (This is unlike a list
  // where if the field itself is missing it just returns Nil.)
  //
  // Use these accessors to ignore the Optionability of the Map itself.
  def safeFields = fields getOrElse Map()
  def safeSnippets = fields getOrElse Map()
}


case class Tag(
        /**
         * The id of this tag: this should always be the path
         * to the tag page on www.guardian.co.uk
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
         * Full url on which tag page can be found on www.guardian.co.uk
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
          bylineImageUrl: Option[String] = None
        ) {

  /**
   * Same as `type` but without needing the backticks
   */
  def tagType = `type`
}

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
        code: String
        )

case class Section(
        /**
         * The id of this section: this should always be the path
         * to the section front on www.guardian.co.uk
         */
        id: String,

        /**
         * Short description of this section.
         */
        webTitle: String,

        /**
         * Full url on which section front can be found on www.guardian.co.uk
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
        editions: List[Edition]
        )

case class Folder(
                    /**
                     * The id of this folder.  Note that folders may not have a
                     * direct representation on the website, they are simply
                     * collections of tags for editorial use.
                     */
                    id: String,

                    /**
                     * Short description of this folder.
                     */
                    webTitle: String,

                    /**
                     * Full url on which full information about this folder can be found on
                     * the content api.
                     *
                     */
                    apiUrl: String,

                    /**
                     * Section is usually provided: some folders do not belong
                     * to any section so this will be None
                     */
                    sectionId: Option[String] = None,

                    /**
                     * The display name of the section.  Will be None iff sectionId is None.
                     */
                    sectionName: Option[String] = None

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
        file: Option[String],
        fields: Option[Map[String, String]],
        encodings: List[MediaEncoding] = Nil
        ) {
  def mediaAssetType = `type`
}

case class MediaEncoding(
        format: String,
        file: String
        )

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

case class BestBet(
        webTitle: String,
        webUrl: String,
        trailText: Option[String]
        )

case class Reference(
        `type`: String,
        id: String
        )

case class Element(
            id: String,
            relation: String,
            `type`: String,
            assets: List[Asset]
          ) {
  def elementType = `type`
}

case class Asset(
            `type`: String,
            mimeType: Option[String],
            file: Option[String],
            typeData: Map[String, String]
         ) {
  def assetType = `type`
}
