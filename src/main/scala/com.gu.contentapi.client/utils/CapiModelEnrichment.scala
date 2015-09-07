package com.gu.contentapi.client.model.v1

import org.joda.time.DateTime

object CapiModelEnrichment {

  implicit class RichCapiDateTime(val cdt: CapiDateTime) extends AnyVal {
    def toJodaDateTime: DateTime = new DateTime(cdt.dateTime)
  }

  implicit class RichJodaDateTime(val dt: DateTime) extends AnyVal {
    def toCapiDateTime: CapiDateTime = CapiDateTime.apply(dt.getMillis)
  }

}
