package com.gu.openplatform.contentapi

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

package object parameters {
  trait RenderParameters[A] {
    def render(a: A): Map[String, String]
  }

  def dateString(dateTime: DateTime): String = ISODateTimeFormat.dateTimeNoMillis.print(dateTime)

  def flatten[A, B](map: Map[A, Option[B]]) = (map.toSeq collect { case (k, Some(v)) => k -> v }).toMap

  def renderParams[A: RenderParameters](a: A) = implicitly[RenderParameters[A]].render(a)
}
