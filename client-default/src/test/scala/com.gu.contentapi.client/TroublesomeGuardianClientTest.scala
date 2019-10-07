package com.gu.contentapi.client

import com.gu.contentatom.thrift.{AtomData, AtomType}
import com.gu.contentapi.client.model.v1.{ContentType, ErrorResponse, SearchResponse}
import com.gu.contentapi.client.model.{ContentApiError, ContentApiRecoverableException, HttpResponse, ItemQuery, SearchQuery}
import java.time.Instant

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Inside, Matchers, OptionValues}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future, Promise}

object TroublesomeGuardianContentClientTest {
  private final val ApiKeyProperty = "CAPI_TEST_KEY"
  private val apiKey: String = {
    Option(System.getProperty(ApiKeyProperty)) orElse Option(System.getenv(ApiKeyProperty))
    }.orNull ensuring(_ != null, s"Please supply a $ApiKeyProperty as a system property or an environment variable e.g. sbt -Dsome-api-key")
}

class TroublesomeGuardianContentClient(apiKey: String) extends GuardianContentClient(apiKey)  {
  private val promise = Promise[HttpResponse]()

  private val errorUrls = List(
    "throw/me/a/request-timeout" -> 408,
    "throw/me/a/too-many-requests" -> 429,
    "throw/me/a/service-unavailable" -> 503,
    "throw/me/a/gateway-timeout" -> 504,
    "throw/me/a/bandwidth-limit-exceeded" -> 509,
    "throw/me/a/unhandled-exception" -> 418
  )

  override def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse] = {
    val error = errorUrls.find(_._1 == url)

    val resp = error match {
      case e: Some[(String,Int)] => ContentApiRecoverableException(e.get._2, s"$url is a ${e.get._2} generating URL")
      case _ => ContentApiRecoverableException(500, "Unexpected failure in test")
    }

    Future(HttpResponse(resp.httpMessage.getBytes, resp.httpStatus, resp.httpMessage))(context)
  }
}

class TroublesomeGuardianContentClientTest extends FlatSpec with Matchers with ScalaFutures with OptionValues with BeforeAndAfterAll with Inside with IntegrationPatience {

  import TroublesomeGuardianContentClientTest.apiKey

  private val api = new TroublesomeGuardianContentClient(apiKey)
  private val test408Path = "throw/me/a/request-timeout"
  private val test429Path = "throw/me/a/too-many-requests"

  override def afterAll() {
    api.shutdown()
  }

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  "client interface" should "retry with backoff when encountering a 408 error" in {
    val query = ItemQuery(test408Path)
    val errorTest = api.getResponse(query) recover { case error =>
      error should be (ContentApiRecoverableException(408, "Request Timeout - failed after 2 retries."))
    }
    errorTest.futureValue
  }

  it should "retry with backoff when encountering a 429 error" in {
    val query = ItemQuery(test429Path)
    val errorTest = api.getResponse(query) recover { case error =>
      error should be (ContentApiRecoverableException(429, "Too Many Requests - failed after 2 retries."))
    }
    errorTest.futureValue
  }

}