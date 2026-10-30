package com.gu.facia.api

import com.gu.contentapi.client.model.v1.{Tag, TagType}
import com.gu.facia.api.models.PublicationStatus.{Draft, Live}
import com.gu.facia.client.models.CustomSubnavFormat.Large
import com.gu.facia.client.models.TargetedPageType.{Article, Front, HasTag}
import com.gu.facia.client.models._
import lib.TestContent
import org.joda.time.DateTime
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class CustomSubnavServiceTest extends AnyFreeSpec with Matchers with TestContent {
  private val updatedAt = new DateTime(0)

  private def subnav(id: String, pages: List[TargetedPage]): CustomSubnav =
    CustomSubnav(
      id = id,
      header = CustomSubnavHeader("Header", None, "Copy"),
      format = Large,
      links = Nil,
      pages = pages,
      images = None,
      palette = None,
      lastUpdated = updatedAt,
      updatedBy = "test",
      updatedEmail = "test@example.com"
    )

  "getSubnavForFront" - {
    "returns matching live subnav for a front id" in {
      val liveMatch = subnav("live", List(TargetedPage(Front, "uk")))
      val config = CustomSubnavConfig(live = List(liveMatch), draft = Nil)

      CustomSubnavService.getSubnavForFront(config, "uk", Live) shouldBe Some(
        liveMatch
      )
    }

    "returns matching draft subnav when status is Draft" in {
      val draftMatch = subnav("draft", List(TargetedPage(Front, "culture")))
      val config = CustomSubnavConfig(live = Nil, draft = List(draftMatch))

      CustomSubnavService.getSubnavForFront(
        config,
        "culture",
        Draft
      ) shouldBe Some(draftMatch)
      CustomSubnavService.getSubnavForFront(
        config,
        "culture",
        Live
      ) shouldBe None
    }

    "prefers draft match over live in Draft mode, but not in Live mode" in {
      val liveMatch = subnav("live-front", List(TargetedPage(Front, "sport")))
      val draftMatch = subnav("draft-front", List(TargetedPage(Front, "sport")))
      val config =
        CustomSubnavConfig(live = List(liveMatch), draft = List(draftMatch))

      CustomSubnavService.getSubnavForFront(
        config,
        "sport",
        Draft
      ) shouldBe Some(draftMatch)
      CustomSubnavService.getSubnavForFront(
        config,
        "sport",
        Live
      ) shouldBe Some(liveMatch)
    }
  }

  "getSubnavForContent" - {
    "returns matching live subnav for a specific article id" in {
      val content = baseContent.copy(id = "article/123")
      val liveMatch =
        subnav("by-article", List(TargetedPage(Article, "article/123")))
      val config = CustomSubnavConfig(live = List(liveMatch), draft = Nil)

      CustomSubnavService.getSubnavForContent(
        config,
        content,
        Live
      ) shouldBe Some(liveMatch)
    }

    "returns matching draft subnav for one of the article tags" in {
      val content = baseContent.copy(tags = List(
        Tag(
          "tone/comment",
          TagType.Keyword,
          webTitle = "",
          webUrl = "",
          apiUrl = ""
        )
      ))
      val draftMatch =
        subnav("by-tag", List(TargetedPage(HasTag, "tone/comment")))
      val config = CustomSubnavConfig(live = Nil, draft = List(draftMatch))

      CustomSubnavService.getSubnavForContent(
        config,
        content,
        Draft
      ) shouldBe Some(draftMatch)
      CustomSubnavService.getSubnavForContent(
        config,
        content,
        Live
      ) shouldBe None
    }

    "prefers draft match over live in Draft mode, but not in Live mode" in {
      val content = baseContent.copy(id = "article/456")
      val liveMatch =
        subnav("live-content", List(TargetedPage(Article, "article/456")))
      val draftMatch =
        subnav("draft-content", List(TargetedPage(Article, "article/456")))
      val config =
        CustomSubnavConfig(live = List(liveMatch), draft = List(draftMatch))

      CustomSubnavService.getSubnavForContent(
        config,
        content,
        Draft
      ) shouldBe Some(draftMatch)
      CustomSubnavService.getSubnavForContent(
        config,
        content,
        Live
      ) shouldBe Some(liveMatch)
    }
  }
}
