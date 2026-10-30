package com.gu.facia.api.utils

import com.gu.contentapi.client.model.v1.{Content, ContentFields}
import com.gu.facia.api.models.CollectionConfig
import com.gu.facia.client.models.{CollectionConfigJson, TrailMetaData}
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{OneInstancePerTest, OptionValues}
import org.scalatestplus.mockito.MockitoSugar

class ItemKickerTest
    extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with OptionValues
    with OneInstancePerTest {
  val trailMetadata = Mockito.spy(TrailMetaData.empty)
  val content = mock[Content]
  val collectionConfig = Mockito.spy(
    CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())
  )
  val metaDataDefaults = ResolvedMetaData.Default

  "fromContentAndTrail" - {
    "should prefer item level custom kicker to collection level section kicker" in {
      when(trailMetadata.customKicker).thenReturn(Some("custom kicker"))
      when(trailMetadata.showKickerCustom).thenReturn(Some(true))
      val metaDataDefaultsWithShowKickerCustom =
        metaDataDefaults.copy(showKickerCustom = true)
      when(collectionConfig.showSections).thenReturn(true)
      when(content.tags).thenReturn(Nil)

      ItemKicker
        .fromContentAndTrail(
          Option(content),
          trailMetadata,
          metaDataDefaultsWithShowKickerCustom,
          Some(collectionConfig)
        )
        .value shouldBe a[FreeHtmlKicker]
    }

    "should prefer item level section kicker to collection level tag kicker" in {
      when(collectionConfig.showTags).thenReturn(true)
      when(trailMetadata.showKickerSection).thenReturn(Some(true))
      val metaDataDefaultsWithShowKickerSection =
        metaDataDefaults.copy(showKickerSection = true)
      when(content.sectionId).thenReturn(Some("section"))
      when(content.sectionName).thenReturn(Some("Section"))
      when(content.tags).thenReturn(Nil)

      ItemKicker
        .fromContentAndTrail(
          Option(content),
          trailMetadata,
          metaDataDefaultsWithShowKickerSection,
          Some(collectionConfig)
        )
        .value shouldBe a[SectionKicker]
    }

    "should show breaking news kicker even when hide kickers is set to true" in {
      when(collectionConfig.showTags).thenReturn(true)
      when(trailMetadata.showKickerCustom).thenReturn(None)
      when(trailMetadata.isBreaking).thenReturn(Some(true))

      ItemKicker.fromContentAndTrail(
        Option(content),
        trailMetadata,
        metaDataDefaults,
        Some(collectionConfig)
      ) shouldBe Some(BreakingNewsKicker)
    }
  }

  "kickerContents" - {
    "should return the contents of podcast kickers" in {
      ItemKicker.kickerContents(
        PodcastKicker(Some(Series("Name Goes Here", "")))
      ) shouldBe Some("Name Goes Here")
    }

    "should return the contents of tag kickers" in {
      ItemKicker.kickerContents(
        TagKicker(
          "Aberdeen-Grampian",
          "",
          "aberdeen-grampian/aberdeen-grampian"
        )
      ) shouldBe Some("Aberdeen-Grampian")
    }

    "should return the contents of free HTML kickers (without links)" in {
      ItemKicker.kickerContents(
        FreeHtmlKicker("<b>Something</b>")
      ) shouldBe Some("<b>Something</b>")
    }

    "should return the contents of free HTML kickers (with links)" in {
      ItemKicker.kickerContents(
        FreeHtmlKickerWithLink(
          "<b>Something</b>",
          "http://www.theguardian.com/football"
        )
      ) shouldBe Some("<b>Something</b>")
    }
  }

  "kickerText" - {
    "should return a textual description for Breaking News kickers" in {
      ItemKicker.kickerText(BreakingNewsKicker) shouldBe Some("Breaking news")
    }

    "should return a textual description for Analysis kickers" in {
      ItemKicker.kickerText(AnalysisKicker) shouldBe Some("Analysis")
    }

    "should return a textual description for Review kickers" in {
      ItemKicker.kickerText(ReviewKicker) shouldBe Some("Review")
    }

    "should return a textual description for Cartoon kickers" in {
      ItemKicker.kickerText(CartoonKicker) shouldBe Some("Cartoon")
    }

    "should return a textual description for tag kickers" in {
      ItemKicker.kickerText(
        TagKicker(
          "Aberdeen-Grampian",
          "",
          "aberdeen-grampian/aberdeen-grampian"
        )
      ) shouldBe Some("Aberdeen-Grampian")
    }

    "should return a textual description for section kickers" in {
      ItemKicker.kickerText(SectionKicker("Football", "")) shouldBe Some(
        "Football"
      )
    }

    "should return nothing for free HTML kickers containing HTML" in {
      ItemKicker.kickerText(FreeHtmlKicker("<b>Something</b>")) shouldBe None
      ItemKicker.kickerText(
        FreeHtmlKicker("Look at <a href=\"http://example.com\">this</a> link")
      ) shouldBe None
      ItemKicker.kickerText(
        FreeHtmlKickerWithLink(
          "<b>Something</b>",
          "http://www.theguardian.com/football"
        )
      ) shouldBe None
      ItemKicker.kickerText(
        FreeHtmlKickerWithLink(
          "<a href=\"foo\">Something</b>",
          "http://www.theguardian.com/football"
        )
      ) shouldBe None
    }

    "should return the text of free HTML kickers not actually containing HTML" in {
      ItemKicker.kickerText(FreeHtmlKicker("Something")) shouldBe Some(
        "Something"
      )
      ItemKicker.kickerText(
        FreeHtmlKickerWithLink(
          "Something",
          "http://www.theguardian.com/football"
        )
      ) shouldBe Some("Something")
    }
  }

  "section kicker" - {
    "should not appear for supporting" in {
      when(trailMetadata.showKickerSection).thenReturn(Some(false))
      val metaDataDefaultsWithShowKickerSection =
        metaDataDefaults.copy(showKickerSection = false)
      when(content.sectionId).thenReturn(Some("section"))
      when(content.sectionName).thenReturn(Some("Section"))
      when(content.tags).thenReturn(Nil)
      when(content.fields).thenReturn(Some(ContentFields()))

      ItemKicker.fromContentAndTrail(
        Option(content),
        trailMetadata,
        metaDataDefaultsWithShowKickerSection,
        None
      ) should be(None)
    }

  }
}
