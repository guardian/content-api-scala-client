package com.gu.facia.api.utils

import com.gu.facia.client.models.MetaDataCommonFields
import play.api.libs.json.{Format, Json}
import com.gu.contentapi.client.model.v1.{Content, Element, ElementType}

sealed trait BoostLevel {
  def label: String
}

object BoostLevel {
  case object Default extends BoostLevel {
    val label = "default"
  }
  case object Boost extends BoostLevel {
    val label = "boost"
  }
  case object MegaBoost extends BoostLevel {
    val label = "megaboost"
  }
  case object GigaBoost extends BoostLevel {
    val label = "gigaboost"
  }
  def fromMetaData(value: String): BoostLevel = value match {
    case GigaBoost.label => GigaBoost
    case MegaBoost.label => MegaBoost
    case Boost.label     => Boost
    case _               => Default
  }
}

object ResolvedMetaData {
  implicit val resolvedMetaDataFormat: Format[ResolvedMetaData] =
    Json.format[ResolvedMetaData]

  val Cartoon = "type/cartoon"
  val Video = "type/video"
  val Comment = "tone/comment"

  def isCartoonForContent(content: Content): Boolean =
    content.tags.exists(_.id == Cartoon)

  def isCommentForContent(content: Content): Boolean =
    content.tags.exists(_.id == Comment)

  def isVideoForContent(content: Content): Boolean =
    content.tags.exists(_.id == Video)

  val Default = ResolvedMetaData(
    isBreaking = false,
    boostLevel = BoostLevel.Default.label,
    isImmersive = false,
    imageHide = false,
    imageReplace = false,
    showKickerSection = false,
    showKickerCustom = false,
    showBoostedHeadline = false,
    showMainVideo = false,
    showLivePlayable = false,
    showKickerTag = false,
    showByline = false,
    imageCutoutReplace = false,
    showQuotedHeadline = false,
    imageSlideshowReplace = false,
    videoReplace = false,
    multimediaSlideshowReplace = false
  )

  def fromTrailMetaData(trailMeta: MetaDataCommonFields): ResolvedMetaData =
    ResolvedMetaData(
      isBreaking = trailMeta.isBreaking.exists(identity),
      boostLevel = trailMeta.boostLevel.getOrElse(BoostLevel.Default.label),
      isImmersive = trailMeta.isImmersive.exists(identity),
      imageHide = trailMeta.imageHide.exists(identity),
      imageReplace = trailMeta.imageReplace.exists(identity),
      showKickerSection = trailMeta.showKickerSection.exists(identity),
      showKickerCustom = trailMeta.showKickerCustom.exists(identity),
      showBoostedHeadline = trailMeta.showBoostedHeadline.exists(identity),
      showMainVideo = trailMeta.showMainVideo.exists(identity),
      showLivePlayable = trailMeta.showLivePlayable.exists(identity),
      showKickerTag = trailMeta.showKickerTag.exists(identity),
      showByline = trailMeta.showByline.exists(identity),
      imageCutoutReplace = trailMeta.imageCutoutReplace.exists(identity),
      showQuotedHeadline = trailMeta.showQuotedHeadline.exists(identity),
      imageSlideshowReplace = trailMeta.imageSlideshowReplace.exists(identity),
      videoReplace = trailMeta.videoReplace.exists(identity),
      multimediaSlideshowReplace = trailMeta.multimediaSlideshowReplace.exists(identity)
    )

  def fromContent(content: Content, cardStyle: CardStyle): ResolvedMetaData =
    cardStyle match {
      case com.gu.facia.api.utils.Comment =>
        Default.copy(
          showByline = true,
          showQuotedHeadline = true,
          imageCutoutReplace = true
        )
      case _ if isCartoonForContent(content) => Default.copy(showByline = true)
      case _ if isVideoForContent(content)   => Default.copy(showMainVideo = true)
      case _                                 => Default
    }

  def fromContentAndTrailMetaData(
      content: Content,
      trailMeta: MetaDataCommonFields,
      cardStyle: CardStyle
  ): ResolvedMetaData = {
    val metaDataFromContent = fromContent(content, cardStyle)
    metaDataFromContent.copy(
      isBreaking = trailMeta.isBreaking.getOrElse(metaDataFromContent.isBreaking),
      boostLevel = trailMeta.boostLevel.getOrElse(BoostLevel.Default.label),
      isImmersive = trailMeta.isImmersive.getOrElse(metaDataFromContent.isImmersive),
      imageHide = trailMeta.imageHide.getOrElse(metaDataFromContent.imageHide),
      imageReplace = trailMeta.imageReplace.getOrElse(metaDataFromContent.imageReplace),
      showKickerSection = trailMeta.showKickerSection.getOrElse(
        metaDataFromContent.showKickerSection
      ),
      showKickerCustom = trailMeta.showKickerCustom.getOrElse(
        metaDataFromContent.showKickerCustom
      ),
      showBoostedHeadline = trailMeta.showBoostedHeadline.getOrElse(
        metaDataFromContent.showBoostedHeadline
      ),
      showMainVideo = trailMeta.showMainVideo.getOrElse(metaDataFromContent.showMainVideo),
      showLivePlayable = trailMeta.showLivePlayable.getOrElse(
        metaDataFromContent.showLivePlayable
      ),
      showKickerTag = trailMeta.showKickerTag.getOrElse(metaDataFromContent.showKickerTag),
      showByline = trailMeta.showByline.getOrElse(metaDataFromContent.showByline),
      imageCutoutReplace = trailMeta.imageCutoutReplace.getOrElse(
        metaDataFromContent.imageCutoutReplace
      ),
      showQuotedHeadline = trailMeta.showQuotedHeadline.getOrElse(
        metaDataFromContent.showQuotedHeadline
      ),
      imageSlideshowReplace = trailMeta.imageSlideshowReplace.getOrElse(
        metaDataFromContent.imageSlideshowReplace
      ),
      videoReplace = trailMeta.videoReplace.getOrElse(metaDataFromContent.videoReplace),
      multimediaSlideshowReplace = trailMeta.multimediaSlideshowReplace
        .getOrElse(metaDataFromContent.multimediaSlideshowReplace)
    )
  }

  def toMap(resolvedMetaData: ResolvedMetaData): Map[String, Boolean] =
    resolvedMetaData match {
      case ResolvedMetaData(
            isBreaking,
            boostLevel,
            isImmersive,
            imageHide,
            imageReplace,
            showKickerSection,
            showKickerCustom,
            showBoostedHeadline,
            showMainVideo,
            showLivePlayable,
            showKickerTag,
            showByline,
            imageCutoutReplace,
            showQuotedHeadline,
            imageSlideshowReplace,
            videoReplace,
            multimediaSlideshowReplace
          ) =>
        Map(
          "isBreaking" -> isBreaking,
          "boostLevel.default" -> (BoostLevel.fromMetaData(
            boostLevel
          ) == BoostLevel.Default),
          "boostLevel.boost" -> (BoostLevel.fromMetaData(
            boostLevel
          ) == BoostLevel.Boost),
          "boostLevel.megaboost" -> (BoostLevel.fromMetaData(
            boostLevel
          ) == BoostLevel.MegaBoost),
          "boostLevel.gigaBoost" -> (BoostLevel.fromMetaData(
            boostLevel
          ) == BoostLevel.GigaBoost),
          "isImmersive" -> isImmersive,
          "imageHide" -> imageHide,
          "imageReplace" -> imageReplace,
          "showKickerSection" -> showKickerSection,
          "showKickerCustom" -> showKickerCustom,
          "showBoostedHeadline" -> showBoostedHeadline,
          "showMainVideo" -> showMainVideo,
          "showLivePlayable" -> showLivePlayable,
          "showKickerTag" -> showKickerTag,
          "showByline" -> showByline,
          "imageCutoutReplace" -> imageCutoutReplace,
          "showQuotedHeadline" -> showQuotedHeadline,
          "imageSlideshowReplace" -> imageSlideshowReplace,
          "videoReplace" -> videoReplace,
          "multimediaSlideshowReplace" -> multimediaSlideshowReplace
        )
    }
}

case class ResolvedMetaData(
    isBreaking: Boolean,
    boostLevel: String,
    isImmersive: Boolean,
    imageHide: Boolean,
    imageReplace: Boolean,
    showKickerSection: Boolean,
    showKickerCustom: Boolean,
    showBoostedHeadline: Boolean,
    showMainVideo: Boolean,
    showLivePlayable: Boolean,
    showKickerTag: Boolean,
    showByline: Boolean,
    imageCutoutReplace: Boolean,
    showQuotedHeadline: Boolean,
    imageSlideshowReplace: Boolean,
    videoReplace: Boolean,
    multimediaSlideshowReplace: Boolean
)

object ContentProperties {
  def fromResolvedMetaData(
      resolvedMetaData: ResolvedMetaData
  ): ContentProperties =
    ContentProperties(
      isBreaking = resolvedMetaData.isBreaking,
      boostLevel = BoostLevel.fromMetaData(resolvedMetaData.boostLevel),
      isImmersive = resolvedMetaData.isImmersive,
      imageHide = resolvedMetaData.imageHide,
      showBoostedHeadline = resolvedMetaData.showBoostedHeadline,
      showMainVideo = resolvedMetaData.showMainVideo,
      showLivePlayable = resolvedMetaData.showLivePlayable,
      showKickerTag = resolvedMetaData.showKickerTag,
      showByline = resolvedMetaData.showByline,
      showQuotedHeadline = resolvedMetaData.showQuotedHeadline,
      imageSlideshowReplace = resolvedMetaData.imageSlideshowReplace,
      videoReplace = resolvedMetaData.videoReplace,
      multimediaSlideshowReplace = resolvedMetaData.multimediaSlideshowReplace
    )
}

case class ContentProperties(
    isBreaking: Boolean,
    boostLevel: BoostLevel,
    isImmersive: Boolean,
    imageHide: Boolean,
    showBoostedHeadline: Boolean,
    showMainVideo: Boolean,
    showLivePlayable: Boolean,
    showKickerTag: Boolean,
    showByline: Boolean,
    showQuotedHeadline: Boolean,
    imageSlideshowReplace: Boolean,
    videoReplace: Boolean,
    multimediaSlideshowReplace: Boolean
)
