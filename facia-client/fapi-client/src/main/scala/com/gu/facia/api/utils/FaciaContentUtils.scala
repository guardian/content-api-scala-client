package com.gu.facia.api.utils

import com.gu.contentapi.client.model.v1._
import com.gu.facia.api.models._
import org.joda.time.DateTime

import scala.util.Try

object FaciaContentUtils {
  import ContentApiUtils._
  import com.gu.facia.api.utils.CapiModelEnrichment.RichCapiDateTime

  def fold[T](fc: FaciaContent)(
      c: (CuratedContent) => T,
      scc: (SupportingCuratedContent) => T,
      ls: (LinkSnap) => T,
      las: (LatestSnap) => T,
      eg: (EventGraphic) => T
  ): T = fc match {
    case curatedContent: CuratedContent                     => c(curatedContent)
    case supportingCuratedContent: SupportingCuratedContent =>
      scc(supportingCuratedContent)
    case linkSnap: LinkSnap         => ls(linkSnap)
    case latestSnap: LatestSnap     => las(latestSnap)
    case eventGraphic: EventGraphic => eg(eventGraphic)
  }

  def maybeContent(fc: FaciaContent): Option[Content] = fold(fc)(
    curatedContent => Option(curatedContent.content),
    supportingCuratedContent => Option(supportingCuratedContent.content),
    _ => None,
    latestSnap => latestSnap.latestContent,
    _ => None
  )

  def tags(fc: FaciaContent): List[com.gu.contentapi.client.model.v1.Tag] =
    maybeContent(fc).map(_.tags.toList).getOrElse(Nil)

  def webPublicationDateOption(fc: FaciaContent): Option[DateTime] = fold(fc)(
    c = curatedContent => curatedContent.content.webPublicationDate,
    scc = supportingCuratedContent => supportingCuratedContent.content.webPublicationDate,
    ls = _ => None,
    las = latestSnap => latestSnap.latestContent.flatMap(_.webPublicationDate),
    eg = _ => None
  ).map(_.toJodaDateTime)

  def webPublicationDate(fc: FaciaContent): DateTime =
    webPublicationDateOption(fc).getOrElse(DateTime.now)

  def id(fc: FaciaContent): String = fold(fc)(
    c = curatedContent => curatedContent.content.id,
    scc = supportingCuratedContent => supportingCuratedContent.content.id,
    ls = linkSnap => linkSnap.id,
    las = latestSnap => latestSnap.id,
    eg = eg => eg.id
  )

  def maybeContentId(fc: FaciaContent): Option[String] = fold(fc)(
    c = curatedContent => Option(curatedContent.content.id),
    scc = supportingCuratedContent => Option(supportingCuratedContent.content.id),
    ls = _ => None,
    las = latestSnap => latestSnap.latestContent.map(_.id),
    eg = _ => None
  )

  def group(fc: FaciaContent): String = fc.group

  def embedType(fc: FaciaContent): Option[String] = fold(fc)(
    c = curatedContent => curatedContent.embedType,
    scc = supportingCuratedContent => None,
    ls = linkSnap => Option(linkSnap.snapType),
    las = latestSnap => Option("latest"),
    eg = _ => None
  )

  def embedCss(fc: FaciaContent): Option[String] = fold(fc)(
    c = curatedContent => curatedContent.embedCss,
    scc = supportingCuratedContent => None,
    ls = linkSnap => linkSnap.snapCss,
    las = latestSnap => latestSnap.snapCss,
    eg = _ => None
  )

  def embedUri(fc: FaciaContent): Option[String] = fold(fc)(
    c = curatedContent => curatedContent.embedUri,
    scc = supportingCuratedContent => None,
    ls = linkSnap => linkSnap.snapUri,
    las = latestSnap => latestSnap.snapUri,
    eg = _ => None
  )

  def itemKicker(fc: FaciaContent): Option[ItemKicker] = fc.kicker

  def headlineOption(fc: FaciaContent): Option[String] = fold(fc)(
    c = curatedContent => Option(curatedContent.headline),
    scc = supportingCuratedContent => Option(supportingCuratedContent.headline),
    ls = linkSnap => linkSnap.headline,
    las = latestSnap => latestSnap.headline,
    eg = _ => None
  )

  def headline(fc: FaciaContent): String =
    headlineOption(fc).getOrElse("Missing Headline")

  def standfirst(fc: FaciaContent): Option[String] =
    fieldsGet(fc)(_.flatMap(_.standfirst))

  def body(fc: FaciaContent): Option[String] = fieldsGet(fc)(_.flatMap(_.body))

  def webUrl(fc: FaciaContent): Option[String] = fold(fc)(
    c = curatedContent => Option(curatedContent.content.webUrl),
    scc = supportingCuratedContent => Option(supportingCuratedContent.content.webUrl),
    ls = linkSnap => linkSnap.snapUri,
    las = latestSnap => latestSnap.latestContent.map(_.webUrl),
    eg = _ => None
  )

  val DefaultSnapHref: String = "/"
  def href(fc: FaciaContent): Option[String] = fold(fc)(
    c = curatedContent => curatedContent.href,
    scc = supportingCuratedContent => supportingCuratedContent.href,
    ls = linkSnap => linkSnap.href.orElse(linkSnap.snapUri),
    las = latestSnap => latestSnap.href.orElse(latestSnap.snapUri),
    eg = _ => None
  )

  def atomId(fc: FaciaContent): Option[String] = fc.atomId

  def mediaType(fc: FaciaContent): Option[MediaType] = {
    def mediaTypeFromContent(content: Content): Option[MediaType] =
      if (isGallery(fc)) Option(Gallery)
      else if (isAudio(fc)) Option(Audio)
      else if (isVideo(fc)) Option(Video)
      else None
    fold(fc)(
      c = curatedContent => mediaTypeFromContent(curatedContent.content),
      scc = supportingCuratedContent => mediaTypeFromContent(supportingCuratedContent.content),
      ls = _ => None,
      las = latestSnap => latestSnap.latestContent.flatMap(mediaTypeFromContent),
      eg = _ => None
    )
  }

  def isLive(fc: FaciaContent): Boolean = fold(fc)(
    c = curatedContent => curatedContent.content.fields.flatMap(_.liveBloggingNow).exists(identity),
    scc = supportingCuratedContent =>
      supportingCuratedContent.content.fields
        .flatMap(_.liveBloggingNow)
        .exists(identity),
    ls = _ => false,
    las = latestSnap =>
      latestSnap.latestContent.exists(
        _.fields.flatMap(_.liveBloggingNow).exists(identity)
      ),
    eg = _ => false
  )

  private def fieldsExists(fc: FaciaContent)(
      f: (Option[ContentFields]) => Boolean
  ): Boolean = fold(fc)(
    c = curatedContent => f(curatedContent.content.fields),
    scc = supportingCuratedContent => f(supportingCuratedContent.content.fields),
    ls = _ => false,
    las = latestSnap => latestSnap.latestContent.exists(c => f(c.fields)),
    eg = _ => false
  )
  def isCommentable(fc: FaciaContent) =
    fieldsExists(fc)(_.flatMap(_.commentable).exists(identity))
  def commentCloseDate(fc: FaciaContent) =
    fieldsGet(fc)(_.flatMap(_.commentCloseDate))
  private def fieldsGet[T](fc: FaciaContent)(
      f: (Option[ContentFields]) => Option[T]
  ): Option[T] = fold(fc)(
    c = curatedContent => f(curatedContent.content.fields),
    scc = supportingCuratedContent => f(supportingCuratedContent.content.fields),
    ls = _ => None,
    las = latestSnap => latestSnap.latestContent.flatMap(c => f(c.fields)),
    eg = _ => None
  )
  def maybeShortUrl(fc: FaciaContent) = fieldsGet(fc)(_.flatMap(_.shortUrl))
  def shortUrl(fc: FaciaContent): String = maybeShortUrl(fc).getOrElse("")
  def shortUrlPath(fc: FaciaContent) = maybeShortUrl(fc).map(
    _.replaceFirst("^https?://www.theguardian.com", "")
  )
  def discussionId(fc: FaciaContent) = shortUrlPath(fc)

  def boostLevel(fc: FaciaContent): BoostLevel = fc.properties.boostLevel
  def showBoostedHeadline(fc: FaciaContent): Boolean =
    fc.properties.showBoostedHeadline
  def showQuotedHeadline(fc: FaciaContent): Boolean =
    fc.properties.showQuotedHeadline
  def isImmersive(fc: FaciaContent): Boolean = fc.properties.isImmersive
  def showMainVideo(fc: FaciaContent): Boolean = fc.properties.showMainVideo

  def videoReplace(fc: FaciaContent): Boolean = fc.properties.videoReplace

  def multimediaSlideshowReplace(fc: FaciaContent): Boolean =
    fc.properties.multimediaSlideshowReplace

  def showLivePlayable(fc: FaciaContent): Boolean =
    fc.properties.showLivePlayable
  def sectionName(fc: FaciaContent): Option[String] = fold(fc)(
    c = curatedContent => curatedContent.content.sectionName,
    scc = supportingCuratedContent => supportingCuratedContent.content.sectionName,
    ls = linkSnap => None,
    las = latestSnap => latestSnap.latestContent.flatMap(_.sectionName),
    eg = _ => None
  )
  def maybeSection(fc: FaciaContent): Option[String] = fold(fc)(
    c = curatedContent => curatedContent.content.sectionId,
    scc = supportingCuratedContent => supportingCuratedContent.content.sectionId,
    ls = linkSnap => None,
    las = latestSnap => latestSnap.latestContent.flatMap(_.sectionId),
    eg = _ => None
  )
  def section(fc: FaciaContent): String = maybeSection(fc).getOrElse("")

  def byline(fc: FaciaContent): Option[String] = fc.byline

  def showByline(fc: FaciaContent): Boolean = fc.properties.showByline

  private def tagsOfType(fc: FaciaContent)(tagType: TagType): Seq[Tag] =
    tags(fc).filter(_.`type` == tagType)
  def nonKeywordTags(fc: FaciaContent): Seq[Tag] =
    tags(fc).filterNot(_.`type` == TagType.Keyword)
  def keywords(fc: FaciaContent): Seq[Tag] = tagsOfType(fc)(TagType.Keyword)
  def series(fc: FaciaContent): Seq[Tag] = tagsOfType(fc)(TagType.Series)
  def blogs(fc: FaciaContent): Seq[Tag] = tagsOfType(fc)(TagType.Blog)
  def tones(fc: FaciaContent): Seq[Tag] = tagsOfType(fc)(TagType.Tone)
  def types(fc: FaciaContent): Seq[Tag] = tagsOfType(fc)(TagType.Type)

  def contributors(fc: FaciaContent): Seq[Tag] =
    maybeContent(fc).map(_.contributors).getOrElse(Nil)
  def isContributorPage(fc: FaciaContent): Boolean =
    maybeContent(fc).exists(_.contributors.nonEmpty)
  def isVideo(fc: FaciaContent) = maybeContent(fc).exists(_.isVideo)
  def isGallery(fc: FaciaContent) = maybeContent(fc).exists(_.isGallery)
  def isAudio(fc: FaciaContent) = maybeContent(fc).exists(_.isAudio)
  def isCartoon(fc: FaciaContent) = maybeContent(fc).exists(_.isCartoon)
  def isArticle(fc: FaciaContent) = maybeContent(fc).exists(_.isArticle)
  def isCrossword(fc: FaciaContent) = maybeContent(fc).exists(_.isCrossword)
  def isLiveBlog(fc: FaciaContent): Boolean =
    maybeContent(fc).exists(_.isLiveBlog)
  def isPodcast(fc: FaciaContent): Boolean =
    maybeContent(fc).exists(_.isPodcast)
  def isMedia(fc: FaciaContent): Boolean = maybeContent(fc).exists(_.isMedia)
  def isEditorial(fc: FaciaContent): Boolean =
    maybeContent(fc).exists(_.isEditorial)
  def isComment(fc: FaciaContent): Boolean =
    maybeContent(fc).exists(_.isComment)
  def isAnalysis(fc: FaciaContent): Boolean =
    maybeContent(fc).exists(_.isAnalysis)
  def isReview(fc: FaciaContent): Boolean = maybeContent(fc).exists(_.isReview)
  def isLetters(fc: FaciaContent): Boolean =
    maybeContent(fc).exists(_.isLetters)
  def isFeature(fc: FaciaContent): Boolean =
    maybeContent(fc).exists(_.isFeature)

  def supporting(fc: FaciaContent): List[FaciaContent] = fc match {
    case c: CuratedContent           => c.supportingContent
    case _: Snap                     => Nil
    case _: SupportingCuratedContent => Nil
    case _: EventGraphic             => Nil
  }

  def starRating(fc: FaciaContent): Option[Int] = Try(
    fieldsGet(fc)(_.flatMap(_.starRating))
  ).toOption.flatten

  def trailText(fc: FaciaContent): Option[String] = fc.trailText

  def wordCount(fc: FaciaContent): Option[Int] =
    fieldsGet(fc)(_.flatMap(_.wordcount))

  def maybeWebTitle(fc: FaciaContent): Option[String] = fold(fc)(
    c = curatedContent => Option(curatedContent.content.webTitle),
    scc = supportingCuratedContent => Option(supportingCuratedContent.content.webTitle),
    ls = _ => None,
    las = latestSnap => latestSnap.latestContent.map(_.webTitle),
    eg = _ => None
  )

  def webTitle(fc: FaciaContent): String = maybeWebTitle(fc).getOrElse("")

  def linkText(fc: FaciaContent) = maybeWebTitle(fc)

  def elements(fc: FaciaContent): List[Element] = fold(fc)(
    c = curatedContent => curatedContent.content.elements.map(_.toList).getOrElse(Nil),
    scc = supportingCuratedContent => supportingCuratedContent.content.elements.map(_.toList).getOrElse(Nil),
    ls = _ => Nil,
    las = latestSnap => latestSnap.latestContent.flatMap(_.elements.map(_.toList)).getOrElse(Nil),
    eg = _ => Nil
  )

  def cardStyle(fc: FaciaContent): CardStyle = fold(fc)(
    curatedContent => curatedContent.cardStyle,
    supportingCuratedContent => supportingCuratedContent.cardStyle,
    linkSnap =>
      if (linkSnap.href.exists(ExternalLinks.external)) ExternalLink
      else DefaultCardstyle,
    latestSnap => latestSnap.cardStyle,
    eg => DefaultCardstyle
  )

  def image(fc: FaciaContent): Option[FaciaImage] = fc.image

  def isClosedForComments(fc: FaciaContent) = fieldsExists(fc)(
    !_.flatMap(_.commentCloseDate).exists(_.toJodaDateTime.isAfterNow)
  )

  def properties(fc: FaciaContent): Option[ContentProperties] = fold(fc)(
    c = curatedContent => Option(curatedContent.properties),
    scc = supportingCuratedContent => Option(supportingCuratedContent.properties),
    ls = _ => None,
    las = latestSnap => Option(latestSnap.properties),
    eg = _ => None
  )

  def maybeFrontPublicationDate(fc: FaciaContent): Option[Long] =
    fc.maybeFrontPublicationDate
}
