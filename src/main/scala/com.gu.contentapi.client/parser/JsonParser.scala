package com.gu.contentapi.client.parser

import com.gu.contentapi.client.model._
import org.json4s.DefaultFormats
import org.json4s.JsonAST._
import org.json4s.native.JsonMethods.parse


trait JsonParser {
  implicit val formats = DefaultFormats + new JodaJsonSerializer

  def parseTags(json: String): TagsResponse = (parse(json) \ "response").extract[TagsResponse]
  def parseSections(json: String): SectionsResponse = (parse(json) \ "response").extract[SectionsResponse]
  def parseFronts(json: String): FrontsResponse = (parse(json) \ "response").extract[FrontsResponse]
  def parseFolders(json: String): FoldersResponse = (parse(json) \ "response").extract[FoldersResponse]

  def parseItem(json: String): ItemResponse = (parse(json) \ "response")
    .transformField{ fixFields }.extract[ItemResponse]

  def parseSearch(json: String): SearchResponse = (parse(json) \ "response")
    .transformField{ fixFields }.extract[SearchResponse]

  def parseCollection(json: String): CollectionResponse = (parse(json) \ "response")
    .transformField{ fixFields }.extract[CollectionResponse]

  private def fixFields: PartialFunction[JField, JField] = {
    case JField("isExpired", JString(s)) => JField("isExpired", JBool(s.toBoolean))
    case JField("webPublicationDate", JString(s)) => JField("webPublicationDateOption", JString(s))
  }
}

object JsonParser extends JsonParser