package com.gu.contentapi.client.parser

import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}
import com.gu.contentapi.client.ClientTest

class JsonParserCollectionTest extends FlatSpec with Matchers with ClientTest {

  val collectionResponse = JsonParser.parseCollection(loadJson("a-collection.json"))

  "collection parser" should "parse basic response fields" in {
    collectionResponse.status should be ("ok")
    collectionResponse.userTier should be ("developer")
    collectionResponse.total should be (1)
    collectionResponse.pages should be (1)
    collectionResponse.startIndex should be (1)
    collectionResponse.pageSize should be (10)
    collectionResponse.currentPage should be (1)
  }

  it should "parse the collection item" in {
    collectionResponse.collection.id should be ("754c-8e8c-fad9-a927")
    collectionResponse.collection.`type` should be ("news/special")
    collectionResponse.collection.title should be(Some("sport"))
    collectionResponse.collection.groups.size should be(0)
    collectionResponse.collection.lastModified should be (new DateTime(2014, 9, 10, 16, 13, 37, 0))
    collectionResponse.collection.modifiedBy should be("somebody@theguardian.com")
    collectionResponse.collection.curatedContent.size should be (4)
    collectionResponse.collection.curatedContent.head.id should be ("football/2014/sep/10/premier-league-new-signings-tactical")
    collectionResponse.collection.curatedContent.head.sectionId should be (Some("football"))
    collectionResponse.collection.curatedContent.head.sectionName should be (Some("Football"))
    collectionResponse.collection.curatedContent.head.webPublicationDate should be (new DateTime(2014, 9, 10, 11, 1, 0, 0))
    collectionResponse.collection.curatedContent.head.webTitle should be ("Falcao, Welbeck, Ben Arfa: how will new boys fit in this weekend?")
    collectionResponse.collection.curatedContent.head.webUrl should be ("http://www.theguardian.com/football/2014/sep/10/premier-league-new-signings-tactical")
    collectionResponse.collection.curatedContent.head.apiUrl should be ("http://content.guardianapis.com/football/2014/sep/10/premier-league-new-signings-tactical")
    collectionResponse.collection.backfill should be(Some("search?tag=tone/reviews"))
  }

}
