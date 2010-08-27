package com.gu.openplatform.contentapi.parser

import com.gu.openplatform.contentapi.model.json._
import net.liftweb.json.JsonParser._


class LiftJsonParser {
  implicit val formats = net.liftweb.json.DefaultFormats + new JodaJsonSerializer

  def parseSearch(json: String) = (parse(json) \ "response").extract[SearchResponse]
  def parseTags(json: String) = (parse(json) \ "response").extract[TagsResponse]
  def parseSections(json: String) = (parse(json) \ "response").extract[SectionsResponse]
  def parseItem(json: String) = (parse(json) \ "response").extract[ItemResponse]

}

object LiftJsonParser extends LiftJsonParser