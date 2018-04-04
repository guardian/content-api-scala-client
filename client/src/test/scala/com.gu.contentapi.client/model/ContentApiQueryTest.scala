package com.gu.contentapi.client.model

import org.scalatest.{Matchers, FlatSpec}

class ContentApiQueryTest extends FlatSpec with Matchers  {
  "ItemQuery" should "be excellent" in {
    ItemQuery("profile/robert-berry").showFields("all")("") shouldEqual
      "/profile/robert-berry?show-fields=all"
  }

  "SearchQuery" should "also be excellent" in {
    SearchQuery().tag("profile/robert-berry").showElements("all").contentType("article")("") shouldEqual
      "/search?tag=profile%2Frobert-berry&show-elements=all&type=article"
  }

  "SectionsQuery" should "be beautiful" in {
    SectionsQuery()("") shouldEqual "/sections"
  }

  "SectionsQuery" should "add sponsorship-type filter" in {
    SectionsQuery().sponsorshipType("paid-content")("") shouldEqual "/sections?sponsorship-type=paid-content"
  }

  "TagsQuery" should "be awesome" in {
    TagsQuery().tagType("contributor")("") shouldEqual "/tags?type=contributor"
  }

  "RemovedContentQuery" should "be amazing" in {
    RemovedContentQuery().reason("gone")("") shouldEqual "/content/removed?reason=gone"
  }
}
