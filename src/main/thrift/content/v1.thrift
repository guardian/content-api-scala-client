namespace scala com.gu.contentapi.client.model.v1

/** date times are represented as i64 - epoch millis */
typedef i64 DateTime

enum EventType {
    Update = 1,
    Delete = 2
}

enum ItemType {
    Content = 1,
    Tag = 2,
    Section = 3,
    Front = 4,
    StoryPackage = 5
}

enum ContentType {
    ARTICLE = 0,
    LIVEBLOG = 1,
    GALLERY = 2,
    INTERACTIVE = 3,
    PICTURE = 4,
    VIDEO = 5
}

enum MembershipTier {
    MEMBERS_ONLY = 0,
    PAID_MEMBERS_ONLY = 1
}

enum AssetType {
    IMAGE = 0,
    VIDEO = 1
}

struct Rights {

    1: required bool syndicatable;

    2: required bool subscriptionDatabases;

    3: required bool developerCommunity;
}

struct AssetFields {

  1: optional string aspectRatio;

  2: optional string altText;

  3: optional bool isInappropriateForAdverts;

  4: optional string caption;

  5: optional string credit;

  6: optional bool embeddable;

  7: optional string photographer;

  8: optional string source;

  9: optional string stillImageUrl;

  10: optional i32 width;

  11: optional string height;

  12: optional string name;

  13: optional string secureFile;
}

struct Asset {

    1: required AssetType assetType;

    2: required string mimeType;

    3: required string url;

    4: optional AssetFields fields;
}

struct BlockAsset {

    1: required string type; // TODO make this an enum?

    2: required string mimeType;

    3: required string file;

    4: required AssetFields assetFields;
}

struct TextElementFields {

    1: optional string html;
}

struct PullquoteElementFields {

    1: optional string html;

    2: optional string attribution;
}

struct TweetElementFields {

    1: optional string source;

    2: optional string url;

    3: optional string id;

    4: optional string html;

    5: optional string originalUrl;
}

struct AudioElementFields {

    1: optional string html;

    2: optional string  source;

    3: optional string description;

    4: optional string title;

    5: optional string credit;

    6: optional string caption;
}

struct VideoElementFields {

    1: optional string url;

    2: optional string description;

    3: optional string title;

    4: optional string html;
}

struct ImageElementFields {

    1: optional string caption;

    2: optional string copyright;

    3: optional bool displayCredit;

    4: optional string credit;

    5: optional string source;

    6: optional string photographer;

    7: optional string alt;

    8: optional string mediaId;

    9: optional string mediaApiUri;

    10: optional string picdarUrn;

    11: optional string suppliersReference;

    12: optional string imageType;
}

struct BlockElement {

    1: required string type; // TODO make this a enum?

    2: required list<BlockAsset> assets;

    3: optional TextElementFields textElementFields;

    4: optional VideoElementFields videoElementFields;

    5: optional TweetElementFields tweetElementFields;

    6: optional ImageElementFields imageElementFields;

    7: optional AudioElementFields audioElementFields;

    8: optional PullquoteElementFields pullquoteElementFields;
}

struct BlockAttributes {

    1: optional bool keyEvent;

    2: optional bool summary;

    3: optional string title;
}

struct User {

    1: required string email;

    2: optional string firstName;

    3: optional string lastName;
}

struct Block {

    1: required string id;

    2: required string bodyHtml;

    3: required string bodyTextSummary;

    4: optional string title;

    5: required BlockAttributes attributes;

    6: required bool published;

    7: optional DateTime createdDate;

    8: optional DateTime firstPublicationDate;

    9: optional DateTime publishedDate;

    10: optional DateTime lastModifiedDate;

    11: required list<string> contributors;

    12: optional User createdBy;

    13: optional User lastModifiedBy;

    14: required list<BlockElement> elements;
}

struct Blocks {

    1: optional Block main;

    2: optional list<Block> body;
}

struct CrosswordDimensions {

    1: required i32 cols;

    2: required i32 rows;
}

struct CrosswordPosition {

    1: required i32 x;

    2: required i32 y;
}

struct CrosswordCreator {

    1: required string name;

    2: required string webUrl;
}

struct SeparatorLocation {

    1: optional string separator;

    2: optional list<i32> locations;
}

struct CrosswordEntry {

    1: required string id;

    2: optional i32 number;

    3: optional string humanNumber;

    4: optional string direction;

    5: optional CrosswordPosition position;

    6: optional list<SeparatorLocation> separatorLocations;

    7: optional i32 length;

    8: optional string clue;

    9: optional list<string> group;

    10: optional string solution;

    11: optional string format;
}

struct Crossword {

    1: required string name;

    2: required string type; // TODO make this an enum?

    3: required i32 number;

    4: required string date;

    5: required CrosswordDimensions dimensions;

    6: required list<CrosswordEntry> entries;

    7: required bool solutionAvailable;

    8: required bool hasNumbers;

    9: required bool randomCluesOrdering;

    10: optional string instructions;

    11: optional CrosswordCreator creator;

    12: optional string pdf;

    13: optional string annotatedSolution;

}

struct Element {

    1: required string id;

    2: required string relation;

    3: required string type; // TODO make this a enum?

    4: optional i32 galleryIndex;

    5: required list<Asset> assets;
}

struct ContentFields {

    1: optional string headline;

    2: optional string standfirst;

    3: optional string trailText;

    4: optional string byline;

    5: optional Block main;

    6: optional list<Block> body;

    7: optional i32 newspaperPageNumber;

    8: optional i16 starRating;

    9: optional string contributorBio;

    10: optional MembershipTier membershipAccess;

    11: optional i32 wordcount;

    12: optional DateTime commentCloseDate;

    13: optional bool commentable;

    14: optional DateTime creationDate;

    15: optional string displayHint;

    16: optional DateTime firstPublicationDate;

    17: optional bool hasStoryPackage;

    18: optional string internalComposerCode;

    19: optional string internalOctopusCode;

    20: optional i32 internalPageCode;

    21: optional i32 internalStoryPackageCode;

    22: optional bool isInappropriateForSponsorship;

    23: optional bool isPremoderated;

    24: optional DateTime lastModified;

    25: optional bool liveBloggingNow;

    26: optional DateTime newspaperEditionDate;

    27: optional string productionOffice;

    28: optional string publication;

    29: optional DateTime scheduledPublicationDate;

    30: optional string secureThumbnail;

    31: optional string shortUrl;

    32: optional bool shouldHideAdverts;

    33: optional bool showInRelatedContent;

    /* The text used when linking to the content, usually derives from the headline */
    /* 34: optional string linkText; */
}

struct Reference {

    1: required string id;

    2: required string type; // TODO make this a enum?
}

struct Podcast {

    1: required string linkUrl;

    2: required string copyright;

    3: required string author;

    4: optional string subscriptionUrl;

    5: required bool explicit;
}

struct Tag {

    1: required string id;

    2: required string type; // TODO make this a enum?

    3: optional string sectionId;

    4: optional string sectionName;

    5: required string webTitle;

    6: required string webUrl;

    7: required string apiUrl;

    8: required list<Reference> references;

    9: optional string description;

    10: optional string bio;

    11: optional string bylineImageUrl;

    12: optional string bylineLargeImageUrl;

    13: optional Podcast podcast;

    14: optional string firstName;

    15: optional string lastName;

    16: optional string emailAddress;

    17: optional string twitterHandle;
}


struct Content {

    1: required string id;

    2: optional string sectionId;

    3: optional string sectionName;

    4: optional DateTime webPublicationDate;

    5: required string webTitle;

    6: required string webUrl;

    7: required string apiUrl;

    8: optional ContentFields fields; // go through these

    9: required list<Tag> tags;

    10: optional list<Element> elements;

    11: required list<Reference> references;

    12: optional bool isExpired;

    13: optional Blocks blocks;

    14: optional Rights rights;

    15: optional Crossword crossword;
}