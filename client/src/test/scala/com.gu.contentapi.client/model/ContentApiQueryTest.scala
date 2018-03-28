package com.gu.contentapi.client.model

import org.scalatest.{Matchers, FlatSpec}

class ContentApiQueryTest extends FlatSpec with Matchers  {
  "ItemQuery.toString" should "be excellent" in {
    ItemQuery("profile/robert-berry").showFields("all").toString shouldEqual
      "/profile/robert-berry?show-fields=all"
  }

  "SearchQuery.toString" should "also be excellent" in {
    SearchQuery().tag("profile/robert-berry").showElements("all").contentType("article").toString shouldEqual
      "/search?tag=profile%2Frobert-berry&show-elements=all&type=article"
  }

  "SectionsQuery.toString" should "be beautiful" in {
    SectionsQuery().toString shouldEqual "/sections"
  }

  "SectionsQuery.toString" should "add sponsorship-type filter" in {
    SectionsQuery().sponsorshipType("paid-content").toString shouldEqual "/sections?sponsorship-type=paid-content"
  }

  "TagsQuery.toString" should "be awesome" in {
    TagsQuery().tagType("contributor").toString shouldEqual "/tags?type=contributor"
  }

  "RemovedContentQuery.toString" should "be amazing" in {
    RemovedContentQuery().reason("gone").toString shouldEqual "/content/removed?reason=gone"
  }
}
