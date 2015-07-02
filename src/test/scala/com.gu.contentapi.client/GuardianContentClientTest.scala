package com.gu.contentapi.client

import com.gu.contentapi.client.model.ItemQuery
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{OptionValues, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class GuardianContentClientTest extends FlatSpec with Matchers with ClientTest with ScalaFutures with OptionValues {
  implicit override val patienceConfig = PatienceConfig(timeout = Span(2, Seconds))

  "client interface" should "successfully call the Content API" in {
    val query = ItemQuery("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry")
    val content = for {
      response <- api.getResponse(query)
    } yield response.content.get
    content.futureValue.id should be ("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry")
  }

  it should "return errors as a broken promise" in {
    val query = ItemQuery("something-that-does-not-exist")
    val errorTest = api.getResponse(query) recover { case error =>
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
    val query = ItemQuery("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry")
    val content = for (response <- api.getResponse(query)) yield response.content.get
    content.futureValue.id should be ("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry")
  }

  it should "perform a given removed content query" in {
    val query = api.removedContent.reason("expired")
    val results = for (response <- api.getResponse(query)) yield response.results
    val fResults = results.futureValue
    fResults.size should be (10)
  }
}
