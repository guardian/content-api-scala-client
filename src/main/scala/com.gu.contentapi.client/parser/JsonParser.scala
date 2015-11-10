package com.gu.contentapi.client.parser

import com.gu.contentapi.client.model.v1._
import com.twitter.scrooge.ThriftEnum
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import org.json4s.{CustomSerializer, DefaultFormats}
import org.json4s.JsonAST._
import org.json4s.native.JsonMethods
import com.gu.contentapi.client.model._

object JsonParser {

  implicit val formats = DefaultFormats + ContentTypeSerializer + DateTimeSerializer +
    MembershipTierSerializer + OfficeSerializer + AssetTypeSerializer + ElementTypeSerializer +
    TagTypeSerializer + CrosswordTypeSerializer

  def parseItem(json: String): ItemResponse = {
    (JsonMethods.parse(json) \ "response").transformField(fixFields).extract[ItemResponse]
  }

  def parseSearch(json: String): SearchResponse = {
    (JsonMethods.parse(json) \ "response").transformField(fixFields).extract[SearchResponse]
  }

  def parseRemovedContent(json: String): RemovedContentResponse = {
    (JsonMethods.parse(json) \ "response").extract[RemovedContentResponse]
  }

  def parseTags(json: String): TagsResponse = {
    (JsonMethods.parse(json) \ "response").extract[TagsResponse]
  }

  def parseSections(json: String): SectionsResponse = {
    (JsonMethods.parse(json) \ "response").extract[SectionsResponse]
  }

  def parseEditions(json: String): EditionsResponse = {
    (JsonMethods.parse(json) \ "response").extract[EditionsResponse]
  }

  def parseError(json: String): Option[ErrorResponse] = for {
    parsedJson <- JsonMethods.parseOpt(json)
    response = parsedJson \ "response"
    errorResponse <- response.extractOpt[ErrorResponse]
  } yield errorResponse

  private def fixFields: PartialFunction[JField, JField] = {
    case JField("showInRelatedContent", JString(s)) => JField("showInRelatedContent", JBool(s.toBoolean))
    case JField("shouldHideAdverts", JString(s)) => JField("shouldHideAdverts", JBool(s.toBoolean))
    case JField("hasStoryPackage", JString(s)) => JField("hasStoryPackage", JBool(s.toBoolean))
    case JField("isExpired", JString(s)) => JField("isExpired", JBool(s.toBoolean))
    case JField("syndicatable", JString(s)) => JField("syndicatable", JBool(s.toBoolean))
    case JField("subscriptionDatabases", JString(s)) => JField("subscriptionDatabases", JBool(s.toBoolean))
    case JField("developerCommunity", JString(s)) => JField("developerCommunity", JBool(s.toBoolean))
    case JField("commentable", JString(s)) => JField("commentable", JBool(s.toBoolean))
    case JField("liveBloggingNow", JString(s)) => JField("liveBloggingNow", JBool(s.toBoolean))
    case JField("isPremoderated", JString(s)) => JField("isPremoderated", JBool(s.toBoolean))
    case JField("wordcount", JString(s)) => JField("wordcount", JInt(s.toInt))
    case JField("newspaperPageNumber", JString(s)) => JField("newspaperPageNumber", JInt(s.toInt))
    case JField("starRating", JString(s)) => JField("starRating", JInt(s.toInt))
    case JField("isInappropriateForSponsorship", JString(s)) => JField("isInappropriateForSponsorship", JBool(s.toBoolean))
    case JField("isInappropriateForAdverts", JString(s)) => JField("isInappropriateForAdverts", JBool(s.toBoolean))
    case JField("internalPageCode", JString(s)) => JField("internalPageCode", JInt(s.toInt))
    case JField("internalStoryPackageCode", JString(s)) => JField("internalStoryPackageCode", JInt(s.toInt))
    case JField("width", JString(s)) => JField("width", JInt(s.toInt))
    case JField("height", JString(s)) => JField("height", JInt(s.toInt))
    case JField("duration", JString(s)) => JField("duration", JInt(s.toInt))
    case JField("isMaster", JString(s)) => JField("isMaster", JBool(s.toBoolean))
    case JField("sizeInBytes", JString(s)) => JField("sizeInBytes", JLong(s.toLong))
    case JField("blockAds", JString(s)) => JField("blockAds", JBool(s.toBoolean))
    case JField("allowUgc", JString(s)) => JField("allowUgc", JBool(s.toBoolean))
  }

  def generateJson[A <: ThriftEnum]: PartialFunction[Any, JString] = {
    case a: ThriftEnum => JString(a.name)
  }

  /** Normalise a string for use in enum lookup by removing hyphens */
  def norm(s: String): String = s.replaceAllLiterally("-", "")
}

import JsonParser._

object ContentTypeSerializer extends CustomSerializer[ContentType](format => (
  {
    case JString(s) => ContentType.valueOf(norm(s)).getOrElse(ContentType.Article)
    case JNull => null
  },
   generateJson[ContentType]
  ))

object MembershipTierSerializer extends CustomSerializer[MembershipTier](format => (
  {
    case JString(s) => MembershipTier.valueOf(norm(s)).getOrElse(MembershipTier.MembersOnly)
    case JNull => null
  },
   generateJson[MembershipTier]
  ))

object OfficeSerializer extends CustomSerializer[Office](format => (
  {
    case JString(s) => Office.valueOf(norm(s)).getOrElse(Office.Uk)
    case JNull => null
  },
   generateJson[Office]
  ))

object AssetTypeSerializer extends CustomSerializer[AssetType](format => (
  {
    case JString(s) => AssetType.valueOf(norm(s)).getOrElse(AssetType.Image)
    case JNull => null
  },
   generateJson[AssetType]
  ))

object ElementTypeSerializer extends CustomSerializer[ElementType](format => (
  {
    case JString(s) => ElementType.valueOf(norm(s)).getOrElse(ElementType.Text)
    case JNull => null
  },
   generateJson[ElementType]
  ))

object TagTypeSerializer extends CustomSerializer[TagType](format => (
  {
    case JString(s) => TagType.valueOf(norm(s)).getOrElse(TagType.Contributor)
    case JNull => null
  },
   generateJson[TagType]
  ))

object CrosswordTypeSerializer extends CustomSerializer[CrosswordType](format => (
  {
    case JString(s) => CrosswordType.valueOf(norm(s)).getOrElse(CrosswordType.Quick)
    case JNull => null
  },
   generateJson[CrosswordType]
  ))

object DateTimeSerializer extends CustomSerializer[CapiDateTime](format => (
  {
    case JString(s) => {
      CapiDateTime.apply(ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(s).getMillis)
    }
    case JNull => null
  },
  {
    // This is never used because we never generate JSON, only parse it
    case d: CapiDateTime => JString(new DateTime(d.dateTime).toString)
  }
  ))
