package com.gu.contentapi.client.parser

import org.scalatest.{FlatSpec, Matchers}
import com.gu.contentapi.client.ClientTest

class JsonParserTagsTest extends FlatSpec with Matchers with ClientTest {

  val tagsResponse = JsonParser.parseTags(loadJson("tags.json"))

  "tags parser" should "parse basic response fields" in {
    tagsResponse.status should be ("ok")
    tagsResponse.userTier should be ("developer")
    tagsResponse.total should be (43880)
    tagsResponse.startIndex should be (1)
    tagsResponse.pageSize should be (10)
    tagsResponse.currentPage should be (1)
    tagsResponse.pages should be (4388)
  }

  it should "parse the tags" in {
    tagsResponse.results.size should be (10)
    tagsResponse.results.head.apiUrl should be ("http://content.guardianapis.com/abu-dhabi/abu-dhabi")
    tagsResponse.results.head.id should be ("abu-dhabi/abu-dhabi")
    tagsResponse.results.head.sectionId should be (Some("abu-dhabi"))
    tagsResponse.results.head.sectionName should be (Some("Abu Dhabi"))
    tagsResponse.results.head.`type` should be ("keyword")
    tagsResponse.results.head.webTitle should be ("Abu Dhabi")
    tagsResponse.results.head.webUrl should be ("http://www.theguardian.com/abu-dhabi/abu-dhabi")
  }

}
