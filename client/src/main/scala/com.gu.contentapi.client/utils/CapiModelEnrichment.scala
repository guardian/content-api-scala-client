package com.gu.contentapi.client.utils

import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.utils.format._

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object CapiModelEnrichment {

  type ContentFilter = Content => Boolean

  def getFromPredicate[T](content: Content, predicates: List[(ContentFilter, T)]): Option[T] =
    predicates.collectFirst { case (predicate, t) if predicate(content) => t }

  def tagExistsWithId(tagId: String): ContentFilter = c => c.tags.exists(tag => tag.id == tagId)

  def isLiveBloggingNow: ContentFilter = c => c.fields.flatMap(_.liveBloggingNow).contains(true)

  val isImmersive: ContentFilter = c => c.fields.flatMap(_.displayHint).contains("immersive")

  val isMedia: ContentFilter = c => tagExistsWithId("type/audio")(c) || tagExistsWithId("type/video")(c) || tagExistsWithId("type/gallery")(c)

  val isReview: ContentFilter = c => tagExistsWithId("tone/reviews")(c) || tagExistsWithId("tone/livereview")(c) || tagExistsWithId("tone/albumreview")(c)

  val isComment: ContentFilter = c => tagExistsWithId("tone/comment")(c) || tagExistsWithId("tone/letters")(c)

  val isPhotoEssay: ContentFilter = c => c.fields.flatMap(_.displayHint).contains("photoessay")

  val isLiveBlog: ContentFilter = c => isLiveBloggingNow(c) && tagExistsWithId("tone/minutebyminute")(c)

  val isDeadBlog: ContentFilter = c => !isLiveBloggingNow(c) && tagExistsWithId("tone/minutebyminute")(c)

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
        isComment -> Comment,
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

      val predicates: List[(ContentFilter, Design)] = List(
        tagExistsWithId("artanddesign/series/guardian-print-shop") -> PrintShopDesign,
        tagExistsWithId("tone/matchreports") -> MatchReportDesign,
        isMedia -> MediaDesign,
        isReview -> ReviewDesign,
        tagExistsWithId("tone/analysis") -> AnalysisDesign,
        isComment -> CommentDesign,
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

      def isPillar(pillar: String): ContentFilter = c => c.pillarName.contains(pillar)

      val isSpecialReport: ContentFilter = c => c.tags.exists(t => specialReportTags(t.id))
      val isOpinion: ContentFilter = c => isComment(c) && isPillar("News")(c) || isPillar("Opinion")(c)

      val predicates: List[(ContentFilter, Theme)] = List(
        isOpinion -> OpinionPillar,
        isPillar("Sport") -> SportPillar,
        isPillar("Culture") -> CulturePillar,
        isPillar("Lifestyle") -> LifestylePillar,
        isSpecialReport -> SpecialReport,
        tagExistsWithId("tone/advertisement-features") -> Labs,
      )

      val result = getFromPredicate(content, predicates)
      result.getOrElse(defaultTheme)
    }

    def display: Display = {

      val defaultDisplay = StandardDisplay

      // TODO: Handle Display.Showcase.
      //  Could be done outside client if appropriate.
      val predicates: List[(ContentFilter, Display)] = List(
        isImmersive -> ImmersiveDisplay,
      )

      val result = getFromPredicate(content, predicates)
      result.getOrElse(defaultDisplay)
    }

  }

}

}
