package com.gu.contentapi.client

import com.gu.contentatom.thrift.{AtomData, AtomType}
import com.gu.contentapi.client.model.v1.{ContentType, ErrorResponse}
import com.gu.contentapi.client.model.{ItemQuery, SearchQuery}
import java.time.Instant
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Inside, Matchers, OptionValues}
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global

class ContentApiClientTest extends FlatSpec with Matchers with ScalaFutures with OptionValues with BeforeAndAfterAll with Inside {
  private val api = new ContentApiClient {
    val apiKey = "TEST-API-KEY"
    val userAgent = ""
    val targetUrl = ""

    def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext) = Future.failed(new Throwable("This will never get called"))
  }

  it should "correctly add API key to request" in {
    api.url(ContentApiClient.search) should include(s"api-key=${api.apiKey}")
  }

  it should "understand custom parameters" in {
    val now = Instant.now()
    val params = ContentApiClient.search
      .stringParam("aStringParam", "foo")
      .intParam("aIntParam", 3)
      .dateParam("aDateParam", now)
      .boolParam("aBoolParam", true)
      .parameters

    params.get("aStringParam") should be (Some("foo"))
    params.get("aIntParam") should be (Some("3"))
    params.get("aDateParam") should be (Some(now.toString))
    params.get("aBoolParam") should be (Some("true"))
  }

}
