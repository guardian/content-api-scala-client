package com.gu.contentapi.client

import java.io.IOException
import java.util.concurrent.TimeUnit

import com.gu.contentapi.client.model.{ContentApiRecoverableException, HttpResponse, ItemQuery}
import okhttp3.{Call, Callback, OkHttpClient, Request, Response}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Inside, Matchers, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object FakeGuardianContentClient {
  private final val ApiKeyProperty = "CAPI_TEST_KEY"
  private val apiKey: String = {
    Option(System.getProperty(ApiKeyProperty)) orElse Option(System.getenv(ApiKeyProperty))
    }.orNull ensuring(_ != null, s"Please supply a $ApiKeyProperty as a system property or an environment variable e.g. sbt -D$ApiKeyProperty=some-api-key")
}

class FakeGuardianContentClient(backoffStrategy: ContentApiBackoff) extends GuardianContentClient(FakeGuardianContentClient.apiKey, backoffStrategy) {

  private var attempts = 0
  private var failWithCode = 0
  private var expectedFailures = 0

  def resetAttemptCount(): Unit = {
    attempts = 0
  }

  def attemptCount: Int = attempts

  def setFailureCode(code: Int): Unit = {
    failWithCode = code
  }

  def setFailureCount(failures: Int) = {
    expectedFailures = failures
  }

  override def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse] = {

    val reqBuilder = new Request.Builder().url(url)
    val req = headers.foldLeft(reqBuilder) {
      case (r, (name, value)) => r.header(name, value)
    }.build()

    val promise = Promise[HttpResponse]()

    attempts += 1
    http.newCall(req).enqueue(new Callback() {
      override def onFailure(call: Call, e: IOException): Unit = promise.failure(e)
      override def onResponse(call: Call, response: Response): Unit = {
        if (attempts < expectedFailures) {
          try {
            promise.failure(ContentApiRecoverableException(failWithCode, s"failed with result $failWithCode. This is intentional"))
          } finally {
            response.body().close() // because we're not processing the response body here
          }
        } else {
          promise.success(HttpResponse(response.body().bytes, response.code(), response.message()))
        }
      }
    })

    promise.future
  }

}

class GuardianContentClientBackoffTest extends FlatSpec with Matchers with ScalaFutures with OptionValues with BeforeAndAfterAll with Inside with IntegrationPatience {

  private val TestItemPath = "commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry"

  "Client interface" should "establish the backoff strategy" in {
    val myInterval = 250L
    val myAttempts = 3
    val myStrategy = ContentApiBackoff.doublingStrategy(Duration(myInterval, TimeUnit.MILLISECONDS), myAttempts)
    val fakeApi = new FakeGuardianContentClient(myStrategy)
    val expectedStrategy = Multiple(Duration(myInterval, TimeUnit.MILLISECONDS), 0, myAttempts, 2.0)
    fakeApi.backoffStrategy should be(expectedStrategy)
    fakeApi.shutdown()
  }

  it should "succeed after two 408 retry attempts" in {
    val myInterval = 250L
    val myAttempts = 3
    val myStrategy = ContentApiBackoff.doublingStrategy(Duration(myInterval, TimeUnit.MILLISECONDS), myAttempts)
    val fakeApi = new FakeGuardianContentClient(myStrategy)
    fakeApi.setFailureCode(408)
    fakeApi.setFailureCount(myAttempts - 1)  // make sure we succeed!
    val query = ItemQuery(TestItemPath)
    val content = for {
      response <- fakeApi.getResponse(query)
    } yield response.content.get
    content.futureValue.id should be (TestItemPath)
    fakeApi.shutdown()
  }

  it should "fail after three 429 retries" in {
    val myInterval = 250L
    val myRetries = 3 // i.e. try this once, and then make three retry attempts = 4 attempts in total
    val myStrategy = ContentApiBackoff.doublingStrategy(Duration(myInterval, TimeUnit.MILLISECONDS), myRetries)
    val fakeApi = new FakeGuardianContentClient(myStrategy)
    fakeApi.setFailureCode(429)
    fakeApi.setFailureCount(myRetries + 2) // make sure we fail! (it's +2 because the initial attempt is not a retry)
    val query = ItemQuery(TestItemPath)
    try {
      fakeApi.getResponse(query)
    } catch {
      case e: Exception => e.getMessage should be ("Retry failed after 3 retries")
    }
    fakeApi.shutdown()
  }

  it should "retry all recoverable error codes" in {
    val myInterval = 250L
    val myRetries = 3 // i.e. try once, then retry twice = 3 attempts total
    val myStrategy = ContentApiBackoff.doublingStrategy(Duration(myInterval, TimeUnit.MILLISECONDS), myRetries)
    val fakeApi = new FakeGuardianContentClient(myStrategy)
    fakeApi.setFailureCount(myRetries - 1) // ensure we make the expected number of attempts
    val query = ItemQuery(TestItemPath)

    HttpResponse.failedButMaybeRecoverable.foreach(code => {
      fakeApi.resetAttemptCount()
      fakeApi.setFailureCode(code)
      val content = for {
        response <- fakeApi.getResponse(query)
      } yield response.content.get
      content.futureValue.id should be (TestItemPath)
      fakeApi.attemptCount should be (myRetries - 1)
    })
  }

}
