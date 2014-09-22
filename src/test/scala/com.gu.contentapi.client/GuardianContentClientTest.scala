package com.gu.contentapi.client

import scala.concurrent.ExecutionContext
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}
import dispatch.enrichFuture

class GuardianContentClientTest extends FlatSpec with Matchers with ClientTest {

  implicit def executionContext = ExecutionContext.global

  "client interface" should "successfully call the Content API" in {
    val content = for {
      response <- api.item.itemId("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry").response
    }
    yield response.content.get
    content.apply().id should be ("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry")
  }

  it should "return errors as a broken promise" in {
    val errorResponse = api.item.itemId("something-that-does-not-exist").response
    val errorTest = errorResponse recover { case error =>
      error should be (GuardianContentApiError(404, "Not Found"))
    }
    errorTest.apply()
  }

  it should "correctly add API key to request if present" in {
    api.search.parameters.get("api-key") should be (Some("test"))
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

}
