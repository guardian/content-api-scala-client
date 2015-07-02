package com.gu.contentapi.client.model

import org.scalatest.{Matchers, FlatSpec}

class ContentApiQueryTest extends FlatSpec with Matchers  {
  "ItemQuery.toString" should "be excellent" in {
    ItemQuery("profile/robert-berry").showFields("all").toString shouldEqual
      "ItemQuery(/profile/robert-berry?show-fields=all)"
  }

  "SearchQuery.toString" should "also be excellent" in {
    SearchQuery().tag("profile/robert-berry").showElements("all").toString shouldEqual
      "SearchQuery(/search?tag=profile%2Frobert-berry&show-elements=all)"
  }

  "SectionsQuery.toString" should "be beautiful" in {
    SectionsQuery().toString shouldEqual "SectionsQuery(/sections)"
  }

  "TagsQuery.toString" should "be awesome" in {
    TagsQuery().tagType("contributor").toString shouldEqual "TagsQuery(/tags?type=contributor)"
  }

  "RemovedContentQuery.toString" should "be amazing" in {
    RemovedContentQuery().reason("gone").toString shouldEqual "RemovedContentQuery(/content/removed?reason=gone)"
  }
}
