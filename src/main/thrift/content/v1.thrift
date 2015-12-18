namespace scala com.gu.contentapi.client.model.v1

struct CapiDateTime {

    /*
     * Date times are represented as i64 - epoch millis
     */
    1: required i64 dateTime
}

enum ContentType {
    ARTICLE = 0,
    LIVEBLOG = 1,
    GALLERY = 2,
    INTERACTIVE = 3,
    PICTURE = 4,
    VIDEO = 5,
    CROSSWORD = 6
}

/*
 * The supported Element types, the types inform what fields to expect and how the Element renders as HTML
 */
enum ElementType {

    /*
     * A bog standard text element
     */
    TEXT = 0,

    /*
     * An element with an image, will have Assets
     */
    IMAGE = 1,

    /*
     * Generic embed Element
     */
    EMBED = 2,

    /*
     * an element containing a formstack form
     */
    FORM = 3,

    /*
     * An element containing a text to be treated a pull quote
     */
    PULLQUOTE = 4,

    /*
     * An element containing a javascript interactive (although actual interactivity varies)
     */
    INTERACTIVE = 5,

    /*
     * An embeded comment from discussion
     */
    COMMENT = 6,

    /*
     * A rich link to guardian content - presents a nice trail outside of the contnet body
     */
    RICH_LINK = 7,

    /*
     * A table
     */
    TABLE = 8,

    /*
     * A video element, will contain Assets
     */
    VIDEO = 9,

    /*
     * A tweet element
     */
    TWEET = 10,

    /*
     * An embedded piece of witness UGC
     */
    WITNESS = 11,

    /*
     * An element containing computer codez, this is syntax highlighted.
     * This is used almost exclusively for the developer blog and dogfooding composer.
     */
    CODE = 12,

    /*
     * An audi embed, typically via embedly
     */
    AUDIO = 13,

    /*
     * A map element, embedded via embedly
     */
    MAP = 14,

    /*
     * A document element, ebedded via embedly
     */
    DOCUMENT = 15,

    /*
     * A Guardian Membership event
     */
    MEMBERSHIP = 16
}

enum TagType {

    CONTRIBUTOR = 0,

    KEYWORD = 1,

    SERIES = 2,

    NEWSPAPER_BOOK_SECTION = 3,

    NEWSPAPER_BOOK = 4,

    BLOG = 5,

    TONE = 6,

    TYPE = 7,

    PUBLICATION = 8

}

enum CrosswordType {

    QUICK = 0,

    CRYPTIC = 1,

    QUIPTIC = 2,

    SPEEDY = 3,

    PRIZE = 4,

    EVERYMAN = 5,

    DIAN_QUIPTIC_CROSSWORD = 6
}

enum Office {
    UK = 0,
    US = 1,
    AUS = 2
}

enum AssetType {
    IMAGE = 0,
    VIDEO = 1,
    AUDIO = 2
}

enum MembershipTier {
    MEMBERS_ONLY = 0,
    PAID_MEMBERS_ONLY = 1
}

struct Rights {

    1: required bool syndicatable

    2: required bool subscriptionDatabases

    3: required bool developerCommunity
}

struct AssetFields {

  1: optional string aspectRatio

  2: optional string altText

  3: optional bool isInappropriateForAdverts

  4: optional string caption

  5: optional string credit

  6: optional bool embeddable

  7: optional string photographer

  8: optional string source

  9: optional string stillImageUrl

  10: optional i32 width

  11: optional i32 height

  12: optional string name

  13: optional string secureFile

  14: optional bool isMaster

  15: optional i64 sizeInBytes

  16: optional i32 durationMinutes

  17: optional i32 durationSeconds

  18: optional bool displayCredit

  19: optional string thumbnailUrl

  20: optional string role

  21: optional string mediaId

  22: optional string iframeUrl

  23: optional string scriptName

  24: optional string scriptUrl

  25: optional bool blockAds

  26: optional string html

  27: optional string embedType

  28: optional bool explicit

  29: optional bool clean
}

struct Asset {

    1: required AssetType type

    2: optional string mimeType

    3: optional string file

    4: optional AssetFields typeData
}

struct TextElementFields {

    1: optional string html
}

struct PullquoteElementFields {

    1: optional string html

    2: optional string attribution
}

struct TweetElementFields {

    1: optional string source

    2: optional string url

    3: optional string id

    4: optional string html

    5: optional string originalUrl

    6: optional string role
}

struct AudioElementFields {

    1: optional string html

    2: optional string source

    3: optional string description

    4: optional string title

    5: optional string credit

    6: optional string caption

    7: optional i32 durationMinutes

    8: optional i32 durationSeconds

    9: optional bool clean

    10: optional bool explicit
}

struct VideoElementFields {

    1: optional string url

    2: optional string description

    3: optional string title

    4: optional string html

    5: optional string source

    6: optional string credit

    7: optional string caption

    8: optional i32 height

    9: optional i32 width

    10: optional i32 duration

    11: optional string contentAuthSystem

    12: optional string embeddable

    13: optional bool isInappropriateForAdverts

    14: optional string mediaId

    15: optional string stillImageUrl

    16: optional string thumbnailUrl

    17: optional string shortUrl

    18: optional string role

    19: optional string originalUrl
}

struct ImageElementFields {

    1: optional string caption

    2: optional string copyright

    3: optional bool displayCredit

    4: optional string credit

    5: optional string source

    6: optional string photographer

    7: optional string alt

    8: optional string mediaId

    9: optional string mediaApiUri

    10: optional string picdarUrn

    11: optional string suppliersReference

    12: optional string imageType

    13: optional string comment

    14: optional string role
}

struct InteractiveElementFields {
    1: optional string url
    2: optional string originalUrl
    3: optional string source
    4: optional string caption
    5: optional string alt
    6: optional string scriptUrl
    7: optional string html
    8: optional string scriptName
    9: optional string iframeUrl
}

struct StandardElementFields {
    1: optional string url
    2: optional string originalUrl
    3: optional string source
    4: optional string title
    5: optional string description
    6: optional string credit
    7: optional string caption
    8: optional i32 width
    9: optional i32 height
    10: optional string html
    11: optional string role
}

struct WitnessElementFields {
    1: optional string url
    2: optional string originalUrl
    3: optional string witnessEmbedType
    4: optional string mediaId
    5: optional string source
    6: optional string title
    7: optional string description
    8: optional string authorName
    9: optional string authorUsername
    10: optional string authorWitnessProfileUrl
    11: optional string authorGuardianProfileUrl
    12: optional string caption
    13: optional string alt
    14: optional i32 width
    15: optional i32 height
    16: optional string html
    17: optional string apiUrl
    18: optional string photographer
    19: optional CapiDateTime dateCreated
    20: optional string youtubeUrl
    21: optional string youtubeSource
    22: optional string youtubeTitle
    23: optional string youtubeDescription
    24: optional string youtubeAuthorName
    25: optional string youtubeHtml
    26: optional string role
}

struct RichLinkElementFields {
    1: optional string url
    2: optional string originalUrl
    3: optional string linkText
    4: optional string linkPrefix
    5: optional string role
}

struct MembershipElementFields {
    1: optional string originalUrl
    2: optional string linkText
    3: optional string linkPrefix
    4: optional string title
    5: optional string venue
    6: optional string location
    7: optional string identifier
    8: optional string image
    9: optional string price
    10: optional CapiDateTime start
    11: optional CapiDateTime end
}

struct BlockElement {

    1: required ElementType type

    2: required list<Asset> assets

    3: optional TextElementFields textTypeData

    4: optional VideoElementFields videoTypeData

    5: optional TweetElementFields tweetTypeData

    6: optional ImageElementFields imageTypeData

    7: optional AudioElementFields audioTypeData

    8: optional PullquoteElementFields pullquoteTypeData

    9: optional InteractiveElementFields interactiveTypeData

    10: optional StandardElementFields mapTypeData

    11: optional StandardElementFields documentTypeData

    12: optional StandardElementFields tableTypeData

    13: optional WitnessElementFields witnessTypeData

    14: optional RichLinkElementFields richLinkTypeData

    15: optional MembershipElementFields membershipTypeData
}

struct BlockAttributes {

    1: optional bool keyEvent

    2: optional bool summary

    3: optional string title
}

struct User {

    1: required string email

    2: optional string firstName

    3: optional string lastName
}

struct Block {

    /*
     * The unique ID of the block.
     */
    1: required string id

    /*
     * The HTML body of the block.
     */
    2: required string bodyHtml

    /*
     * The textual content of the block, with HTML tags stripped.
     * This will not include any non-textual content such as pullquotes, tweet embeds, etc.
     */
    3: required string bodyTextSummary

    /*
     * The block's title, if it has one.
     */
    4: optional string title

    /*
     * Metadata about the block.
     */
    5: required BlockAttributes attributes

    /*
     * Whether this block is currently live.
     */
    6: required bool published

    /*
     * The first time this block was created.
     */
    7: optional CapiDateTime createdDate

    /*
     * The first time this block was published.
     */
    8: optional CapiDateTime firstPublishedDate

    /*
     * The last time this block was published.
     */
    9: optional CapiDateTime publishedDate

    /*
     * The last time this block was modified.
     */
    10: optional CapiDateTime lastModifiedDate

    /*
     * People who contributed to this block.
     */
    11: required list<string> contributors

    /*
     * Person who created this block.
     */
    12: optional User createdBy

    /*
     * Person who last modified this block.
     */
    13: optional User lastModifiedBy

    /*
     * The elements associated with this block.
     */
    14: required list<BlockElement> elements = []
}

struct Blocks {

    /*
     * The main block, which will include the main image and other furniture
     */
    1: optional Block main

    /*
     * The block(s) that make up the body of the content. For a liveblog there may be multiple blocks.
     * Any other content will have only one block.
     */
    2: optional list<Block> body
}

struct CrosswordDimensions {

    1: required i32 cols

    2: required i32 rows
}

struct CrosswordPosition {

    1: required i32 x

    2: required i32 y
}

struct CrosswordCreator {

    1: required string name

    2: required string webUrl
}

struct SeparatorLocation {

    1: optional string separator

    2: optional list<i32> locations
}

struct CrosswordEntry {

    1: required string id

    2: optional i32 number

    3: optional string humanNumber

    4: optional string direction

    5: optional CrosswordPosition position

    6: optional list<SeparatorLocation> separatorLocations

    7: optional i32 length

    8: optional string clue

    9: optional list<string> group

    10: optional string solution

    11: optional string format
}

struct Crossword {

    1: required string name

    2: required CrosswordType type

    3: required i32 number

    4: required CapiDateTime date

    5: required CrosswordDimensions dimensions

    6: required list<CrosswordEntry> entries

    7: required bool solutionAvailable

    8: required bool hasNumbers

    9: required bool randomCluesOrdering

    10: optional string instructions

    11: optional CrosswordCreator creator

    12: optional string pdf

    13: optional string annotatedSolution
}

struct Element {

    1: required string id

    2: required string relation

    3: required ElementType type

    4: optional i32 galleryIndex

    5: required list<Asset> assets
}

struct ContentFields {

    1: optional string headline

    2: optional string standfirst

    3: optional string trailText

    4: optional string byline

    5: optional string main

    6: optional string body

    7: optional i32 newspaperPageNumber

    8: optional i32 starRating

    9: optional string contributorBio

    10: optional MembershipTier membershipAccess

    11: optional i32 wordcount

    12: optional CapiDateTime commentCloseDate

    13: optional bool commentable

    14: optional CapiDateTime creationDate

    15: optional string displayHint

    16: optional CapiDateTime firstPublicationDate

    17: optional bool hasStoryPackage

    18: optional string internalComposerCode

    19: optional string internalOctopusCode

    20: optional i32 internalPageCode

    21: optional i32 internalStoryPackageCode

    22: optional bool isInappropriateForSponsorship

    23: optional bool isPremoderated

    24: optional CapiDateTime lastModified

    25: optional bool liveBloggingNow

    26: optional CapiDateTime newspaperEditionDate

    27: optional Office productionOffice

    28: optional string publication

    29: optional CapiDateTime scheduledPublicationDate

    30: optional string secureThumbnail

    31: optional string shortUrl

    32: optional bool shouldHideAdverts

    33: optional bool showInRelatedContent

    34: optional string thumbnail

    35: optional bool legallySensitive

    36: optional bool allowUgc

    37: optional bool sensitive
}

struct Reference {

    1: required string id

    2: required string type
}

struct Podcast {

    1: required string linkUrl

    2: required string copyright

    3: required string author

    4: optional string subscriptionUrl

    5: required bool explicit

    6: optional string image
}

struct Tag {

    /*
     * The id of this tag: this should always be the path
     * to the tag page on www.theguardian.com
     */
    1: required string id

    /*
     * The type of this tag
     */
    2: required TagType type

    /*
     * Section is usually provided: some tags (notably contributor tags)
     * does not belong to any section so this will be None
     */
    3: optional string sectionId

    /*
     * The display name of the section.  Will be None if sectionId is None.
     */
    4: optional string sectionName

    /*
     * Short description of this tag.
     */
    5: required string webTitle

    /*
     * Full url on which tag page can be found on www.theguardian.com
     */
    6: required string webUrl

    /*
     * Full url on which full information about this tag can be found on
     * the content api.
     *
     * For tags, this allows access to the editorsPicks for the tag,
     * and automatically shows the most recent content for the tag.
     */
    7: required string apiUrl

    /*
     * List of references associated with the tag. References are
     * strings that identify things beyond the content api. A good example
     * is an isbn number, which associates the tag with a book.
     *
     * Use showReferences passing in the the type of reference you want to
     * see or 'all' to see all references.
     */
    8: required list<Reference> references

    /**
     * A tag *may* have a description field.
     *
     * Contributor tags never have a description field. They may
     * instead have a 'bio' field.
     */
    9: optional string description

    /**
     * If this tag is a contributor then we *may* have a small bio
     * for the contributor.
     *
     * This field is optional in all cases, even contributors are not
     * guaranteed to have one.
     */
    10: optional string bio

    /*
     * If this tag is a contributor then we *may* have a small byline
     * picturefor the contributor.
     *
     * This field is optional in all cases, even contributors are not
     * guaranteed to have one.
     */
    11: optional string bylineImageUrl

    /**
     * If this tag is a contributor then we *may* have a large byline
     * picture for the contributor.
     */
    12: optional string bylineLargeImageUrl

    /*
     * If this tag is a series it could be a podcast.
     */
    13: optional Podcast podcast

    /*
     * If the tag is a contributor it may have a first name, a last name, email address and a twitter handle.
     */
    14: optional string firstName

    15: optional string lastName

    16: optional string emailAddress

    17: optional string twitterHandle
}


struct Content {

    /*
     * The id of this item of content: this should always be the path to the item on www.theguardian.com
     */
    1: required string id

    /*
     * The content type of the content. Defaults to article if none is specified.
     */
    2: required ContentType type = ContentType.ARTICLE

    /*
     * Section is usually provided: some content (such as user help information)
     * does not belong to any section so this will be None
     */
    3: optional string sectionId

    /*
     * The display name of the section. Will be None if sectionId is None.
     */
    4: optional string sectionName

    /*
     * The date and time when this content was published to the web. Note that
     * editors can set this field manually so does not necessarily exactly match
     * when it actually appeared on the web. Current convention is that when
     * "significant updates" are made to a story the web publication date is
     * updated.
     */
    5: optional CapiDateTime webPublicationDate

    /*
     * Short description of this item of content.
     */
    6: required string webTitle

    /*
     * Full url on which the content can be found on www.theguardian.com
     */
    7: required string webUrl

    /*
     * Full url on which full information about this content can be found on
     * the content api. You need to access this to find, e.g. related content
     * for the item.
     */
    8: required string apiUrl

    /*
     * Optional field list containing other variable information about this
     * content. Fields are only returned if you specify showFields("xxx") on the request
     * with either a comma separated list of fields or "all".
     *
     * Note that the set of fields returned vary per item of content, and may
     * vary over time as the api evolves (although we will make every effort
     * to maintain compatibility, we do not promise it).
     */
    9: optional ContentFields fields

    /*
     * List of tags associated with this content.
     *
     * Only returned if you specify showTags("xxx") on the request
     * with either a comma separated list of tag types or "all".
     *
     * The order of tags is significant; tags towards the top of the list
     * are considered editorially more important than those towards the end.
     */
    10: required list<Tag> tags = []

    /*
     * New representation to elements (assets lists) only returns if show-elements("all")
     * or show-elements("image") is specified
     */
    11: optional list<Element> elements

    /*
     * List of references associated with the content. References are
     * strings that identify things beyond the content api. A good example
     * is an isbn number, which associates a piece of content with a book.
     *
     * Use showReferences passing in the the type of reference you want to
     * see or 'all' to see all references.
     */
    12: required list<Reference> references = []

    /*
     * Set to true if the rights to this content have expired. Expired
     * content is only available to internal users.
     */
    13: optional bool isExpired

    /*
     * The blocks that make up a piece of content.
     */
    14: optional Blocks blocks

    15: optional Rights rights

    16: optional Crossword crossword
}

struct Edition {

    /*
     * he path of the edition, e.g. 'au/business'
     */
    1: required string id

    /*
     * Short description of the edition
     */
    2: required string webTitle

    /*
     * Edition URL for the main Guardian website
     */
    3: required string webUrl

    /*
     * Path from which the edition is available in the Content API
     */
    4: required string apiUrl

    /*
     * The edition code, e.g. 'uk' or 'default'.
     */
    5: required string code
}

struct Section {

    /*
     * The id of this section: this should always be the path to the section front on www.theguardian.com
     */
    1: required string id

    /*
     * Short description of this section.
     */
    2: required string webTitle

    /*
     * Full url on which section front can be found on www.theguardian.com
     */
    3: required string webUrl

    /*
     * Full url on which full information about this section can be found on
     * the content api.
     *
     * For sections, this allows access to the editorsPicks for the section,
     * mostRead content in the section,
     * and automatically shows the most recent content for the section.
     */
    4: required string apiUrl

    /*
     * List of available editions for this section
     */
    5: required list<Edition> editions
}

struct NetworkFront {

    /*
     * The id of the network front, e.g. 'au'
     */
    1: required string id

    /*
     * The path of the network front, e.g. 'au'
     */
    2: required string path

    /*
     * The edition code of the network front, e.g. 'AU'
     */
    3: required string edition

    /*
     * Short description of the edition
     */
    4: required string webTitle

    /*
     * Edition URL for the main Guardian website
     */
    5: required string webUrl

    /*
     * Path from which the edition is available in the Content API
     */
    6: required string apiUrl
}
