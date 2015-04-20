package com.gu.contentapi.client.parser

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.json4s.{CustomSerializer, DefaultFormats}
import org.json4s.JsonAST.{JNull, JField, JString, JBool}
import org.json4s.native.JsonMethods
import com.gu.contentapi.client.model.{ItemResponse, SearchResponse, TagsResponse, SectionsResponse}

object JsonParser {

  implicit val formats = DefaultFormats + BetterDateTimeSerializer

  def parseItem(json: String): ItemResponse = {
    (JsonMethods.parse(json) \ "response").transformField(fixFields).extract[ItemResponse]
  }

  def parseSearch(json: String): SearchResponse = {
    (JsonMethods.parse(json) \ "response").transformField(fixFields).extract[SearchResponse]
  }

  def parseTags(json: String): TagsResponse = {
    (JsonMethods.parse(json) \ "response").extract[TagsResponse]
  }

  def parseSections(json: String): SectionsResponse = {
    (JsonMethods.parse(json) \ "response").extract[SectionsResponse]
  }

  private def fixFields: PartialFunction[JField, JField] = {
    case JField("isExpired", JString(s)) => JField("isExpired", JBool(s.toBoolean))
    case JField("webPublicationDate", JString(s)) => JField("webPublicationDateOption", JString(s))
    case JField("syndicatable", JString(s)) => JField("syndicatable", JBool(s.toBoolean))
    case JField("subscriptionDatabases", JString(s)) => JField("subscriptionDatabases", JBool(s.toBoolean))
    case JField("developerCommunity", JString(s)) => JField("developerCommunity", JBool(s.toBoolean))
  }

}

/** A Joda DateTime serializer that handles more flexible date formats, e.g. optional milliseconds */
object BetterDateTimeSerializer extends CustomSerializer[DateTime](format => (
  {
    case JString(s) => ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(s)
    case JNull => null
  },
  {
    // This is never used because we never generate JSON, only parse it
    case d: DateTime => JString(format.dateFormat.format(d.toDate))
  }
))

