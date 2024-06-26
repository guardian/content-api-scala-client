package com.gu.contentapi.client.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ContentApiQueryTest extends AnyFlatSpec with Matchers  {
  "ItemQuery" should "be excellent" in {
    ItemQuery("profile/robert-berry").showFields("all").getUrl("") shouldEqual
      "/profile/robert-berry?show-fields=all"
  }

  "ItemQuery" should "be similarly excellent when asked to show alias paths" in {
    ItemQuery("profile/justin-pinner").showAliasPaths(true).getUrl("") shouldEqual
      "/profile/justin-pinner?show-alias-paths=true"
  }

  "ItemQuery" should "accept a channel" in {
    ItemQuery("lifeandstyle/thing").withChannelId("recipes").getUrl("") shouldEqual
      "/channel/recipes/item/lifeandstyle/thing"
  }

  "ItemQuery" should "drop the included channel if asked" in {
    ItemQuery("lifeandstyle/thing").withChannelId("recipes").withoutChannelId().getUrl("") shouldEqual
      "/lifeandstyle/thing"
  }

  "SearchQuery" should "also be excellent" in {
    SearchQuery().tag("profile/robert-berry").showElements("all").contentType("article").queryFields("body").getUrl("") shouldEqual
      "/search?tag=profile%2Frobert-berry&show-elements=all&type=article&query-fields=body"
  }

  "SearchQuery" should "not be perturbed when asked to show alias paths" in {
    SearchQuery().tag("profile/justin-pinner").showElements("all").showAliasPaths(true).contentType("article").getUrl("") shouldEqual
      "/search?tag=profile%2Fjustin-pinner&show-elements=all&show-alias-paths=true&type=article"
  }

  "SearchQuery" should "accept paths as a parameter" in {
    SearchQuery().paths("path/one,path/two").getUrl("") shouldEqual
      "/search?paths=path%2Fone%2Cpath%2Ftwo"
  }

  "SearchQuery" should "include a channel if asked" in {
    SearchQuery().withChannel("my-channel").paths("path/one").getUrl("") shouldEqual
      "/channel/my-channel/search?paths=path%2Fone"
  }

  "SearchQuery" should "drop the included channel if asked" in {
    SearchQuery().withChannel("my-channel").withoutChannel().paths("path/one").getUrl("") shouldEqual
      "/search?paths=path%2Fone"
  }

  "SectionsQuery" should "be beautiful" in {
    SectionsQuery().getUrl("") shouldEqual "/sections"
  }

  "SectionsQuery" should "add sponsorship-type filter" in {
    SectionsQuery().sponsorshipType("paid-content").getUrl("") shouldEqual "/sections?sponsorship-type=paid-content"
  }

  "TagsQuery" should "be awesome" in {
    TagsQuery().tagType("contributor").getUrl("") shouldEqual "/tags?type=contributor"
  }

}
