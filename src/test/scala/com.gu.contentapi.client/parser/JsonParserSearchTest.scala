package com.gu.contentapi.client.parser

import com.gu.contentapi.client.model.v1.{CapiDateTime, ContentType}
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}
import com.gu.contentapi.client.ClientTest

class JsonParserSearchTest extends FlatSpec with Matchers with ClientTest {

  val searchResponse = JsonParser.parseSearch(loadJson("search.json"))

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
    searchResponse.results.head.`type` should be (ContentType.Article)
    searchResponse.results.head.webTitle should be ("County cricket – live!")

    val expectedWebPublicationDate = CapiDateTime(new DateTime(2014, 9, 10, 17, 10, 21, 0).getMillis)
    searchResponse.results.head.webPublicationDate.get should be (expectedWebPublicationDate)

    searchResponse.results.head.sectionName should be (Some("Sport"))
    searchResponse.results.head.sectionId should be (Some("sport"))
    searchResponse.results.head.id should be ("sport/blog/live/2014/sep/10/county-cricket-live-blog-notts-yorkshire-surrey")
    searchResponse.results.head.webUrl should be ("http://www.theguardian.com/sport/blog/live/2014/sep/10/county-cricket-live-blog-notts-yorkshire-surrey")
    searchResponse.results.head.apiUrl should be ("http://content.guardianapis.com/sport/blog/live/2014/sep/10/county-cricket-live-blog-notts-yorkshire-surrey")
  }

  it should "default type to 'article' if type field not present on content." in {
    searchResponse.results.head.`type` should be (ContentType.Article)
  }

  it should "parse the correct content type" in {
    searchResponse.results(1).`type` should be (ContentType.Video)
  }

}
