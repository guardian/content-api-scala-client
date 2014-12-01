package com.gu.contentapi.client

import com.gu.contentapi.client.model.{TagsQuery, CollectionQuery, ItemQuery}
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span, Millis}
import org.scalatest.{OptionValues, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class GuardianContentClientTest extends FlatSpec with Matchers with ClientTest with ScalaFutures with OptionValues {

  implicit def executionContext = ExecutionContext.global
  implicit override val patienceConfig = PatienceConfig(timeout = Span(2, Seconds))
  import api.implicits._

  "client interface" should "successfully call the Content API" in {
    val content = for {
      response <- api.item.itemId("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry").response
    } yield response.content.get
    content.futureValue.id should be ("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry")
  }

  it should "return errors as a broken promise" in {
    val errorResponse = api.item.itemId("something-that-does-not-exist").response
    val errorTest = errorResponse recover { case error =>
      error should be (GuardianContentApiError(404, "Not Found"))
    }
    errorTest.futureValue
  }

  it should "correctly add API key to request" in {
    api.url("location", Map.empty) should include(s"api-key=${api.apiKey}")
  }

  it should "understand custom parameters" in {
    val now = new DateTime
    val params = api.search
      .stringParam("aStringParam", "foo")
      .intParam("aIntParam", 3)
      .dateParam("aDateParam", now)
      .boolParam("aBoolParam", true)
      .parameters

    params.get("aStringParam") should be (Some("foo"))
    params.get("aIntParam") should be (Some("3"))
    params.get("aDateParam") should be (Some(now.toString()))
    params.get("aBoolParam") should be (Some("true"))
  }

  it should "perform a given item query" in {
    val query = ItemQuery().itemId("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry")
    val content = for (response <- api.getResponse(query)) yield response.content.get
    content.futureValue.id should be ("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry")
  }

  it should "support itemQuery's apiUrl" in {
    val query = ItemQuery().apiUrl(s"${api.targetUrl}/commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry")
    query.id.value should equal("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry")
  }

  it should "perform a given collection query" in {
    val query = CollectionQuery().collectionId("uk-alpha/news/regular-stories")
    val collection = for (response <- api.getResponse(query)) yield response.collection
    collection.futureValue.id should equal("uk-alpha/news/regular-stories")
  }

  it should "support collectionQuery's apiUrl method" in {
    val query = CollectionQuery().apiUrl(s"${api.targetUrl}/collections/41cc-b696-db2e-0156")
    query.collectionId.value should equal("41cc-b696-db2e-0156")
  }
}
