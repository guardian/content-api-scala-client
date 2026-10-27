package com.gu.facia.api.utils

import com.gu.contentapi.client.model.v1._
import com.gu.facia.client.models.TrailMetaData
import lib.TestContent
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.JsBoolean

class ResolvedMetaDataTest extends AnyFreeSpec with Matchers with TestContent {

  def tagWithId(id: String) = Tag(
    id = id,
    `type` = TagType.Keyword,
    webTitle = "",
    webUrl = "",
    apiUrl = ""
  )

  def contentWithTags(tags: Tag*): Content =
    baseContent.copy(tags = tags.toList)

  val contentWithCartoon = contentWithTags(tagWithId("type/cartoon"))
  val contentWithComment = contentWithTags(tagWithId("tone/comment"))
  val contentWithVideo = contentWithTags(tagWithId("type/video"))
  val atomBlock = Some(
    Block(
      id = "foo",
      bodyHtml = "foo",
      bodyTextSummary = "foo",
      attributes = BlockAttributes(),
      published = true,
      elements = Seq(BlockElement(`type` = ElementType.Contentatom))
    )
  )

  val contentWithAtom: Content =
    contentWithVideo.copy(blocks = Some(Blocks(atomBlock)))

  val emptyTrailMetaData = TrailMetaData(Map.empty)
  val trailMetaDataWithFieldsSetTrue = TrailMetaData(
    Map(
      "showByline" -> JsBoolean(true),
      "showMainVideo" -> JsBoolean(true),
      "showQuotedHeadline" -> JsBoolean(true),
      "imageCutoutReplace" -> JsBoolean(true),
      "multimediaSlideshowReplace" -> JsBoolean(true)
    )
  )
  val trailMetaDataWithFieldsSetFalse = TrailMetaData(
    Map(
      "showByline" -> JsBoolean(false),
      "showMainVideo" -> JsBoolean(false),
      "showQuotedHeadline" -> JsBoolean(false),
      "imageCutoutReplace" -> JsBoolean(false)
    )
  )

  "Resolving Metadata using fromContent" - {

    "Content with type cartoon should showByline" in {
      val resolvedMetaData =
        ResolvedMetaData.fromContent(contentWithCartoon, DefaultCardstyle)
      resolvedMetaData should have(Symbol("showByline")(true))
    }

    "Content with type comment should showByline, showQuotedHeadline and imageCutoutReplace" in {
      val resolvedMetaData =
        ResolvedMetaData.fromContent(contentWithComment, Comment)
      resolvedMetaData should have(
        Symbol("showByline")(true),
        Symbol("showQuotedHeadline")(true),
        Symbol("imageCutoutReplace")(true)
      )
    }

    "Content with type video should showMainVideo" in {
      val resolvedMetaData =
        ResolvedMetaData.fromContent(contentWithVideo, DefaultCardstyle)
      resolvedMetaData should have(Symbol("showMainVideo")(true))
    }

    "Content with silly type should all false" in {
      val contentWithVideo = contentWithTags(tagWithId("sillyid"))
      val resolvedMetaData =
        ResolvedMetaData.fromContent(contentWithVideo, DefaultCardstyle)
      resolvedMetaData should have(
        Symbol("showByline")(false),
        Symbol("showQuotedHeadline")(false),
        Symbol("imageCutoutReplace")(false),
        Symbol("showMainVideo")(false),
        Symbol("isBreaking")(false),
        Symbol("imageHide")(false),
        Symbol("imageReplace")(false),
        Symbol("showKickerCustom")(false),
        Symbol("showKickerSection")(false),
        Symbol("showKickerTag")(false)
      )
    }
  }

  "Resolving Metadata using fromTrailMetaData" - {

    "Resolve all to false for empty TrailMetaData" in {
      val resolvedMetaData =
        ResolvedMetaData.fromTrailMetaData(emptyTrailMetaData)
      resolvedMetaData should have(
        Symbol("showByline")(false),
        Symbol("showQuotedHeadline")(false),
        Symbol("imageCutoutReplace")(false),
        Symbol("showMainVideo")(false),
        Symbol("multimediaSlideshowReplace")(false),
        Symbol("isBreaking")(false),
        Symbol("imageHide")(false),
        Symbol("imageReplace")(false),
        Symbol("showKickerCustom")(false),
        Symbol("showKickerSection")(false),
        Symbol("showKickerTag")(false)
      )
    }

    "Resolve all to true for empty TrailMetaData" in {
      val resolvedMetaData =
        ResolvedMetaData.fromTrailMetaData(trailMetaDataWithFieldsSetTrue)
      resolvedMetaData should have(
        Symbol("showByline")(true),
        Symbol("showQuotedHeadline")(true),
        Symbol("imageCutoutReplace")(true),
        Symbol("showMainVideo")(true),
        Symbol("multimediaSlideshowReplace")(true),
        Symbol("isBreaking")(false),
        Symbol("imageHide")(false),
        Symbol("imageReplace")(false),
        Symbol("showKickerCustom")(false),
        Symbol("showKickerSection")(false),
        Symbol("showKickerTag")(false)
      )
    }
  }

  "Resolving Metadata using fromContentAndTrailMetaData" - {

    "should resolve correct for cartoon when trailMetaData is not set" in {
      val resolvedCartoon = ResolvedMetaData.fromContentAndTrailMetaData(
        contentWithCartoon,
        emptyTrailMetaData,
        DefaultCardstyle
      )
      resolvedCartoon should have(Symbol("showByline")(true))
    }

    "should resolve correct for comment when trailMetaData is not set" in {
      val resolvedComment = ResolvedMetaData.fromContentAndTrailMetaData(
        contentWithComment,
        emptyTrailMetaData,
        Comment
      )
      resolvedComment should have(
        Symbol("showByline")(true),
        Symbol("showQuotedHeadline")(true),
        Symbol("imageCutoutReplace")(true)
      )
    }

    "should resolve correct for video when trailMetaData is not set" in {
      val resolvedVideo = ResolvedMetaData.fromContentAndTrailMetaData(
        contentWithVideo,
        emptyTrailMetaData,
        DefaultCardstyle
      )
      resolvedVideo should have(Symbol("showMainVideo")(true))
    }

    "should resolve correct for video with Atom" in {
      val resolvedVideo = ResolvedMetaData.fromContentAndTrailMetaData(
        contentWithAtom,
        emptyTrailMetaData,
        DefaultCardstyle
      )
      resolvedVideo should have(Symbol("showMainVideo")(true))
    }

    "should resolve correct for cartoon when trailMetaData IS set" in {
      val resolvedCartoon = ResolvedMetaData.fromContentAndTrailMetaData(
        contentWithCartoon,
        trailMetaDataWithFieldsSetFalse,
        DefaultCardstyle
      )
      resolvedCartoon should have(Symbol("showByline")(false))
    }

    "should resolve correct for comment when trailMetaData IS set" in {
      val resolvedComment = ResolvedMetaData.fromContentAndTrailMetaData(
        contentWithComment,
        trailMetaDataWithFieldsSetFalse,
        DefaultCardstyle
      )
      resolvedComment should have(
        Symbol("showByline")(false),
        Symbol("showQuotedHeadline")(false),
        Symbol("imageCutoutReplace")(false)
      )
    }

    "should resolve correct for video when trailMetaData IS set" in {
      val resolvedVideo = ResolvedMetaData.fromContentAndTrailMetaData(
        contentWithVideo,
        trailMetaDataWithFieldsSetFalse,
        DefaultCardstyle
      )
      resolvedVideo should have(Symbol("showMainVideo")(false))
    }
  }

}
