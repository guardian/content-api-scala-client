package com.gu.contentapi.client.utils

import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.utils.format._

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object CapiModelEnrichment {

  type ContentFilter = Content => Boolean

  def getFromPredicate[T](content: Content, predicates: List[(ContentFilter, T)]): Option[T] =
    predicates.collectFirst { case (predicate, t) if predicate(content) => t }

  def tagExistsWithId(tagId: String): ContentFilter = content => content.tags.exists(tag => tag.id == tagId)

  def displayHintExistsWithName(displayHintName: String): ContentFilter = content => content.fields.flatMap(_.displayHint).contains(displayHintName)

  def isLiveBloggingNow: ContentFilter = content => content.fields.flatMap(_.liveBloggingNow).contains(true)

  val isImmersive: ContentFilter = content => displayHintExistsWithName("immersive")(content)

  val isMedia: ContentFilter = content => tagExistsWithId("type/audio")(content) || tagExistsWithId("type/video")(content) || tagExistsWithId("type/gallery")(content)

  val isReview: ContentFilter = content => tagExistsWithId("tone/reviews")(content) || tagExistsWithId("tone/livereview")(content) || tagExistsWithId("tone/albumreview")(content)

  val isCommentDesign: ContentFilter = content => tagExistsWithId("tone/comment")(content) || tagExistsWithId("tone/letters")(content)

  val isLiveBlog: ContentFilter = content => isLiveBloggingNow(content) && tagExistsWithId("tone/minutebyminute")(content)

  val isDeadBlog: ContentFilter = content => !isLiveBloggingNow(content) && tagExistsWithId("tone/minutebyminute")(content)

  implicit class RichCapiDateTime(val cdt: CapiDateTime) extends AnyVal {
    def toOffsetDateTime: OffsetDateTime = OffsetDateTime.parse(cdt.iso8601)
  }

  implicit class RichOffsetDateTime(val dt: OffsetDateTime) extends AnyVal {
    def toCapiDateTime: CapiDateTime = CapiDateTime.apply(dt.toInstant.toEpochMilli, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dt))
  }

  implicit class RichContent(val content: Content) extends AnyVal {

    def designType: DesignType = {

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
        isCommentDesign -> Comment,
        tagExistsWithId("tone/features") -> Feature,
        isLiveBlog -> Live,
        isDeadBlog -> Article
      )

      val result = getFromPredicate(content, predicates)
      result.getOrElse(defaultDesignType)
    }
  }

  implicit class RenderingFormat(val content: Content) extends AnyVal {


    def design: Design = {

      val defaultDesign: Design = ArticleDesign

      val isPhotoEssay: ContentFilter = content => content.fields.flatMap(_.displayHint).contains("photoessay")

      val predicates: List[(ContentFilter, Design)] = List(
        tagExistsWithId("artanddesign/series/guardian-print-shop") -> PrintShopDesign,
        isMedia -> MediaDesign,
        isReview -> ReviewDesign,
        tagExistsWithId("tone/analysis") -> AnalysisDesign,
        isCommentDesign -> CommentDesign,
        tagExistsWithId("tone/features") -> FeatureDesign,
        tagExistsWithId("tone/recipes") -> RecipeDesign,
        tagExistsWithId("tone/matchreports") -> MatchReportDesign,
        tagExistsWithId("tone/interview") -> InterviewDesign,
        tagExistsWithId("tone/editorials") -> EditorialDesign,
        tagExistsWithId("tone/quizzes") -> QuizDesign,
        isPhotoEssay -> PhotoEssayDesign,
        isLiveBlog -> LiveBlogDesign,
        isDeadBlog -> DeadBlogDesign
      )

      val result = getFromPredicate(content, predicates)
      result.getOrElse(defaultDesign)
    }

    def theme: Theme = {
      val defaultTheme: Theme = NewsPillar

      val specialReportTags: Set[String] = Set(
        "business/series/undercover-in-the-chicken-industry",
        "business/series/britains-debt-timebomb",
        "world/series/this-is-europe",
        "environment/series/the-polluters",
        "news/series/hsbc-files",
        "news/series/panama-papers",
        "us-news/homan-square",
        "uk-news/series/the-new-world-of-work",
        "world/series/the-new-arrivals",
        "news/series/nauru-files",
        "us-news/series/counted-us-police-killings",
        "australia-news/series/healthcare-in-detention",
        "society/series/this-is-the-nhs"
      )

      def isPillar(pillar: String): ContentFilter = content => content.pillarName.contains(pillar)

      val isSpecialReport: ContentFilter = content => content.tags.exists(t => specialReportTags(t.id))
      val isOpinion: ContentFilter = content => (isCommentDesign(content) && isPillar("News")(content)) ||
        isPillar("Opinion")(content)
      val isCulture: ContentFilter = content => isPillar("Arts")(content) || isPillar("Books")(content)

      val predicates: List[(ContentFilter, Theme)] = List(
        isOpinion -> OpinionPillar,
        isPillar("Sport") -> SportPillar,
        isCulture -> CulturePillar,
        isPillar("Lifestyle") -> LifestylePillar,
        isSpecialReport -> SpecialReportTheme,
        tagExistsWithId("tone/advertisement-features") -> Labs,
      )

      val result = getFromPredicate(content, predicates)
      result.getOrElse(defaultTheme)
    }

    def display: Display = {

      val defaultDisplay = StandardDisplay

      def hasShowcaseImage: ContentFilter = content => {
        val hasShowcaseImage = for {
          blocks <- content.blocks
          main <- blocks.main
          mainMedia = main.elements.head
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
        hasShowcaseEmbed(content)

      val isNumberedList: ContentFilter = displayHintExistsWithName("numberedList")

      val predicates: List[(ContentFilter, Display)] = List(
        isImmersive -> ImmersiveDisplay,
        isShowcase -> ShowcaseDisplay,
        isNumberedList -> NumberedListDisplay
      )

      val result = getFromPredicate(content, predicates)
      result.getOrElse(defaultDisplay)
    }
  }

}
