package com.gu.facia.api.utils

import org.joda.time.DateTime
import com.gu.contentapi.client.model.v1._

object CapiModelEnrichment {

  implicit class RichCapiDateTime(val cdt: CapiDateTime) extends AnyVal {
    def toJodaDateTime: DateTime = new DateTime(cdt.dateTime)
  }

}
