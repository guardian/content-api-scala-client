package com.gu.contentapi.client.parser

import org.scalatest.{FlatSpec, Matchers}
import com.gu.contentapi.client.ClientTest

class JsonParserSectionsTest extends FlatSpec with Matchers with ClientTest {

  val sectionsResponse = JsonParser.parseSections(loadJson("sections.json"))

  "sections parser" should "parse basic response fields" in {
    sectionsResponse.status should be ("ok")
    sectionsResponse.userTier should be ("developer")
    sectionsResponse.total should be (65)
  }

  it should "parse the sections" in {
    sectionsResponse.results.size should be (65)

    val section = sectionsResponse.results.head
    section.id should be ("community")
    section.webTitle should be ("Community")
    section.webUrl should be ("http://www.theguardian.com/community")
    section.apiUrl should be ("http://content.guardianapis.com/community")
  }

}
