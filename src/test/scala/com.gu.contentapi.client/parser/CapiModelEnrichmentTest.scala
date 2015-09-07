package com.gu.contentapi.client.parser

import com.gu.contentapi.client.model.v1.{CapiDateTime}
import org.joda.time.DateTime
import org.scalatest.{Matchers, FlatSpec}

import com.gu.contentapi.client.model.v1.CapiModelEnrichment._

class CapiModelEnrichmentTest extends FlatSpec with Matchers {

  behavior of "CapiModelEnrichment"

    val dateTimeString = "2015-06-07T09:00:02Z"
    val capiDateTime: CapiDateTime = CapiDateTime(new DateTime(dateTimeString).getMillis)
    val jodaDateTime = new DateTime(dateTimeString)

    "toJodaDateTime" should "give a DateTime object from a CapiDateTime object" in {
       capiDateTime.toJodaDateTime should be(jodaDateTime)
    }

    "toCapiDateTime" should "give a CapiDateTime object from a DateTime object" in {
      jodaDateTime.toCapiDateTime should be(capiDateTime)
    }

}
