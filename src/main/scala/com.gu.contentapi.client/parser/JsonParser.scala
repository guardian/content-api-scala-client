package com.gu.contentapi.client.parser

import com.gu.contentapi.client.model.v1.{MembershipTier, CapiDateTime, ContentType}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import org.json4s.{CustomSerializer, DefaultFormats}
import org.json4s.JsonAST._
import org.json4s.native.JsonMethods
import com.gu.contentapi.client.model._

object JsonParser {

  implicit val formats = DefaultFormats + ContentTypeSerializer + DateTimeSerializer + MembershipTierSerializer

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
    case JField("internalPageCode", JString(s)) => JField("internalPageCode", JInt(s.toInt))
    case JField("internalStoryPackageCode", JString(s)) => JField("internalStoryPackageCode", JInt(s.toInt))
    case JField("width", JString(s)) => JField("width", JInt(s.toInt))
    case JField("height", JString(s)) => JField("height", JInt(s.toInt))
  }
}

object ContentTypeSerializer extends CustomSerializer[ContentType](format => (
  {
    /* Defaults to article */
    case JString(s) => ContentType.valueOf(s).getOrElse(ContentType.Article)
    case JNull => null
  },
  {
    // This is never used because we never generate JSON, only parse it
    case ct: ContentType => JString(ct.name)
  }
  ))

object MembershipTierSerializer extends CustomSerializer[MembershipTier](format => (
  {
    /* Defaults to MembersOnly */
    case JString(s) => MembershipTier.valueOf(s).getOrElse(MembershipTier.MembersOnly)
    case JNull => null
  },
  {
    // This is never used because we never generate JSON, only parse it
    case mt: MembershipTier => JString(mt.name)
  }
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

