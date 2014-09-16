package com.gu.contentapi.client.parser

import org.json4s.DefaultFormats
import org.json4s.JsonAST.{JField, JString, JBool}
import org.json4s.native.JsonMethods
import com.gu.contentapi.client.model._

object JsonParser {

  implicit val formats = DefaultFormats + new JodaJsonSerializer

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

  def parseCollection(json: String): CollectionResponse = {
    (JsonMethods.parse(json) \ "response").extract[CollectionResponse]
  }

  private def fixFields: PartialFunction[JField, JField] = {
    case JField("isExpired", JString(s)) => JField("isExpired", JBool(s.toBoolean))
    case JField("webPublicationDate", JString(s)) => JField("webPublicationDateOption", JString(s))
  }

}
