package com.gu.facia.api.utils

import com.gu.contentapi.client.model.v1._
import com.gu.facia.api.models._
import lib.TestContent
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class FaciaContentUtilsTest extends AnyFreeSpec with Matchers with TestContent {

  val emptyProperties =
    ContentProperties.fromResolvedMetaData(ResolvedMetaData.Default)

  def makeLinkSnap(linkSnapId: String) = LinkSnap(
    id = linkSnapId,
    maybeFrontPublicationDate = None,
    snapType = "",
    snapUri = None,
    snapCss = None,
    atomId = None,
    headline = None,
    href = None,
    trailText = None,
    group = "",
    image = None,
    ContentProperties.fromResolvedMetaData(ResolvedMetaData.Default),
    byline = None,
    kicker = None,
    brandingByEdition = Map.empty,
    mediaAtom = None
  )

  val staticDateTime =
    new DateTime().withYear(2015).withMonthOfYear(4).withDayOfMonth(22)

  val content = baseContent.copy(webPublicationDate =
    Option(
      CapiDateTime(
        staticDateTime.getMillis,
        staticDateTime.toString(ISODateTimeFormat.dateTime())
      )
    )
  )

  def makeLatestSnap(latestSnapId: String, content: Content = content) =
    LatestSnap(
      id = latestSnapId,
      maybeFrontPublicationDate = None,
      cardStyle = DefaultCardstyle,
      format = ContentFormat(content),
      snapUri = None,
      snapCss = None,
      latestContent = Option(content),
      headline = None,
      href = None,
      trailText = None,
      group = "",
      image = None,
      properties = emptyProperties,
      byline = None,
      kicker = None,
      brandingByEdition = Map.empty,
      atomId = None,
      mediaAtom = None
    )

  def makeCuratedContent(curatedContentId: String, content: Content = content) =
    CuratedContent(
      content = content,
      maybeFrontPublicationDate = None,
      supportingContent = Nil,
      cardStyle = DefaultCardstyle,
      format = ContentFormat(content),
      headline = "",
      href = None,
      trailText = None,
      group = "",
      image = None,
      properties = emptyProperties,
      byline = None,
      kicker = None,
      embedType = None,
      embedUri = None,
      embedCss = None,
      brandingByEdition = Map.empty,
      atomId = None,
      mediaAtom = None,
      multimediaSlideshowAtomId = None,
      multimediaSlideshow = None,
      tests = None
    )

  def makeSupportingCuratedContent(
      curatedContentId: String,
      content: Content = content
  ) = SupportingCuratedContent(
    content = content,
    maybeFrontPublicationDate = None,
    cardStyle = DefaultCardstyle,
    format = ContentFormat(content),
    headline = "",
    href = None,
    trailText = None,
    group = "",
    image = None,
    properties = emptyProperties,
    byline = None,
    kicker = None,
    atomId = None,
    mediaAtom = None,
    tests = None
  )

  "webPublicationDateOption" - {
    "should return a None for a LinkSnap" in {
      val linkSnap = makeLinkSnap("some-link-snap")
      FaciaContentUtils.webPublicationDateOption(linkSnap) should be(None)
    }

    "should return DateTime on inner content for LatestSnap" in {
      val latestSnap = makeLatestSnap("some-latest-snap")
      FaciaContentUtils.webPublicationDateOption(latestSnap) should be(
        Some(staticDateTime)
      )
    }

    "should return DateTime on inner content for CuratedContent" in {
      val curatedContent = makeCuratedContent("some-curated-content")
      FaciaContentUtils.webPublicationDateOption(curatedContent) should be(
        Some(staticDateTime)
      )
    }

    "should return DateTime on inner content for SupportingCuratedContent" in {
      val supportingCuratedContent =
        makeSupportingCuratedContent("some-supporting-curated-content")
      FaciaContentUtils.webPublicationDateOption(
        supportingCuratedContent
      ) should be(Some(staticDateTime))
    }
  }

  "webPublicationDate" - {
    val contentWithNoDatetime =
      baseContent.copy(
        webPublicationDate = None,
        fields = Some(
          ContentFields(
            headline = Some("Content headline"),
            trailText = Some("Content trailtext"),
            byline = Some("Content byline")
          )
        )
      )

    val now = DateTime.now()

    "should return a NOW for a LinkSnap" in {
      val linkSnap = makeLinkSnap("some-link-snap")
      val dateTime = FaciaContentUtils.webPublicationDate(linkSnap)

      dateTime.getDayOfMonth should be(now.getDayOfMonth)
      dateTime.getMonthOfYear should be(now.getMonthOfYear)
      dateTime.getYear should be(now.getYear)
    }

    "should throw exception for DateTime on inner content with no DateTime for LatestSnap" in {
      val latestSnap = makeLatestSnap("some-latest-snap", contentWithNoDatetime)
      val dateTime = FaciaContentUtils.webPublicationDate(latestSnap)

      dateTime.getDayOfMonth should be(now.getDayOfMonth)
      dateTime.getMonthOfYear should be(now.getMonthOfYear)
      dateTime.getYear should be(now.getYear)
    }

    "should throw exception for DateTime on inner content with no DateTime for CuratedContent" in {
      val curatedContent =
        makeCuratedContent("some-curated-content", contentWithNoDatetime)
      val dateTime = FaciaContentUtils.webPublicationDate(curatedContent)

      dateTime.getDayOfMonth should be(now.getDayOfMonth)
      dateTime.getMonthOfYear should be(now.getMonthOfYear)
      dateTime.getYear should be(now.getYear)
    }

    "should throw exception for DateTime on inner content with no DateTime for SupportingCuratedContent" in {
      val supportingCuratedContent = makeSupportingCuratedContent(
        "some-supporting-curated-content",
        contentWithNoDatetime
      )
      val dateTime =
        FaciaContentUtils.webPublicationDate(supportingCuratedContent)

      dateTime.getDayOfMonth should be(now.getDayOfMonth)
      dateTime.getMonthOfYear should be(now.getMonthOfYear)
      dateTime.getYear should be(now.getYear)
    }
  }

  "mediaType" - {
    def contentWithTagIds(tagIds: String*) =
      baseContent.copy(
        webPublicationDate = None,
        fields = Some(
          ContentFields(
            headline = Some("Content headline"),
            trailText = Some("Content trailtext"),
            byline = Some("Content byline")
          )
        ),
        tags = tagIds.toList.map(tagId =>
          Tag(tagId, TagType.Type, None, None, "", "", "")
        )
      )

    "should return a None for a LinkSnap" in {
      val linkSnap = makeLinkSnap("some-link-snap")
      FaciaContentUtils.mediaType(linkSnap) should be(None)
    }

    "should return a Gallery for a Content with type/gallery in LatestSnap" in {
      val latestSnap =
        makeLatestSnap("some-latest-snap", contentWithTagIds("type/gallery"))
      FaciaContentUtils.mediaType(latestSnap) should be(Some(Gallery))
    }

    "should return a Audio for a Content with type/audio in LatestSnap" in {
      val latestSnap =
        makeLatestSnap("some-latest-snap", contentWithTagIds("type/audio"))
      FaciaContentUtils.mediaType(latestSnap) should be(Some(Audio))
    }

    "should return a Video for a Content with type/video in LatestSnap" in {
      val latestSnap =
        makeLatestSnap("some-latest-snap", contentWithTagIds("type/video"))
      FaciaContentUtils.mediaType(latestSnap) should be(Some(Video))
    }

    "should return a Gallery for a Content with type/gallery in CuratedContent" in {
      val cuaratedContent = makeLatestSnap(
        "some-curated-content",
        contentWithTagIds("type/gallery")
      )
      FaciaContentUtils.mediaType(cuaratedContent) should be(Some(Gallery))
    }

    "should return a Audio for a Content with type/audio in CuratedContent" in {
      val cuaratedContent =
        makeLatestSnap("some-curated-content", contentWithTagIds("type/audio"))
      FaciaContentUtils.mediaType(cuaratedContent) should be(Some(Audio))
    }

    "should return a Video for a Content with type/video in CuratedContent" in {
      val cuaratedContent =
        makeLatestSnap("some-curated-content", contentWithTagIds("type/video"))
      FaciaContentUtils.mediaType(cuaratedContent) should be(Some(Video))
    }

    "should return a Gallery for a Content with type/gallery in SupportingCuratedContent" in {
      val supportingCuratedContent = makeLatestSnap(
        "some-supporting-curated-content",
        contentWithTagIds("type/gallery")
      )
      FaciaContentUtils.mediaType(supportingCuratedContent) should be(
        Some(Gallery)
      )
    }

    "should return a Audio for a Content with type/audio in SupportingCuratedContent" in {
      val supportingCuratedContent = makeLatestSnap(
        "some-supporting-curated-content",
        contentWithTagIds("type/audio")
      )
      FaciaContentUtils.mediaType(supportingCuratedContent) should be(
        Some(Audio)
      )
    }

    "should return a Video for a Content with type/video in SupportingCuratedContent" in {
      val supportingCuratedContent = makeLatestSnap(
        "some-supporting-curated-content",
        contentWithTagIds("type/video")
      )
      FaciaContentUtils.mediaType(supportingCuratedContent) should be(
        Some(Video)
      )
    }
  }

  "isLive" - {
    def contentWithFields(fields: ContentFields) =
      baseContent.copy(fields = Some(fields))

    "should return a false for a LinkSnap" in {
      val linkSnap = makeLinkSnap("some-link-snap")
      FaciaContentUtils.isLive(linkSnap) should be(false)
    }

    "should return" - {
      "true on inner content with liveBloggingNow true for LatestSnap" in {
        val latestSnap = makeLatestSnap(
          "some-latest-snap",
          contentWithFields(ContentFields(liveBloggingNow = Some(true)))
        )
        FaciaContentUtils.isLive(latestSnap) should be(true)
      }

      "false on inner content with with liveBloggingNow false for LatestSnap" in {
        val latestSnap2 = makeLatestSnap(
          "some-latest-snap",
          contentWithFields(ContentFields(liveBloggingNow = Some(false)))
        )
        FaciaContentUtils.isLive(latestSnap2) should be(false)
      }

      "false on inner content with no fields for LatestSnap" in {
        val latestSnap3 =
          makeLatestSnap("some-latest-snap", contentWithFields(ContentFields()))
        FaciaContentUtils.isLive(latestSnap3) should be(false)
      }
    }

    "should return" - {
      "true on inner content with liveBloggingNow true for CuratedContent" in {
        val curatedContent = makeCuratedContent(
          "some-curated-content",
          contentWithFields(ContentFields(liveBloggingNow = Some(true)))
        )
        FaciaContentUtils.isLive(curatedContent) should be(true)
      }

      "false on inner content with liveBloggingNow false for CuratedContent" in {
        val curatedContent2 = makeCuratedContent(
          "some-curated-content",
          contentWithFields(ContentFields(liveBloggingNow = Some(false)))
        )
        FaciaContentUtils.isLive(curatedContent2) should be(false)
      }

      "false on inner content with no fields for CuratedContent" in {
        val curatedContent3 = makeCuratedContent(
          "some-curated-content",
          contentWithFields(ContentFields())
        )
        FaciaContentUtils.isLive(curatedContent3) should be(false)
      }
    }

    "should return true and false on inner content for SupportingCuratedContent" - {

      "true on inner content with liveBloggingNow true for SupportingCuratedContent" in {
        val supportingCuratedContent = makeSupportingCuratedContent(
          "some-supporting-curated-content",
          contentWithFields(ContentFields(liveBloggingNow = Some(true)))
        )
        FaciaContentUtils.isLive(supportingCuratedContent) should be(true)
      }

      "false on inner content with liveBloggingNow false for SupportingCuratedContent" in {
        val supportingCuratedContent = makeSupportingCuratedContent(
          "some-supporting-curated-content",
          contentWithFields(ContentFields(liveBloggingNow = Some(false)))
        )
        FaciaContentUtils.isLive(supportingCuratedContent) should be(false)
      }

      "false on inner content with no fields for SupportingCuratedContent" in {
        val supportingCuratedContent = makeSupportingCuratedContent(
          "some-supporting-curated-content",
          contentWithFields(ContentFields())
        )
        FaciaContentUtils.isLive(supportingCuratedContent) should be(false)
      }
    }
  }

  "isCommentable" - {
    def contentWithFields(fields: ContentFields) =
      baseContent.copy(fields = Some(fields))

    "should return a false for a LinkSnap" in {
      val linkSnap = makeLinkSnap("some-link-snap")
      FaciaContentUtils.isCommentable(linkSnap) should be(false)
    }

    "should return" - {
      "true on inner content with commentable true for LatestSnap" in {
        val latestSnap = makeLatestSnap(
          "some-latest-snap",
          contentWithFields(ContentFields(commentable = Some(true)))
        )
        FaciaContentUtils.isCommentable(latestSnap) should be(true)
      }

      "false on inner content with commentable false for LatestSnap" in {
        val latestSnap = makeLatestSnap(
          "some-latest-snap",
          contentWithFields(ContentFields(commentable = Some(false)))
        )
        FaciaContentUtils.isCommentable(latestSnap) should be(false)
      }

      "true on inner content with no fields for LatestSnap" in {
        val latestSnap =
          makeLatestSnap("some-latest-snap", contentWithFields(ContentFields()))
        FaciaContentUtils.isCommentable(latestSnap) should be(false)
      }
    }

    "should return" - {
      "true on inner content with commentable true for CuratedContent" in {
        val curatedContent = makeCuratedContent(
          "some-curated-content",
          contentWithFields(ContentFields(commentable = Some(true)))
        )
        FaciaContentUtils.isCommentable(curatedContent) should be(true)
      }

      "false on inner content with commentable false for CuratedContent" in {
        val curatedContent = makeCuratedContent(
          "some-curated-content",
          contentWithFields(ContentFields(commentable = Some(false)))
        )
        FaciaContentUtils.isCommentable(curatedContent) should be(false)
      }

      "false on inner content with no fields for CuratedContent" in {
        val curatedContent = makeCuratedContent(
          "some-curated-content",
          contentWithFields(ContentFields())
        )
        FaciaContentUtils.isCommentable(curatedContent) should be(false)
      }
    }

    "should return" - {
      "true on inner content with commentable true for SupportingCuratedContent" in {
        val supportingCuratedContent = makeSupportingCuratedContent(
          "some-supporting-curated-content",
          contentWithFields(ContentFields(commentable = Some(true)))
        )
        FaciaContentUtils.isCommentable(supportingCuratedContent) should be(
          true
        )
      }

      "false on inner content with commentable false for SupportingCuratedContent" in {
        val supportingCuratedContent2 = makeSupportingCuratedContent(
          "some-supporting-curated-content",
          contentWithFields(ContentFields(commentable = Some(false)))
        )
        FaciaContentUtils.isCommentable(supportingCuratedContent2) should be(
          false
        )
      }

      "false on inner content with no fields for SupportingCuratedContent" in {
        val supportingCuratedContent3 = makeSupportingCuratedContent(
          "some-supporting-curated-content",
          contentWithFields(ContentFields())
        )
        FaciaContentUtils.isCommentable(supportingCuratedContent3) should be(
          false
        )
      }
    }
  }

  "shortUrl (theguardian.com)" - {
    def contentWithShortUrl(shortUrl: String) =
      baseContent.copy(fields = Some(ContentFields(shortUrl = Some(shortUrl))))

    val contentWithNoShortUrl = content

    val latestSnapWithShortUrl = makeLatestSnap(
      "some-latest-snap",
      contentWithShortUrl("http://www.theguardian.com/p/4vq42")
    )
    val latestSnapWithNoShortUrl =
      makeLatestSnap("some-latest-snap", contentWithNoShortUrl)
    val curatedContentWithShortUrl = makeCuratedContent(
      "some-latest-snap",
      contentWithShortUrl("http://www.theguardian.com/p/4vq42")
    )
    val curatedContentWithNoShortUrl =
      makeLatestSnap("some-latest-snap", contentWithNoShortUrl)
    val supportingCuratedContentWithShortUrl = makeSupportingCuratedContent(
      "some-latest-snap",
      contentWithShortUrl("http://www.theguardian.com/p/4vq42")
    )
    val supportingCuratedContentWithNoShortUrl =
      makeLatestSnap("some-latest-snap", contentWithNoShortUrl)

    "should return a false for a LinkSnap" in {
      val linkSnap = makeLinkSnap("some-link-snap")
      FaciaContentUtils.isCommentable(linkSnap) should be(false)
    }

    "maybeShortUrl should" - {
      "return Some for LatestSnap with inner content with shortUrl" in {
        FaciaContentUtils.maybeShortUrl(latestSnapWithShortUrl) should be(
          Some("http://www.theguardian.com/p/4vq42")
        )
      }

      "return None for LatestSnap with inner content with no shortUrl" in {
        FaciaContentUtils.maybeShortUrl(latestSnapWithNoShortUrl) should be(
          None
        )
      }

      "return Some for CuratedContent with inner content with shortUrl" in {
        FaciaContentUtils.maybeShortUrl(curatedContentWithShortUrl) should be(
          Some("http://www.theguardian.com/p/4vq42")
        )
      }

      "return None for CuratedContent with inner content with no shortUrl" in {
        FaciaContentUtils.maybeShortUrl(curatedContentWithNoShortUrl) should be(
          None
        )
      }

      "return Some for SupportingCuratedContent with inner content with shortUrl" in {
        FaciaContentUtils.maybeShortUrl(
          supportingCuratedContentWithShortUrl
        ) should be(Some("http://www.theguardian.com/p/4vq42"))
      }

      "return None for SupportingCuratedContent with inner content with no shortUrl" in {
        FaciaContentUtils.maybeShortUrl(
          supportingCuratedContentWithNoShortUrl
        ) should be(None)
      }
    }

    "shortUrl should" - {
      "return Some for LatestSnap with inner content with shortUrl" in {
        FaciaContentUtils.shortUrl(latestSnapWithShortUrl) should be(
          "http://www.theguardian.com/p/4vq42"
        )
      }

      "return None for LatestSnap with inner content with no shortUrl" in {
        FaciaContentUtils.shortUrl(latestSnapWithNoShortUrl) should be("")
      }

      "return Some for CuratedContent with inner content with shortUrl" in {
        FaciaContentUtils.shortUrl(curatedContentWithShortUrl) should be(
          "http://www.theguardian.com/p/4vq42"
        )
      }

      "return None for CuratedContent with inner content with no shortUrl" in {
        FaciaContentUtils.shortUrl(curatedContentWithNoShortUrl) should be("")
      }

      "return Some for SupportingCuratedContent with inner content with shortUrl" in {
        FaciaContentUtils.shortUrl(
          supportingCuratedContentWithShortUrl
        ) should be("http://www.theguardian.com/p/4vq42")
      }

      "return None for SupportingCuratedContent with inner content with no shortUrl" in {
        FaciaContentUtils.shortUrl(
          supportingCuratedContentWithNoShortUrl
        ) should be("")
      }
    }

    "shortUrlPath should" - {
      "return Some for LatestSnap with inner content with shortUrl" in {
        FaciaContentUtils.shortUrlPath(latestSnapWithShortUrl) should be(
          Some("/p/4vq42")
        )
      }

      "return None for LatestSnap with inner content with no shortUrl" in {
        FaciaContentUtils.shortUrlPath(latestSnapWithNoShortUrl) should be(None)
      }

      "return Some for CuratedContent with inner content with shortUrl" in {
        FaciaContentUtils.shortUrlPath(curatedContentWithShortUrl) should be(
          Some("/p/4vq42")
        )
      }

      "return None for CuratedContent with inner content with no shortUrl" in {
        FaciaContentUtils.shortUrlPath(curatedContentWithNoShortUrl) should be(
          None
        )
      }

      "return Some for SupportingCuratedContent with inner content with shortUrl" in {
        FaciaContentUtils.shortUrlPath(
          supportingCuratedContentWithShortUrl
        ) should be(Some("/p/4vq42"))
      }

      "return None for SupportingCuratedContent with inner content with no shortUrl" in {
        FaciaContentUtils.shortUrlPath(
          supportingCuratedContentWithNoShortUrl
        ) should be(None)
      }
    }
  }
}
