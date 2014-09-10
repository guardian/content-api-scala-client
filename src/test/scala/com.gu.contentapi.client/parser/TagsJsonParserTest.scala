package com.gu.contentapi.client.parser

import org.scalatest.{FlatSpec, Matchers}
import com.gu.contentapi.client.ClientTest

class TagsJsonParserTest extends FlatSpec with Matchers with ClientTest {

  val tagsResponse = JsonParser.parseTags(loadJson("tags.json"))

  it should "parse basic response fields" in {
    tagsResponse.status should be("ok")
    tagsResponse.userTier should be("developer")
    tagsResponse.total should be(43880)
    tagsResponse.startIndex should be(1)
    tagsResponse.pageSize should be(10)
    tagsResponse.currentPage should be(1)
    tagsResponse.pages should be(4388)
  }

  it should "parse the tags" in {
    tagsResponse.results.size should be(10)
    tagsResponse.results(1).webTitle should be("Abu Dhabi")
  }

}
