package com.gu.facia.client.json

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._

import scala.util.control.Exception.allCatch

/*
  joda support has been removed from play-json in v2.6.
  Hence, we provides our own Format for joda.Datetime to ensure backward compatibility and cross play version compilation.
 */

object JodaWrites {
  implicit object JodaDateTimeWrites extends Writes[DateTime] {
    def writes(d: DateTime): JsValue = JsNumber(d.getMillis)
  }
}

object JodaReads {

  implicit val JodaDateTimeReads: Reads[DateTime] = new Reads[DateTime] {

    val dateFormat = ISODateTimeFormat.dateTimeParser().withOffsetParsed()

    def parse(s: String) =
      allCatch[DateTime] opt (DateTime.parse(s, dateFormat))

    def reads(json: JsValue): JsResult[DateTime] = json match {
      case JsNumber(d) => JsSuccess(new DateTime(d.toLong))
      case JsString(s) =>
        parse(s) match {
          case Some(d) => JsSuccess(d)
          case _       =>
            JsError(JsPath(), s"error.unexpected.date.format. Date: '$s'")
        }
      case _ => JsError(JsPath(), "error.expected.date")
    }
  }

}

object JodaFormat {
  implicit val JodaDateTimeFormat: Format[DateTime] =
    Format(JodaReads.JodaDateTimeReads, JodaWrites.JodaDateTimeWrites)
}
