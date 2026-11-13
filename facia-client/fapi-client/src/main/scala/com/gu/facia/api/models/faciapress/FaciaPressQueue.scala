package com.gu.facia.api.models.faciapress

import com.gu.facia.client.json.{JodaReads, JodaWrites}
import org.joda.time.DateTime
import play.api.libs.json._

/** Strictly speaking, these models aren't part of the facia API - they are specific to communication between
  * the fronts tool and facia-press in the frontend project. It's useful to enforce consistency between the
  * two apps though to avoid messages failing to parse, and this seems the most obvious place to put them, so
  * here they are.
  */

object PressType {
  implicit val jsonFormat: Format[PressType] = new Format[PressType] {
    override def reads(json: JsValue): JsResult[PressType] = json match {
      case JsString("live")  => JsSuccess(Live)
      case JsString("draft") => JsSuccess(Draft)
      case _                 => JsError("Content type must be either 'live' or 'draft'")
    }

    override def writes(o: PressType): JsValue = o match {
      case Live  => JsString("live")
      case Draft => JsString("draft")
    }
  }
}

sealed trait PressType

case object Live extends PressType {
  override def toString = "Live"
}

case object Draft extends PressType {
  override def toString = "Draft"
}

object FrontPath {
  implicit val jsonFormat: Format[FrontPath] = new Format[FrontPath] {
    override def writes(o: FrontPath): JsValue = JsString(o.get)

    override def reads(json: JsValue): JsResult[FrontPath] = json match {
      case JsString(path) => JsSuccess(FrontPath(path))
      case _              => JsError("Front path must be a String")
    }
  }
}

case class FrontPath(get: String) extends AnyVal

object PressJob {

  implicit val dateTimeWriter: Writes[DateTime] = JodaWrites.JodaDateTimeWrites
  implicit val dateTimeJsReader: Reads[DateTime] = JodaReads.JodaDateTimeReads
  implicit val jsonFormat: Format[PressJob] = Json.format[PressJob]
}

case class PressJob(
    path: FrontPath,
    pressType: PressType,
    creationTime: DateTime = DateTime.now,
    forceConfigUpdate: Option[Boolean] = Option(false)
)
