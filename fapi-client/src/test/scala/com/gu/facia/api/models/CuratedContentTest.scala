package com.gu.facia.api.models

import com.gu.contentapi.client.model.v1.{ContentFields, Tag, TagType}
import com.gu.facia.api.utils.{ResolvedMetaData, SectionKicker, TagKicker}
import com.gu.facia.client.models.{CollectionConfigJson, TrailMetaData}
import lib.TestContent
import org.scalatest.OptionValues._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsBoolean, JsString}

class CuratedContentTest extends AnyFreeSpec with Matchers with TestContent {

  val collectionConfig =
    CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())

  "CuratedContent headline" - {

    val contentWithFieldHeadline = baseContent.copy(fields =
      Some(
        ContentFields(
          headline = Some("Content headline"),
          trailText = Some("Content trailtext"),
          byline = Some("Content byline")
        )
      )
    )
    val contentWithoutFieldHeadline = baseContent.copy(
      webTitle = "contentWithoutFieldHeadlineHeadline",
      fields = Some(
        ContentFields(
          trailText = Some("Content trailtext"),
          byline = Some("Content byline")
        )
      )
    )

    val trailMetaDataWithHeadline =
      TrailMetaData(Map("headline" -> JsString("trailMetaDataHeadline")))
    val trailMetaDataWithoutHeadline = TrailMetaData(Map.empty)

    "should resolve the headline from TrailMetaData" in {
      val curatedContent = CuratedContent.fromTrailAndContent(
        contentWithFieldHeadline,
        trailMetaDataWithHeadline,
        None,
        collectionConfig,
        None
      )
      curatedContent.headline should be("trailMetaDataHeadline")
    }

    "should resolve the headline from Content fields.headline" in {
      val curatedContent = CuratedContent.fromTrailAndContent(
        contentWithFieldHeadline,
        trailMetaDataWithoutHeadline,
        None,
        collectionConfig,
        None
      )
      curatedContent.headline should be("Content headline")
    }
    "should resolve the headline from Content webTitle" in {
      val curatedContent = CuratedContent.fromTrailAndContent(
        contentWithoutFieldHeadline,
        trailMetaDataWithoutHeadline,
        None,
        collectionConfig,
        None
      )
      curatedContent.headline should be("contentWithoutFieldHeadlineHeadline")
    }
  }

  "CuratedContent multimedia slideshow" - {
    val content = baseContent

    "should carry the multimediaSlideshowAtomId from TrailMetaData and leave the atom unresolved" in {
      val trailMetaData = TrailMetaData(
        Map(
          "multimediaSlideshowReplace" -> JsBoolean(true),
          "multimediaSlideshowAtomId" -> JsString(
            "atom/multimediaslideshow/123"
          )
        )
      )
      val curatedContent = CuratedContent.fromTrailAndContent(
        content,
        trailMetaData,
        None,
        collectionConfig,
        None
      )
      curatedContent.properties.multimediaSlideshowReplace should be(true)
      curatedContent.multimediaSlideshowAtomId should be(
        Some("atom/multimediaslideshow/123")
      )
      curatedContent.multimediaSlideshow should be(None)
    }

    "should default to no multimedia slideshow when not configured" in {
      val curatedContent = CuratedContent.fromTrailAndContent(
        content,
        TrailMetaData(Map.empty),
        None,
        collectionConfig,
        None
      )
      curatedContent.properties.multimediaSlideshowReplace should be(false)
      curatedContent.multimediaSlideshowAtomId should be(None)
      curatedContent.multimediaSlideshow should be(None)
    }
  }

  "CuratedContent itemKicker" - {
    val emptyContent = baseContent.copy(
      fields = Some(
        ContentFields(
          headline = Some("Content headline"),
          trailText = Some("Content trailtext"),
          byline = Some("Content byline")
        )
      ),
      tags = List(Tag("id", TagType.Keyword, None, None, "", "", ""))
    )

    val emptyTrailMetaData = TrailMetaData(Map.empty)
    val collectionConfig =
      CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())
    val collectionConfigShowSections = CollectionConfig.fromCollectionJson(
      CollectionConfigJson.withDefaults(showSections = Option(true))
    )
    val collectionConfigShowTags = CollectionConfig.fromCollectionJson(
      CollectionConfigJson.withDefaults(showTags = Option(true))
    )

    "should resolve to None" in {
      val curatedContent = CuratedContent.fromTrailAndContent(
        emptyContent,
        emptyTrailMetaData,
        None,
        collectionConfig,
        None
      )
      curatedContent.kicker should be(None)
    }

    "should resolve to SectionKicker with config showSections true" in {
      val curatedContent = CuratedContent.fromTrailAndContent(
        emptyContent,
        emptyTrailMetaData,
        None,
        collectionConfigShowSections,
        None
      )
      curatedContent.kicker.value shouldBe a[SectionKicker]
    }

    "should resolve to TagKicker with config showTags true" in {
      //This test requires content to have tags
      val curatedContent = CuratedContent.fromTrailAndContent(
        emptyContent,
        emptyTrailMetaData,
        None,
        collectionConfigShowTags,
        None
      )
      curatedContent.kicker.value shouldBe a[TagKicker]
    }

    "should resolve to SectionKicker for trailMetaData showKickerSection true" in {
      val trailMetaDataShowKickerSection =
        TrailMetaData(Map("showKickerSection" -> JsBoolean(value = true)))
      val curatedContent = CuratedContent.fromTrailAndContent(
        emptyContent,
        trailMetaDataShowKickerSection,
        None,
        collectionConfigShowTags,
        None
      )
      curatedContent.kicker.value shouldBe a[SectionKicker]
    }

    "should resolve to SectionKicker for trailMetaData showKickerTag true" in {
      val trailMetaDataShowKickerTag =
        TrailMetaData(Map("showKickerTag" -> JsBoolean(value = true)))
      val curatedContent = CuratedContent.fromTrailAndContent(
        emptyContent,
        trailMetaDataShowKickerTag,
        None,
        collectionConfigShowTags,
        None
      )
      curatedContent.kicker.value shouldBe a[TagKicker]
    }
  }

  "SupportingCuratedContent itemKicker" - {
    val emptyContent = baseContent.copy(
      fields = Some(
        ContentFields(
          headline = Some("Content headline"),
          trailText = Some("Content trailtext"),
          byline = Some("Content byline"),
          internalPageCode = Some(123)
        )
      ),
      tags = Seq(Tag("id", TagType.Keyword, None, None, "", "", ""))
    )

    val emptyTrailMetaData = TrailMetaData(Map.empty)
    val collectionConfig =
      CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())
    val collectionConfigShowSections = CollectionConfig.fromCollectionJson(
      CollectionConfigJson.withDefaults(showSections = Option(true))
    )
    val collectionConfigShowTags = CollectionConfig.fromCollectionJson(
      CollectionConfigJson.withDefaults(showTags = Option(true))
    )

    "should resolve to None" in {
      val supportingCuratedContent =
        SupportingCuratedContent.fromTrailAndContent(
          emptyContent,
          emptyTrailMetaData,
          None,
          collectionConfig,
          None
        )
      supportingCuratedContent.kicker should be(None)
    }

    "should resolve to None with config showSections true" in {
      val supportingCuratedContent =
        SupportingCuratedContent.fromTrailAndContent(
          emptyContent,
          emptyTrailMetaData,
          None,
          collectionConfigShowSections,
          None
        )
      supportingCuratedContent.kicker should be(None)
    }

    "should resolve to None with config showTags true" in {
      val supportingCuratedContent =
        SupportingCuratedContent.fromTrailAndContent(
          emptyContent,
          emptyTrailMetaData,
          None,
          collectionConfigShowTags,
          None
        )
      supportingCuratedContent.kicker should be(None)
    }

    "should resolve to SectionKicker for trailMetaData showKickerSection true" in {
      val trailMetaDataShowKickerSection =
        TrailMetaData(Map("showKickerSection" -> JsBoolean(value = true)))
      val supportingCuratedContent =
        SupportingCuratedContent.fromTrailAndContent(
          emptyContent,
          trailMetaDataShowKickerSection,
          None,
          collectionConfigShowTags,
          None
        )
      supportingCuratedContent.kicker.value shouldBe a[SectionKicker]
    }

    "should resolve to SectionKicker for trailMetaData showKickerTag true" in {
      val trailMetaDataShowKickerTag =
        TrailMetaData(Map("showKickerTag" -> JsBoolean(value = true)))
      val supportingCuratedContent =
        SupportingCuratedContent.fromTrailAndContent(
          emptyContent,
          trailMetaDataShowKickerTag,
          None,
          collectionConfigShowTags,
          None
        )
      supportingCuratedContent.kicker.value shouldBe a[TagKicker]
    }
  }

  "CuratedContent images" - {
    val replaceSrc = "https://somewhere-on-the-internet/replace-image.jpg"
    val replaceDimensions = "100"
    val cutoutSrc = "https://somewhere-on-the-internet/cutout-image.jpg"
    val cutoutDimensions = "200"
    val contentWithCommentTone = baseContent.copy(
      fields = Some(
        ContentFields(
          headline = Some("Content headline"),
          trailText = Some("Content trailtext"),
          byline = Some("Content byline")
        )
      ),
      tags = List(
        Tag(ResolvedMetaData.Comment, TagType.Tone, None, None, "", "", "")
      )
    )

    "should default the cutout image for content with the Comment tone" in {
      val trailMetadata = TrailMetaData(
        Map(
          "imageCutoutSrc" -> JsString(cutoutSrc),
          "imageCutoutSrcWidth" -> JsString(cutoutDimensions),
          "imageCutoutSrcHeight" -> JsString(cutoutDimensions)
        )
      )
      val curatedContent = CuratedContent.fromTrailAndContent(
        contentWithCommentTone,
        trailMetadata,
        None,
        collectionConfig,
        None
      )

      val expectedImage = Some(
        Cutout(
          cutoutSrc,
          Some(cutoutDimensions),
          Some(cutoutDimensions)
        )
      )

      curatedContent.image shouldBe expectedImage
    }

    "should default to the replacement image, not the cutout image, when `imageReplace` is true for content with the Comment tone" in {
      val trailMetadata = TrailMetaData(
        Map(
          "imageReplace" -> JsBoolean(true),
          "imageSrc" -> JsString(replaceSrc),
          "imageSrcWidth" -> JsString(replaceDimensions),
          "imageSrcHeight" -> JsString(replaceDimensions),
          "imageCutoutSrc" -> JsString(cutoutSrc),
          "imageCutoutSrcWidth" -> JsString(cutoutDimensions),
          "imageCutoutSrcHeight" -> JsString(cutoutDimensions)
        )
      )
      val curatedContent = CuratedContent.fromTrailAndContent(
        contentWithCommentTone,
        trailMetadata,
        None,
        collectionConfig,
        None
      )

      val expectedImage = Some(
        Replace(
          replaceSrc,
          replaceDimensions,
          replaceDimensions,
          None
        )
      )

      curatedContent.image shouldBe expectedImage
    }
  }
}
