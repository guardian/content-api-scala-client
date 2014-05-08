package com.gu.openplatform.contentapi.parser

import com.gu.openplatform.contentapi.model._
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
    .transformField{ fixExpired }.extract[ItemResponse]

  def parseSearch(json: String): SearchResponse = (parse(json) \ "response")
    .transformField{ fixExpired }.extract[SearchResponse]

  def parseCollection(json: String): CollectionResponse = (parse(json) \ "response")
    .transformField{ fixExpired }.extract[CollectionResponse]

  private def fixExpired: PartialFunction[JField, JField] = {
    case JField("isExpired", JString(s)) => JField("isExpired", JBool(s.toBoolean))
  }
}

object JsonParser extends JsonParser