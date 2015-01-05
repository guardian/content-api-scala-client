package com.gu.contentapi.client.utils

import java.net.URLEncoder

import org.joda.time.ReadableInstant
import org.joda.time.format.ISODateTimeFormat

object QueryStringParams {
  def apply(parameters: Iterable[(String, String)]) = {
    def encodeParameter(p: Any): String = p match {
      case dt: ReadableInstant => URLEncoder.encode(ISODateTimeFormat.dateTimeNoMillis.print(dt), "UTF-8")
      case other => URLEncoder.encode(other.toString, "UTF-8")
    }

    parameters map {
      case (k, v) => k + "=" + encodeParameter(v)
    } mkString "&"
  }
}