package com.gu.facia.client.models

import com.gu.facia.client.lib.ResourcesHelper
import org.scalatest.OptionValues
import play.api.libs.json.Json
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ConfigSpec extends AnyFlatSpec with Matchers with OptionValues with ResourcesHelper {
  "Config" should "deserialize" in {
    val config = Json
      .fromJson[ConfigJson](
        Json.parse(slurpOrDie("DEV/frontsapi/config/config.json").get)
      )
      .get

    val collection = config.collections
      .get("uk/commentisfree/most-viewed/regular-stories")
      .value

    collection.displayName.value shouldBe "Most popular"
    collection.`type`.value shouldBe "news/most-popular"
    collection.uneditable.value shouldBe true
    val backfill = collection.backfill.value
    backfill.`type` shouldBe "capi"
    backfill.query shouldBe "uk/commentisfree?show-most-viewed=true&show-editors-picks=false&hide-recent-content=true"

    val front = config.fronts.get("lifeandstyle/home-and-garden").value

    front.collections should have length 2
    front.title.value shouldBe "Home, interiors and gardening news, comment and advice"
    front.description.value shouldBe "Latest news, comment and advice on homes, interior design, " +
      "decorating and gardening from the Guardian, the world's leading liberal voice"
  }

  it should "deserialize territories" in {
    val config = Json
      .fromJson[ConfigJson](
        Json.parse(slurpOrDie("DEV/frontsapi/config/config.json").get)
      )
      .get

    config.collections
      .get("au/sport/golf/regular-stories")
      .value
      .targetedTerritory
      .value shouldBe EU27Territory
  }

  it should "deserialize unsupported territories as unknown" in {

    val configJson = """{
        |      "displayName" : "Golf",
        |      "backfill" : {
        |        "type" : "capi",
        |        "query" : "sport/golf?edition=au"
        |      },
        |      "type" : "news/special",
        |      "href" : "sport/golf",
        |      "targetedTerritory": "Made-Up-Territory"
        |    }""".stripMargin
    val parsed = Json.fromJson[CollectionConfigJson](Json.parse(configJson))
    parsed.asOpt.value.targetedTerritory.value shouldBe UnknownTerritory
  }

  it should "serialize territories" in {
    val config = Json
      .fromJson[ConfigJson](
        Json.parse(slurpOrDie("DEV/frontsapi/config/config.json").get)
      )
      .get

    val collection = config.collections
      .get("uk/commentisfree/most-viewed/regular-stories")
      .value
    val collectionWithTerritory =
      collection.copy(targetedTerritory = Some(NZTerritory))
    val json = Json.toJson(collectionWithTerritory)
    json shouldBe Json.parse(
      """{"displayName":"Most popular","backfill":{"type":"capi","query":"uk/commentisfree?show-most-viewed=true&show-editors-picks=false&hide-recent-content=true"},"type":"news/most-popular","uneditable":true,"targetedTerritory":"NZ"}"""
    )
  }
}
