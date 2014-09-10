package com.gu.contentapi.client.parser

import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}
import com.gu.contentapi.client.ClientTest

class CollectionJsonParserTest extends FlatSpec with Matchers with ClientTest {

  val collectionResponse = JsonParser.parseCollection(loadJson("a-collection.json"))

  it should "parse basic response fields" in {
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
    collectionResponse.collection.lastModified should be (new DateTime(2014, 9, 10, 17, 13, 37, 0))
    collectionResponse.collection.modifiedBy should be("somebody@theguardian.com")
    collectionResponse.collection.curatedContent.size should be (0)
    collectionResponse.collection.backfill should be(Some("search?tag=tone/reviews"))
  }

}
