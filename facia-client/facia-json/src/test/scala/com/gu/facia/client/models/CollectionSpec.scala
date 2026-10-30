package com.gu.facia.client.models

import com.gu.facia.client.lib.ResourcesHelper
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class CollectionSpec extends AnyFlatSpec with Matchers with OptionValues with ResourcesHelper {
  def getCollectionFixture(path: String) = Json.fromJson[CollectionJson](
    Json.parse(slurpOrDie(path).get)
  ) match {
    case JsSuccess(a, _) => a
    case e @ JsError(_)  =>
      println(e)
      throw new RuntimeException("ARGH")
  }

  "Collection" should "deserialize" in {
    val collection = getCollectionFixture(
      "DEV/frontsapi/collection/2409-31b3-83df0-de5a/collection.json"
    )

    collection.live should have size 8

    val front = collection.live
      .find(
        _.id == "football/quiz/2014/jun/11/world-cup-2014-the-ultimate-world-cup-trivia-quiz"
      )
      .value

    front.frontPublicationDate shouldBe 1402500092818L
    front.meta.value shouldBe TrailMetaData.withDefaults(
      ("headline", JsString("The ultimate World Cup trivia quiz")),
      ("group", JsString("0"))
    )

    collection.lastUpdated shouldBe new DateTime(
      2014,
      6,
      12,
      8,
      30,
      20,
      67,
      DateTimeZone.UTC
    )

    collection.updatedBy shouldBe "Katherine Le Ruez"

    collection.updatedEmail shouldBe "katherine.leruez@guardian.co.uk"
  }

  it should "deserialize content with image src widths and heights attached" in {
    val collection = getCollectionFixture(
      "PROD/frontsapi/collection/e394b4df-85b2-4e94-aa9d-7ecf3baddee4/collection2.json"
    )

    val itemMeta = collection.live.lift(1).value.safeMeta
    itemMeta.imageSrcWidth.value shouldBe "940"
    itemMeta.imageSrcHeight.value shouldBe "564"
  }

  it should "deserialize content with slideshows, where captions are optional" in {
    val collection = getCollectionFixture(
      "PROD/frontsapi/collection/e394b4df-85b2-4e94-aa9d-7ecf3baddee4/collection-with-captions.json"
    )

    val item = collection.live.lift(0).value
    item.safeMeta.slideshow shouldBe Some(
      List(
        SlideshowAsset(
          "http://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2014/8/4/1407146294410/PrinceWilliamCatherineDuche.jpg",
          "940",
          "720"
        ),
        SlideshowAsset(
          "http://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2014/8/4/1407140556976/AustraliansingerKylieMinogu.jpg",
          "940",
          "720",
          Some("Kylie Minogue")
        )
      )
    )
  }

  it should "deserialize content without metadata" in {
    val collection = getCollectionFixture(
      "PROD/frontsapi/collection/754c-8e8c-fad9-a927/collection.json"
    )

    collection.live.lift(2).value.meta shouldBe None
  }

  it should "deserialize content with supporting items" in {
    val collection = getCollectionFixture(
      "PROD/frontsapi/collection/e394b4df-85b2-4e94-aa9d-7ecf3baddee4/collection.json"
    )

    val supportingContent =
      collection.live.headOption.value.safeMeta.supporting.value
    supportingContent should have size 3

    val item = supportingContent.lift(1).value
    item.id shouldBe "internal-code/content/442568601"
    item.publishedBy shouldBe Some("user1")

    val meta = item.meta.value
    meta.group shouldBe Some("2")

    meta.headline shouldBe Some(
      "Special report: Scores killed in deadliest assault in Gaza so far"
    )
  }

  it should "be able to serialize and deserialize the same object" in {
    val collectionJson = CollectionJson(
      live = List(Trail("id-123", DateTime.now.getMillis, None, None, None)),
      draft = None,
      treats = None,
      lastUpdated = DateTime.now(),
      updatedBy = "A test",
      updatedEmail = "a test email",
      displayName = None,
      href = None,
      previously = None,
      targetedTerritory = None
    )

    val collectionJsonAsString = Json.stringify(Json.toJson(collectionJson))
    val newCollectionJson =
      Json.parse(collectionJsonAsString).as[CollectionJson]

    newCollectionJson.lastUpdated shouldBe collectionJson.lastUpdated
    newCollectionJson shouldBe collectionJson
  }
}
