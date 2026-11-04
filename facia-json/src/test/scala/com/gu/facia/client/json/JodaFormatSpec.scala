package com.gu.facia.client.json

import com.gu.facia.client.models.jodaDateTimeFormats
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsNumber, JsSuccess, Json, Reads}

class JodaFormatSpec extends AnyFlatSpec with Matchers {
  private val readers: Seq[Reads[DateTime]] =
    Seq(JodaReads.JodaDateTimeReads, jodaDateTimeFormats)

  "Date JSON formats" should "read millisecond timestamps without losing precision" in {
    val millis = 1700000000123L
    val json = Json.parse(millis.toString)

    readers.foreach { reader =>
      json.validate[DateTime](reader).map(_.getMillis) shouldBe JsSuccess(millis)
    }
  }

  it should "preserve the instant and offset of ISO date strings" in {
    val json = Json.parse("\"2026-11-04T14:39:14.123+05:30\"")
    val expected = new DateTime(2026, 11, 4, 14, 39, 14, 123, DateTimeZone.forOffsetHoursMinutes(5, 30))

    readers.foreach { reader =>
      val result = json.validate[DateTime](reader)
      result.map(_.getMillis) shouldBe JsSuccess(expected.getMillis)
      result.map(_.getZone.getOffset(expected.getMillis)) shouldBe JsSuccess(19800000)
    }
  }

  it should "return validation errors for invalid dates and unsupported JSON types" in {
    Seq("\"not-a-date\"", "\"2026-02-30T12:00:00Z\"", "null", "true", "{}", "[]").foreach { input =>
      readers.foreach { reader =>
        Json.parse(input).validate[DateTime](reader).isError shouldBe true
      }
    }
  }

  it should "write epoch milliseconds and round-trip them through Play JSON" in {
    val date = new DateTime(1700000000123L, DateTimeZone.UTC)
    val json = Json.toJson(date)(JodaFormat.JodaDateTimeFormat)

    json shouldBe JsNumber(BigDecimal(date.getMillis))
    Json
      .parse(Json.stringify(json))
      .validate[DateTime](JodaFormat.JodaDateTimeFormat)
      .map(_.getMillis) shouldBe JsSuccess(date.getMillis)
  }
}
