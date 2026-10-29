package com.gu.facia.client

import play.api.libs.json._
import org.joda.time.DateTime
import scala.util.{Failure, Success, Try}
import play.api.libs.json.JsString

package object models {

  /** However Facia Tool is serializing its date time, it's not doing so in a way that the default Play
    * Formats for Joda DateTime can understand.
    *
    * This makes me extremely sad.
    */
  implicit val jodaDateTimeFormats: Reads[DateTime] = new Reads[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] = json match {
      case JsString(dateTimeString) =>
        Try { DateTime.parse(dateTimeString) } match {
          case Success(dateTime) => JsSuccess(dateTime)
          case Failure(error)    => JsError(error.getMessage)
        }
      case JsNumber(dateTimeLong) =>
        Try { new DateTime(dateTimeLong.toLong) } match {
          case Success(dateTime) => JsSuccess(dateTime)
          case Failure(error)    => JsError(error.getMessage)
        }
      case _ => JsError(s"$json is not a date time string")
    }
  }
}
