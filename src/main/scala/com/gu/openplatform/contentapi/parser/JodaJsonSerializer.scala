package com.gu.openplatform.contentapi.parser

import org.joda.time.DateTime
import scala.PartialFunction
import net.liftweb.json.JsonAST._
import org.joda.time.format.ISODateTimeFormat
import net.liftweb.json.{MappingException, TypeInfo, Formats, Serializer}

// Adds Joda DateTime support to the lift-json serializer
class JodaJsonSerializer extends Serializer[DateTime] {
  private val DateTimeClass = classOf[DateTime]
  val formatter = ISODateTimeFormat.dateTimeNoMillis

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), DateTime] = {
    case (TypeInfo(DateTimeClass, _), json) => json match {
      case JString(s) => formatter.parseDateTime(s)
      case x => throw new MappingException("Can't convert " + x + " to DateTime")
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case dt: DateTime => JString(formatter.print(dt))
  }

}
