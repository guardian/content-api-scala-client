package com.gu.facia.client.models

import com.gu.facia.client.lib.ResourcesHelper
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsNumber, Json}

class JsonCompatibilitySpec extends AnyFlatSpec with Matchers with OptionValues with ResourcesHelper {
  private def collection: CollectionJson =
    Json
      .parse(
        slurpOrDie(
          "PROD/frontsapi/collection/e394b4df-85b2-4e94-aa9d-7ecf3baddee4/collection.json"
        ).get
      )
      .as[CollectionJson]

  "Play JSON" should "preserve large integers, timestamps, decimals and exponent values" in {
    val cases = Seq(
      "9007199254740993",
      "9223372036854775807",
      "1700000000123",
      "0.123456789012345678901234567890",
      "1.234567890123456789e+40",
      "1.234567890123456789e-40"
    )

    cases.foreach { input =>
      val parsed = Json.parse(input)
      parsed shouldBe JsNumber(BigDecimal(input))
      Json.parse(Json.stringify(parsed)) shouldBe parsed
    }
  }

  it should "preserve Unicode and escaped text in collection headlines and slideshow captions" in {
    val text = "Headline: \"quoted\" \\ path\n\t\r\u0000 caf\u00e9 \u4e16\u754c \uD83D\uDE00"
    val slideshow = List(SlideshowAsset("https://example.com/image.jpg", "940", "720", Some(text)))
    val original = collection
    val trail = original.live.head.copy(
      meta = Some(
        TrailMetaData.withDefaults(
          "headline" -> Json.toJson(text),
          "slideshow" -> Json.toJson(slideshow)
        )
      )
    )
    val expected = original.copy(live = List(trail))

    val result = Json.parse(Json.stringify(Json.toJson(expected))).as[CollectionJson]

    result.lastUpdated.getMillis shouldBe expected.lastUpdated.getMillis
    // Epoch-millisecond serialization preserves the instant, not the original time zone.
    result.copy(lastUpdated = expected.lastUpdated) shouldBe expected
    result.live.head.safeMeta.headline.value shouldBe text
    result.live.head.safeMeta.slideshow.value.head.caption.value shouldBe text
  }

  it should "round-trip a large collection with long text, supporting items and slideshows" in {
    val original = collection
    val text = "Long collection trail text. " * 200
    val supporting = Json.arr(
      Json.obj(
        "id" -> "news/supporting",
        "frontPublicationDate" -> 1700000000123L,
        "meta" -> Json.obj("headline" -> "Supporting headline")
      )
    )
    val slideshow = List(SlideshowAsset("https://example.com/image.jpg", "940", "720", Some(text)))
    val trails = (1 to 250).map { index =>
      original.live.head.copy(
        id = s"news/article-$index",
        meta = Some(
          TrailMetaData.withDefaults(
            "headline" -> Json.toJson(s"Headline $index"),
            "trailText" -> Json.toJson(text),
            "supporting" -> supporting,
            "slideshow" -> Json.toJson(slideshow)
          )
        )
      )
    }.toList
    val expected = original.copy(live = trails)
    val serialized = Json.stringify(Json.toJson(expected))

    serialized.length should be > 1000000
    val result = Json.parse(serialized).as[CollectionJson]

    result.lastUpdated.getMillis shouldBe expected.lastUpdated.getMillis
    result.copy(lastUpdated = expected.lastUpdated) shouldBe expected
    result.live should have size 250
    result.live.last.safeMeta.trailText.value shouldBe text
    result.live.last.safeMeta.supporting.value.head.id shouldBe "news/supporting"
    result.live.last.safeMeta.slideshow.value.head.caption.value shouldBe text
  }
}
