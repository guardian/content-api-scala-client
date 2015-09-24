package com.gu.contentapi.client.utils

import org.joda.time.DateTime
import com.gu.contentapi.client.model.v1._

object CapiModelEnrichment {

  implicit class RichCapiDateTime(val cdt: CapiDateTime) extends AnyVal {
    def toJodaDateTime: DateTime = new DateTime(cdt.dateTime)
  }

  implicit class RichJodaDateTime(val dt: DateTime) extends AnyVal {
    def toCapiDateTime: CapiDateTime = CapiDateTime.apply(dt.getMillis)
  }

}
