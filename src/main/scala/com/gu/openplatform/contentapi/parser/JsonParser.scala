package com.gu.openplatform.contentapi.parser

import com.gu.openplatform.contentapi.model._
import net.liftweb.json.JsonParser._
import net.liftweb.json.DefaultFormats


trait JsonParser {
  implicit val formats = DefaultFormats + new JodaJsonSerializer

  def parseSearch(json: String) = (parse(json) \ "response").extract[SearchResponse]
  def parseTags(json: String) = (parse(json) \ "response").extract[TagsResponse]
  def parseSections(json: String) = (parse(json) \ "response").extract[SectionsResponse]
  def parseFolders(json: String) = (parse(json) \ "response").extract[FoldersResponse]
  def parseItem(json: String) = (parse(json) \ "response").extract[ItemResponse]
}

object JsonParser extends JsonParser