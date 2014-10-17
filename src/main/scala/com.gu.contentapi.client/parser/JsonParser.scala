package com.gu.contentapi.client.parser

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import org.joda.time.DateTime
import org.json4s.DefaultFormats
import org.json4s.DateFormat
import org.json4s.JsonAST.{JField, JString, JBool}
import org.json4s.native.JsonMethods
import org.json4s.ext.DateTimeSerializer
import org.json4s.ext.JodaTimeSerializers
import scala.util.Try

import com.gu.contentapi.client.model.{ItemResponse, SearchResponse, TagsResponse, SectionsResponse, CollectionResponse}

object JsonParser {

  implicit val formats = new DefaultFormats {
    override def dateFormatter = {
      val formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
      formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
      formatter
    }

    override val dateFormat = new DateFormat {
      def parse(date: String) = Try(DateTime.parse(date).toDate).toOption
      def format(date: Date) = dateFormatter.format(date)
    }
  } ++ JodaTimeSerializers.all

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
