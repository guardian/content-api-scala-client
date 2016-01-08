package com.gu.contentapi.client.parser

import com.gu.contentapi.client.ClientTest
import org.scalatest.{FlatSpec, Matchers}

class JsonParserRemovedContentTest extends FlatSpec with Matchers with ClientTest {

  val removedResponse = JsonParser.parseRemovedContent(loadJson("removed.json"))

  "JsonParser.parseRemovedContent" should "parse basic response fields" in {
    removedResponse.status should be ("ok")
    removedResponse.userTier should be ("developer")
    removedResponse.total should be (18198)
    removedResponse.startIndex should be (1)
    removedResponse.pageSize should be (10)
    removedResponse.currentPage should be (1)
    removedResponse.pages should be (1820)
    removedResponse.orderBy should be ("newest")
  }

  it should "parse the list of results" in {
    removedResponse.results.size should be (10)
    removedResponse.results.head should be ("lifeandstyle/interactive/2014/mar/30/live-better")
  }

}
