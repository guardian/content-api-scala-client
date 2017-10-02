package com.gu.contentapi.client.utils

import com.gu.contentapi.client.model.v1._

import java.time.{ Instant, LocalDateTime, ZoneOffset }
import java.time.format.DateTimeFormatter

object CapiModelEnrichment {

  implicit class RichCapiDateTime(val cdt: CapiDateTime) extends AnyVal {
    def toLocalDateTime: LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(cdt.dateTime), ZoneOffset.UTC)
  }

  implicit class RichJodaDateTime(val dt: LocalDateTime) extends AnyVal {
    def toCapiDateTime: CapiDateTime = CapiDateTime.apply(dt.toInstant(ZoneOffset.UTC).toEpochMilli, dt.format(DateTimeFormatter.ISO_INSTANT))
  }

}
