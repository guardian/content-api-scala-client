package com.gu.contentapi.client.model

import org.scalatest.{Matchers, FlatSpec}

class ContentApiQueryTest extends FlatSpec with Matchers  {
  "ItemQuery.toString" should "be excellent" in {
    ItemQuery("profile/robert-berry").showFields("all").toString shouldEqual
      "ItemQuery(/profile/robert-berry?show-fields=all)"
  }

  "SearchQuery.toString" should "also be excellent" in {
    SearchQuery().tag("profile/robert-berry").showElements("all").contentType("article").toString shouldEqual
      "SearchQuery(/search?tag=profile%2Frobert-berry&show-elements=all&type=article)"
  }

  "SectionsQuery.toString" should "be beautiful" in {
    SectionsQuery().toString shouldEqual "SectionsQuery(/sections)"
  }

  "SectionsQuery.toString" should "add sponsorship-type filter" in {
    SectionsQuery().sponsorshipType("paid-content").toString shouldEqual "SectionsQuery(/sections?sponsorship-type=paid-content)"
  }

  "TagsQuery.toString" should "be awesome" in {
    TagsQuery().tagType("contributor").toString shouldEqual "TagsQuery(/tags?type=contributor)"
  }

  "RemovedContentQuery.toString" should "be amazing" in {
    RemovedContentQuery().reason("gone").toString shouldEqual "RemovedContentQuery(/content/removed?reason=gone)"
  }
}
