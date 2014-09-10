package com.gu.contentapi.client.parser

import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}
import com.gu.contentapi.client.ClientTest

class SearchJsonParserTest extends FlatSpec with Matchers with ClientTest {

  lazy val searchResponse = JsonParser.parseSearch(loadJson("search.json"))

  it should "parse basic response fields" in {
    searchResponse.status should be ("ok")
    searchResponse.userTier should be ("developer")
    searchResponse.total should be (1687267)
    searchResponse.startIndex should be (1)
    searchResponse.pageSize should be (10)
    searchResponse.currentPage should be (1)
    searchResponse.pages should be (168727)
    searchResponse.orderBy should be ("newest")
  }

  it should "parse the content" in {
    searchResponse.results.size should be (10)
    searchResponse.results.head.webTitle should be ("County cricket – live!")
    searchResponse.results.head.webPublicationDate should be (new DateTime(2014, 9, 10, 17, 10, 21, 0))
    searchResponse.results.head.sectionName should be (Some("Sport"))
    searchResponse.results.head.sectionId should be (Some("sport"))
    searchResponse.results.head.id should be ("sport/blog/live/2014/sep/10/county-cricket-live-blog-notts-yorkshire-surrey")
    searchResponse.results.head.webUrl should be ("http://www.theguardian.com/sport/blog/live/2014/sep/10/county-cricket-live-blog-notts-yorkshire-surrey")
    searchResponse.results.head.apiUrl should be ("http://content.guardianapis.com/sport/blog/live/2014/sep/10/county-cricket-live-blog-notts-yorkshire-surrey")
  }

}
