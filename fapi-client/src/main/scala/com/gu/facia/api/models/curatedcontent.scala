package com.gu.facia.api.models

import com.gu.contentapi.client.model.v1.{Content, TagType}
import com.gu.contentapi.client.utils.CapiModelEnrichment.RenderingFormat
import com.gu.contentapi.client.utils.format._
import com.gu.contentatom.thrift.{Atom, Image => AtomImage}
import com.gu.facia.api.utils.ContentApiUtils._
import com.gu.facia.api.utils._
import com.gu.facia.client.models.{
  MetaDataCommonFields,
  SupportingItem,
  Test,
  Trail,
  TrailMetaData
}

sealed trait FaciaImage

case class Cutout(
    imageSrc: String,
    imageSrcWidth: Option[String],
    imageSrcHeight: Option[String]
) extends FaciaImage

case class Replace(
    imageSrc: String,
    imageSrcWidth: String,
    imageSrcHeight: String,
    imageCaption: Option[String]
) extends FaciaImage

case class ImageSlideshow(assets: List[Replace]) extends FaciaImage

object FaciaImage {

  def getFaciaImage(
      maybeContent: Option[Content],
      trailMeta: MetaDataCommonFields,
      resolvedMetadata: ResolvedMetaData
  ): Option[FaciaImage] = {
    if (resolvedMetadata.imageHide)
      None
    else if (resolvedMetadata.imageSlideshowReplace)
      imageSlideshow(trailMeta, resolvedMetadata)
    else if (resolvedMetadata.imageReplace)
      imageReplace(trailMeta, resolvedMetadata)
    else if (resolvedMetadata.imageCutoutReplace)
      imageCutout(trailMeta) orElse maybeContent.flatMap(
        fromContentTags(_, trailMeta)
      )
    else None
  }

  def fromContentTags(
      content: Content,
      trailMeta: MetaDataCommonFields
  ): Option[FaciaImage] = {
    val contributorTags = content.tags.filter(_.`type` == TagType.Contributor)
    if (contributorTags.length == 1)
      for {
        tag <- contributorTags.find(_.bylineLargeImageUrl.isDefined)
        path <- tag.bylineLargeImageUrl
      } yield Cutout(path, None, None)
    else None
  }

  def imageCutout(trailMeta: MetaDataCommonFields): Option[FaciaImage] = for {
    src <- trailMeta.imageCutoutSrc
    width <- trailMeta.imageCutoutSrcWidth
    height <- trailMeta.imageCutoutSrcHeight
  } yield Cutout(src, Option(width), Option(height))

  def imageReplace(
      trailMeta: MetaDataCommonFields,
      resolvedMetaData: ResolvedMetaData
  ): Option[FaciaImage] = {
    trailMeta.imageSource match {
      case Some(imageSource) =>
        Some(
          Replace(imageSource.src, imageSource.width, imageSource.height, None)
        )
      case None =>
        for {
          src <- trailMeta.imageSrc
          width <- trailMeta.imageSrcWidth
          height <- trailMeta.imageSrcHeight
        } yield Replace(src, width, height, None)
    }
  }

  def imageSlideshow(
      trailMeta: MetaDataCommonFields,
      resolvedMetaData: ResolvedMetaData
  ): Option[FaciaImage] =
    trailMeta.slideshow.map { assets =>
      val slideshowAssets = assets.map(asset =>
        Replace(asset.src, asset.width, asset.height, asset.caption)
      )
      ImageSlideshow(slideshowAssets)
    }

}
object AtomId {
  def unapply(content: FaciaContent): Option[String] = {
    content.atomId
  }
}

sealed trait FaciaContent {
  def brandingByEdition: BrandingByEdition = Map.empty

  def maybeFrontPublicationDate: Option[Long]

  def href: Option[String]

  def trailText: Option[String]

  def group: String

  def image: Option[FaciaImage]

  def properties: ContentProperties

  def byline: Option[String]

  def kicker: Option[ItemKicker]

  def atomId: Option[String]
}

// This needs to be kept aligned with Frontend until it's pushed all the way upstream to Thrift
// https://github.com/guardian/frontend/blob/46ac997bbb6482bacbd59c3528ce3141623c8033/common/app/model/meta.scala#L220-L224

final case class ContentFormat(
    design: Design,
    theme: Theme,
    display: Display
)

object ContentFormat {
  lazy val defaultContentFormat: ContentFormat = {
    ContentFormat(ArticleDesign, NewsPillar, StandardDisplay)
  }

  def apply(content: Content): ContentFormat = {
    ContentFormat(content.design, content.theme, content.display)
  }
}

object Snap {
  val LatestType = "latest"
  val LinkType = "link"
  val DefaultType = LinkType

  def maybeFromTrail(trail: Trail): Option[Snap] =
    maybeFromTrailAndBrandings(trail, Map.empty)

  def maybeFromTrailAndBrandings(
      trail: Trail,
      brandingByEdition: BrandingByEdition
  ): Option[Snap] =
    trail.safeMeta.snapType match {
      case Some("latest") =>
        Option(LatestSnap.fromTrailAndContent(trail, None))
      case Some(snapType) =>
        val resolvedMetaData =
          ResolvedMetaData.fromTrailMetaData(trail.safeMeta)
        val contentProperties =
          ContentProperties.fromResolvedMetaData(resolvedMetaData)
        Option(
          LinkSnap(
            trail.id,
            Option(trail.frontPublicationDate),
            snapType,
            trail.safeMeta.snapUri,
            trail.safeMeta.snapCss,
            trail.safeMeta.atomId,
            trail.safeMeta.headline,
            trail.safeMeta.href,
            trail.safeMeta.trailText,
            trail.safeMeta.group.getOrElse("0"),
            FaciaImage.getFaciaImage(None, trail.safeMeta, resolvedMetaData),
            contentProperties,
            trail.safeMeta.byline,
            ItemKicker.fromTrailMetaData(trail.safeMeta),
            brandingByEdition,
            None
          )
        )
      case _ => None
    }

  def maybeFromSupportingItem(supportingItem: SupportingItem): Option[Snap] =
    supportingItem.safeMeta.snapType match {
      case Some("latest") =>
        Option(LatestSnap.fromSupportingItemAndContent(supportingItem, None))
      case Some(snapType) =>
        val resolvedMetaData =
          ResolvedMetaData.fromTrailMetaData(supportingItem.safeMeta)
        val contentProperties =
          ContentProperties.fromResolvedMetaData(resolvedMetaData)
        Option(
          LinkSnap(
            supportingItem.id,
            supportingItem.frontPublicationDate,
            snapType,
            supportingItem.safeMeta.snapUri,
            supportingItem.safeMeta.snapCss,
            supportingItem.safeMeta.atomId,
            supportingItem.safeMeta.headline,
            supportingItem.safeMeta.href,
            supportingItem.safeMeta.trailText,
            supportingItem.safeMeta.group.getOrElse("0"),
            FaciaImage
              .getFaciaImage(None, supportingItem.safeMeta, resolvedMetaData),
            contentProperties,
            supportingItem.safeMeta.byline,
            ItemKicker.fromTrailMetaData(supportingItem.safeMeta),
            Map.empty,
            None
          )
        )
      case _ => None
    }
}

sealed trait Snap extends FaciaContent

case class LinkSnap(
    id: String,
    maybeFrontPublicationDate: Option[Long],
    snapType: String,
    snapUri: Option[String],
    snapCss: Option[String],
    atomId: Option[String],
    headline: Option[String],
    href: Option[String],
    trailText: Option[String],
    group: String,
    image: Option[FaciaImage],
    properties: ContentProperties,
    byline: Option[String],
    kicker: Option[ItemKicker],
    override val brandingByEdition: BrandingByEdition,
    mediaAtom: Option[Atom]
) extends Snap

case class LatestSnap(
    id: String,
    maybeFrontPublicationDate: Option[Long],
    cardStyle: CardStyle,
    format: ContentFormat,
    snapUri: Option[String],
    snapCss: Option[String],
    latestContent: Option[Content],
    headline: Option[String],
    href: Option[String],
    trailText: Option[String],
    group: String,
    image: Option[FaciaImage],
    properties: ContentProperties,
    byline: Option[String],
    kicker: Option[ItemKicker],
    override val brandingByEdition: BrandingByEdition,
    atomId: Option[String],
    mediaAtom: Option[Atom]
) extends Snap

object LatestSnap {
  def fromTrailAndContent(
      trail: Trail,
      maybeContent: Option[Content]
  ): LatestSnap = {
    val cardStyle: CardStyle = maybeContent
      .map(CardStyle.apply(_, trail.safeMeta))
      .getOrElse(DefaultCardstyle)
    val contentFormat: ContentFormat = maybeContent
      .map(ContentFormat.apply)
      .getOrElse(ContentFormat.defaultContentFormat)
    val resolvedMetaData: ResolvedMetaData =
      maybeContent.fold(ResolvedMetaData.fromTrailMetaData(trail.safeMeta))(
        ResolvedMetaData.fromContentAndTrailMetaData(
          _,
          trail.safeMeta,
          cardStyle
        )
      )
    val brandingByEdition =
      maybeContent map (_.brandingByEdition) getOrElse Map.empty
    LatestSnap(
      trail.id,
      Option(trail.frontPublicationDate),
      cardStyle,
      contentFormat,
      trail.safeMeta.snapUri,
      trail.safeMeta.snapCss,
      maybeContent,
      trail.safeMeta.headline
        .orElse(maybeContent.flatMap(_.fields.flatMap(_.headline)))
        .orElse(maybeContent.map(_.webTitle)),
      trail.safeMeta.href.orElse(maybeContent.map(_.id)),
      trail.safeMeta.trailText
        .orElse(maybeContent.flatMap(_.fields.flatMap(_.trailText))),
      trail.safeMeta.group.getOrElse("0"),
      FaciaImage.getFaciaImage(maybeContent, trail.safeMeta, resolvedMetaData),
      ContentProperties.fromResolvedMetaData(resolvedMetaData),
      trail.safeMeta.byline
        .orElse(maybeContent.flatMap(_.fields.flatMap(_.byline))),
      ItemKicker.fromMaybeContentTrailMetaAndResolvedMetaData(
        maybeContent,
        trail.safeMeta,
        resolvedMetaData
      ),
      brandingByEdition,
      trail.safeMeta.atomId,
      None
    )
  }

  def fromSupportingItemAndContent(
      supportingItem: SupportingItem,
      maybeContent: Option[Content]
  ): LatestSnap = {
    val cardStyle: CardStyle = maybeContent
      .map(CardStyle.apply(_, supportingItem.safeMeta))
      .getOrElse(DefaultCardstyle)
    val contentFormat: ContentFormat = maybeContent
      .map(ContentFormat.apply)
      .getOrElse(ContentFormat.defaultContentFormat)
    val resolvedMetaData: ResolvedMetaData =
      maybeContent.fold(
        ResolvedMetaData.fromTrailMetaData(supportingItem.safeMeta)
      )(
        ResolvedMetaData.fromContentAndTrailMetaData(
          _,
          supportingItem.safeMeta,
          cardStyle
        )
      )
    val brandingByEdition =
      maybeContent map (_.brandingByEdition) getOrElse Map.empty
    LatestSnap(
      supportingItem.id,
      supportingItem.frontPublicationDate,
      cardStyle,
      contentFormat,
      supportingItem.safeMeta.snapUri,
      supportingItem.safeMeta.snapCss,
      maybeContent,
      supportingItem.safeMeta.headline
        .orElse(maybeContent.flatMap(_.fields.flatMap(_.headline)))
        .orElse(maybeContent.map(_.webTitle)),
      supportingItem.safeMeta.href,
      supportingItem.safeMeta.trailText
        .orElse(maybeContent.flatMap(_.fields.flatMap(_.trailText))),
      supportingItem.safeMeta.group.getOrElse("0"),
      FaciaImage
        .getFaciaImage(maybeContent, supportingItem.safeMeta, resolvedMetaData),
      ContentProperties.fromResolvedMetaData(resolvedMetaData),
      supportingItem.safeMeta.byline
        .orElse(maybeContent.flatMap(_.fields.flatMap(_.byline))),
      ItemKicker.fromMaybeContentTrailMetaAndResolvedMetaData(
        maybeContent,
        supportingItem.safeMeta,
        resolvedMetaData
      ),
      brandingByEdition,
      supportingItem.safeMeta.atomId,
      None
    )
  }
}

case class FaciaMultimediaSlideshow(
    id: String,
    title: Option[String],
    aspectRatio: Option[String],
    slides: List[FaciaSlide]
)

case class FaciaSlide(
    content: FaciaSlideContent,
    caption: Option[String],
    label: Option[String],
    credit: Option[String]
)

sealed trait FaciaSlideContent
case class FaciaSlideImage(image: AtomImage) extends FaciaSlideContent
case class FaciaSlideVideo(mediaAtom: Atom) extends FaciaSlideContent

case class CuratedContent(
    content: Content,
    maybeFrontPublicationDate: Option[Long],
    supportingContent: List[FaciaContent],
    cardStyle: CardStyle,
    format: ContentFormat,
    headline: String,
    href: Option[String],
    trailText: Option[String],
    group: String,
    image: Option[FaciaImage],
    properties: ContentProperties,
    byline: Option[String],
    kicker: Option[ItemKicker],
    embedType: Option[String],
    embedUri: Option[String],
    embedCss: Option[String],
    override val brandingByEdition: BrandingByEdition,
    atomId: Option[String],
    mediaAtom: Option[Atom],
    multimediaSlideshowAtomId: Option[String],
    multimediaSlideshow: Option[FaciaMultimediaSlideshow],
    tests: Option[List[Test]]
) extends FaciaContent

case class SupportingCuratedContent(
    content: Content,
    maybeFrontPublicationDate: Option[Long],
    cardStyle: CardStyle,
    format: ContentFormat,
    headline: String,
    href: Option[String],
    trailText: Option[String],
    group: String,
    image: Option[FaciaImage],
    properties: ContentProperties,
    byline: Option[String],
    kicker: Option[ItemKicker],
    atomId: Option[String],
    mediaAtom: Option[Atom],
    tests: Option[List[Test]]
) extends FaciaContent

object CuratedContent {

  def fromTrailAndContentWithSupporting(
      content: Content,
      trailMetaData: TrailMetaData,
      maybeFrontPublicationDate: Option[Long],
      supportingContent: List[FaciaContent],
      collectionConfig: CollectionConfig,
      tests: Option[List[Test]]
  ) = {
    val cardStyle = CardStyle(content, trailMetaData)
    val resolvedMetaData = ResolvedMetaData.fromContentAndTrailMetaData(
      content,
      trailMetaData,
      cardStyle
    )

    CuratedContent(
      content,
      maybeFrontPublicationDate,
      supportingContent,
      cardStyle,
      ContentFormat(content),
      trailMetaData.headline
        .orElse(content.fields.flatMap(_.headline))
        .getOrElse(content.webTitle),
      trailMetaData.href,
      trailMetaData.trailText.orElse(content.fields.flatMap(_.trailText)),
      trailMetaData.group.getOrElse("0"),
      FaciaImage.getFaciaImage(Some(content), trailMetaData, resolvedMetaData),
      ContentProperties.fromResolvedMetaData(resolvedMetaData),
      trailMetaData.byline.orElse(content.fields.flatMap(_.byline)),
      ItemKicker.fromContentAndTrail(
        Option(content),
        trailMetaData,
        resolvedMetaData,
        Some(collectionConfig)
      ),
      embedType = trailMetaData.snapType,
      embedUri = trailMetaData.snapUri,
      embedCss = trailMetaData.snapCss,
      brandingByEdition = content.brandingByEdition,
      trailMetaData.atomId,
      None,
      trailMetaData.multimediaSlideshowAtomId,
      None,
      tests
    )
  }

  def fromTrailAndContent(
      content: Content,
      trailMetaData: MetaDataCommonFields,
      maybeFrontPublicationDate: Option[Long],
      collectionConfig: CollectionConfig,
      tests: Option[List[Test]]
  ): CuratedContent = {

    val cardStyle = CardStyle(content, trailMetaData)
    val resolvedMetaData = ResolvedMetaData.fromContentAndTrailMetaData(
      content,
      trailMetaData,
      cardStyle
    )

    CuratedContent(
      content,
      maybeFrontPublicationDate,
      supportingContent = Nil,
      cardStyle = cardStyle,
      ContentFormat(content),
      trailMetaData.headline
        .orElse(content.fields.flatMap(_.headline))
        .getOrElse(content.webTitle),
      trailMetaData.href,
      trailMetaData.trailText.orElse(content.fields.flatMap(_.trailText)),
      trailMetaData.group.getOrElse("0"),
      FaciaImage.getFaciaImage(Some(content), trailMetaData, resolvedMetaData),
      ContentProperties.fromResolvedMetaData(resolvedMetaData),
      trailMetaData.byline.orElse(content.fields.flatMap(_.byline)),
      ItemKicker.fromContentAndTrail(
        Option(content),
        trailMetaData,
        resolvedMetaData,
        Some(collectionConfig)
      ),
      embedType = trailMetaData.snapType,
      embedUri = trailMetaData.snapUri,
      embedCss = trailMetaData.snapCss,
      brandingByEdition = content.brandingByEdition,
      trailMetaData.atomId,
      None,
      trailMetaData.multimediaSlideshowAtomId,
      None,
      tests
    )
  }
}

object SupportingCuratedContent {
  def fromTrailAndContent(
      content: Content,
      trailMetaData: MetaDataCommonFields,
      maybeFrontPublicationDate: Option[Long],
      collectionConfig: CollectionConfig,
      tests: Option[List[Test]]
  ): SupportingCuratedContent = {
    val cardStyle = CardStyle(content, trailMetaData)
    val resolvedMetaData = ResolvedMetaData.fromContentAndTrailMetaData(
      content,
      trailMetaData,
      cardStyle
    )

    SupportingCuratedContent(
      content,
      maybeFrontPublicationDate,
      cardStyle,
      ContentFormat(content),
      trailMetaData.headline
        .orElse(content.fields.flatMap(_.headline))
        .getOrElse(content.webTitle),
      trailMetaData.href,
      trailMetaData.trailText.orElse(content.fields.flatMap(_.trailText)),
      trailMetaData.group.getOrElse("0"),
      FaciaImage.getFaciaImage(Some(content), trailMetaData, resolvedMetaData),
      ContentProperties.fromResolvedMetaData(resolvedMetaData),
      trailMetaData.byline.orElse(content.fields.flatMap(_.byline)),
      ItemKicker.fromContentAndTrail(
        Option(content),
        trailMetaData,
        resolvedMetaData,
        None
      ),
      trailMetaData.atomId,
      None,
      tests
    )
  }
}

case class EventGraphic(
    id: String,
    properties: ContentProperties,
    group: String
) extends FaciaContent {
  override def maybeFrontPublicationDate: Option[Long] = None
  override def href: Option[String] = None
  override def trailText: Option[String] = None
  override def image: Option[FaciaImage] = None
  override def byline: Option[String] = None
  override def kicker: Option[ItemKicker] = None
  override def atomId: Option[String] = None
}

object EventGraphic {
  def fromTrail(trail: Trail): EventGraphic = {
    EventGraphic(
      id = trail.id.stripPrefix(Trail.eventGraphicPrefix),
      properties =
        ContentProperties.fromResolvedMetaData(ResolvedMetaData.Default),
      group = trail.safeMeta.group.getOrElse("0")
    )
  }
}
