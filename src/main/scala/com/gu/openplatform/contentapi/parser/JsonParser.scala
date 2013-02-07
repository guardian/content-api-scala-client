package com.gu.openplatform.contentapi.parser

import com.gu.openplatform.contentapi.model._
import net.liftweb.json.JsonParser._
import net.liftweb.json.DefaultFormats
import net.liftweb.json.JsonAST.{JValue, JBool, JString, JField}


trait JsonParser {
  implicit val formats = DefaultFormats + new JodaJsonSerializer

  def parseTags(json: String): TagsResponse = (parse(json) \ "response").extract[TagsResponse]
  def parseSections(json: String): SectionsResponse = (parse(json) \ "response").extract[SectionsResponse]
  def parseFolders(json: String): FoldersResponse = (parse(json) \ "response").extract[FoldersResponse]

  def parseItem(json: String):ItemResponse = (parse(json) \ "response")
    .transform{ fixExpired }.extract[ItemResponse]

  def parseSearch(json: String): SearchResponse = (parse(json) \ "response")
    .transform{ fixExpired }.extract[SearchResponse]

  private def fixExpired: PartialFunction[JValue, JValue] = {
    case JField("isExpired", JString(s)) => JField("isExpired", JBool(s.toBoolean))
  }
}

object JsonParser extends JsonParser