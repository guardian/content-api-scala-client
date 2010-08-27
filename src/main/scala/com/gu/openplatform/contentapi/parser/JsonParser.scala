package com.gu.openplatform.contentapi.parser

import scala.xml._
import org.joda.time.format.ISODateTimeFormat
import com.gu.openplatform.contentapi.model.json._
import java.net.URL
import net.liftweb.json.JsonParser._
import net.liftweb.json.JsonAST.JValue


class LiftJsonParser {
  implicit val formats = net.liftweb.json.DefaultFormats + new JodaJsonSerializer

  def parseSearch(json: String) = (parse(json) \ "response").extract[SearchResponse]
  def parseTags(json: String) = (parse(json) \ "response").extract[TagsResponse]

}