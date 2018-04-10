package com.gu.contentapi.client.utils

import com.gu.contentapi.client.model.v1._
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object CapiModelEnrichment {

  implicit class RichCapiDateTime(val cdt: CapiDateTime) extends AnyVal {
    def toOffsetDateTime: OffsetDateTime = OffsetDateTime.parse(cdt.iso8601)
  }

  implicit class RichOffsetDateTime(val dt: OffsetDateTime) extends AnyVal {
    def toCapiDateTime: CapiDateTime = CapiDateTime.apply(dt.toInstant.toEpochMilli, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dt))
  }

  implicit class RichContent(val content: Content) extends AnyVal {

    def designType: DesignType = {

      val defaultDesignType = Article

      type ContentFilter = Content => Boolean

      val isImmersive: ContentFilter = c => c.fields.flatMap(_.displayHint).contains("immersive")

      def tagExistsWithId(tagId: String): ContentFilter = c => c.tags.exists(tag => tag.id == tagId)

      val isMedia: ContentFilter = c => tagExistsWithId("type/audio")(c) || tagExistsWithId("type/video")(c) || tagExistsWithId("type/gallery")(c)

      val isReview: ContentFilter = c => tagExistsWithId("tone/reviews")(c) || tagExistsWithId("tone/livereview")(c) || tagExistsWithId("tone/albumreview")(c)

      def isComment: ContentFilter = c => tagExistsWithId("tone/comment")(c) || tagExistsWithId("tone/letters")(c)

      def liveBloggingNow: Boolean = content.fields.flatMap(_.liveBloggingNow).contains(true)

      val liveBlog: ContentFilter = liveBloggingNow && tagExistsWithId("tone/minutebyminute")(_)

      val deadBlog: ContentFilter = !liveBloggingNow && tagExistsWithId("tone/minutebyminute")(_)

      val predicates: List[(ContentFilter, DesignType)] = List (
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
        liveBlog -> Live,
        deadBlog -> Article
      )

      val result = predicates.collectFirst { case (predicate, design) if predicate(content) => design }
      result.getOrElse(defaultDesignType)
    }
  }
}
