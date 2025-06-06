package com.gu.contentapi.client.utils

import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.utils.format._

import org.apache.commons.codec.digest.DigestUtils

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import java.time.ZoneOffset

object CapiModelEnrichment {

  type ContentFilter = Content => Boolean

  def getFromPredicate[T](content: Content, predicates: List[(ContentFilter, T)]): Option[T] =
    predicates.collectFirst { case (predicate, t) if predicate(content) => t }

  def tagExistsWithId(tagId: String): ContentFilter = content => content.tags.exists(tag => tag.id == tagId)

  def displayHintExistsWithName(displayHintName: String): ContentFilter = content => content.fields.flatMap(_.displayHint).contains(displayHintName)

  def isLiveBloggingNow: ContentFilter = content => content.fields.flatMap(_.liveBloggingNow).contains(true)

  val isImmersive: ContentFilter = content => displayHintExistsWithName("immersive")(content)

  val isPhotoEssay: ContentFilter = content => displayHintExistsWithName("photoEssay")(content)

  val isMedia: ContentFilter = content => tagExistsWithId("type/audio")(content) || tagExistsWithId("type/video")(content) || tagExistsWithId("type/gallery")(content)

  val isReview: ContentFilter = content => tagExistsWithId("tone/reviews")(content) || tagExistsWithId("tone/livereview")(content) || tagExistsWithId("tone/albumreview")(content)

  val isLiveBlog: ContentFilter = content => isLiveBloggingNow(content) && tagExistsWithId("tone/minutebyminute")(content)

  val isDeadBlog: ContentFilter = content => !isLiveBloggingNow(content) && tagExistsWithId("tone/minutebyminute")(content)

  val isInteractive: ContentFilter = content => content.`type` == ContentType.Interactive

  val isPictureContent: ContentFilter = content => content.`type` == ContentType.Picture

  val isGallery: ContentFilter = tagExistsWithId("type/gallery")

  // The date used here is arbitrary and will be moved nearer to the present when the new template feature is ready to be used in production
  val immersiveInteractiveSwitchoverDate = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
  
  val publishedBeforeInteractiveImmersiveSwitchover: ContentFilter = content => content.fields.flatMap(_.creationDate).exists(date => ZonedDateTime.parse(date.iso8601).isBefore(immersiveInteractiveSwitchoverDate))
  
  val isLegacyImmersiveInteractive: ContentFilter = content => isInteractive(content) && isImmersive(content) && publishedBeforeInteractiveImmersiveSwitchover(content)

  val isObituary: ContentFilter = content => (tagExistsWithId("tone/obituaries")(content) && !tagExistsWithId("tone/letters")(content))

  val isFullPageInteractive: ContentFilter = content => isInteractive(content) && (displayHintExistsWithName("fullPageInteractive")(content) || isLegacyImmersiveInteractive(content))
  implicit class RichCapiDateTime(val cdt: CapiDateTime) extends AnyVal {
    def toOffsetDateTime: OffsetDateTime = OffsetDateTime.parse(cdt.iso8601)
  }

  implicit class RichOffsetDateTime(val dt: OffsetDateTime) extends AnyVal {
    def toCapiDateTime: CapiDateTime = CapiDateTime.apply(dt.toInstant.toEpochMilli, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dt))
  }

  implicit class RichContent(val content: Content) extends AnyVal {

    def designType: DesignType = {

      val isComment: ContentFilter = content => tagExistsWithId("tone/comment")(content) || tagExistsWithId("tone/letters")(content)
      val defaultDesignType: DesignType = Article

      val predicates: List[(ContentFilter, DesignType)] = List(
        tagExistsWithId("tone/advertisement-features") -> AdvertisementFeature,
        tagExistsWithId("tone/matchreports") -> MatchReport,
        tagExistsWithId("tone/quizzes") -> Quiz,
        isImmersive -> Immersive,
        tagExistsWithId("tone/editorials") -> GuardianView,
        tagExistsWithId("tone/interview") -> Interview,
        tagExistsWithId("tone/recipes") -> Recipe,
        isMedia -> Media,
        isReview -> Review,
        tagExistsWithId("tone/analysis") -> Analysis,
        isComment -> Comment,
        tagExistsWithId("tone/features") -> Feature,
        isLiveBlog -> Live,
        isDeadBlog -> Article,
        tagExistsWithId("tone/newsletter-tone") -> Newsletter
      )

      val result = getFromPredicate(content, predicates)
      result.getOrElse(defaultDesignType)
    }
  }

  implicit class RenderingFormat(val content: Content) extends AnyVal {

    def design: Design = {

      val defaultDesign: Design = ArticleDesign

      // Note: only the first matching predicate will be picked.
      // Modifying the order of predicates could create unintended problems:
      val predicates: List[(ContentFilter, Design)] = List(
        isFullPageInteractive -> FullPageInteractiveDesign,
        isInteractive -> InteractiveDesign,
        tagExistsWithId("info/newsletter-sign-up") -> NewsletterSignupDesign,
        isGallery -> GalleryDesign,
        isPictureContent -> PictureDesign,
        tagExistsWithId("type/audio") -> AudioDesign,
        tagExistsWithId("type/video") -> VideoDesign,
        tagExistsWithId("type/crossword") -> CrosswordDesign,
        isReview -> ReviewDesign,
        isObituary -> ObituaryDesign,
        tagExistsWithId("tone/analysis") -> AnalysisDesign,
        tagExistsWithId("tone/explainers") -> ExplainerDesign,
        tagExistsWithId("tone/comment") -> CommentDesign,
        tagExistsWithId("tone/letters") -> LetterDesign,
        isPhotoEssay -> PhotoEssayDesign,
        tagExistsWithId("tone/interview") -> InterviewDesign,
        tagExistsWithId("tone/recipes") -> RecipeDesign,
        tagExistsWithId("tone/editorials") -> EditorialDesign,
        tagExistsWithId("tone/quizzes") -> QuizDesign,
        isLiveBlog -> LiveBlogDesign,
        isDeadBlog -> DeadBlogDesign,
        tagExistsWithId("tone/features") -> FeatureDesign,
        tagExistsWithId("tone/matchreports") -> MatchReportDesign,
        tagExistsWithId("tone/timelines") -> TimelineDesign,
        tagExistsWithId("tone/profiles") -> ProfileDesign,
      )

      val result = getFromPredicate(content, predicates)
      result.getOrElse(defaultDesign)
    }

    def theme: Theme = {
      val defaultTheme: Theme = NewsPillar

      val specialReportAltTags: Set[String] = Set(
        "news/series/cotton-capital",
        "news/series/cotton-capital-ongoing-series"
      )

      val specialReportTags: Set[String] = Set(
        "business/series/undercover-in-the-chicken-industry",
        "business/series/britains-debt-timebomb",
        "environment/series/the-polluters",
        "news/series/hsbc-files",
        "news/series/panama-papers",
        "us-news/homan-square",
        "uk-news/series/the-new-world-of-work",
        "world/series/the-new-arrivals",
        "news/series/nauru-files",
        "us-news/series/counted-us-police-killings",
        "australia-news/series/healthcare-in-detention",
        "society/series/this-is-the-nhs",
        "news/series/facebook-files",
        "news/series/pegasus-project",
        "news/series/pandora-papers",
        "news/series/suisse-secrets",
        "uk-news/series/cost-of-the-crown"
      )

      // Special Report hashes can be generated by executing:
      // echo -n '<salt><tag-id>' | md5sum
      val hashedSpecialReportTags: Set[String] = Set(
        "0d18e8413ab7cdf377e1202d24452e63"
      )

      val hashedSpecialReportAltTags: Set[String] = Set(
      )

      val salt = "a-public-salt3W#ywHav!p+?r+W2$E6="

      def isPillar(pillar: String): ContentFilter = content => content.pillarName.contains(pillar)

      def hashedTagIds(content: Content) = content.tags.map { tag =>
        DigestUtils.md5Hex(salt + tag.id)
      }

      val isSpecialReport: ContentFilter = content =>
        content.tags.exists(t => specialReportTags(t.id)) || hashedTagIds(content).exists(hashedSpecialReportTags.apply)

      val isSpecialReportAlt: ContentFilter = content =>
        content.tags.exists(t => specialReportAltTags(t.id)) || hashedTagIds(content).exists(hashedSpecialReportAltTags.apply)

      val isOpinion: ContentFilter = content =>
        (tagExistsWithId("tone/comment")(content) && isPillar("News")(content)) ||
          (tagExistsWithId("tone/letters")(content) && isPillar("News")(content)) ||
          isPillar("Opinion")(content)
      val isCulture: ContentFilter = content => isPillar("Arts")(content)

      val predicates: List[(ContentFilter, Theme)] = List(
        isSpecialReport -> SpecialReportTheme,
        isSpecialReportAlt -> SpecialReportAltTheme,
        tagExistsWithId("tone/advertisement-features") -> Labs,
        isOpinion -> OpinionPillar,
        isPillar("Sport") -> SportPillar,
        isCulture -> CulturePillar,
        isPillar("Lifestyle") -> LifestylePillar
      )

      val result = getFromPredicate(content, predicates)
      result.getOrElse(defaultTheme)
    }

    def display: Display = {

      val defaultDisplay = StandardDisplay

      // We separate this out from the previous isImmersive to prevent breaking the legacy designType when adding
      // the logic currently handled on Frontend. isGallery relies on Frontend metadata and so won't be added here
      // https://github.com/guardian/frontend/blob/e71dc1c521672b28399811c59331e0c2c713bf00/common/app/model/content.scala#L86
      val isImmersiveDisplay: ContentFilter = content =>
        isImmersive(content) ||
          isPhotoEssay(content)

      def hasShowcaseImage: ContentFilter = content => {
        val hasShowcaseImage = for {
          blocks <- content.blocks
          main <- blocks.main
          mainMedia <- main.elements.headOption
          imageTypeData <- mainMedia.imageTypeData
          imageRole <- imageTypeData.role
        } yield {
          imageRole == "showcase"
        }
        hasShowcaseImage.getOrElse(false)
      }

      def hasShowcaseEmbed: ContentFilter = content => {

        def isMainEmbed(elem: Element): Boolean = elem.relation == "main" && elem.`type` == ElementType.Embed

        def hasShowcaseAsset(assets: scala.collection.Seq[Asset]): Boolean = {
          val isShowcaseAsset = for {
            embedAsset <- assets.find(asset => asset.`type` == AssetType.Embed)
            typeData <- embedAsset.typeData
            role <- typeData.role
          } yield {
            role == "showcase"
          }
          isShowcaseAsset.getOrElse(false)
        }

        val hasShowcaseEmbed = for {
          elements <- content.elements
          mainEmbed <- elements.find(isMainEmbed)
        } yield {
          hasShowcaseAsset(mainEmbed.assets)
        }

        hasShowcaseEmbed.getOrElse(false)
      }

      val isShowcase: ContentFilter = content => displayHintExistsWithName("column")(content) ||
        displayHintExistsWithName("showcase")(content) ||
        hasShowcaseImage(content) ||
        hasShowcaseEmbed(content) ||
        isPictureContent(content)

      val isNumberedList: ContentFilter = displayHintExistsWithName("numberedList")

      val predicates: List[(ContentFilter, Display)] = List(
        isFullPageInteractive -> StandardDisplay,
        isImmersiveDisplay -> ImmersiveDisplay,
        isNumberedList -> NumberedListDisplay,
        isShowcase -> ShowcaseDisplay
      )

      val result = getFromPredicate(content, predicates)
      result.getOrElse(defaultDisplay)
    }
  }

}
