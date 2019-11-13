package com.gu.contentapi.client

import java.io.IOException
import java.util.concurrent.TimeUnit

import com.gu.contentapi.client.model.{ContentApiRecoverableException, HttpResponse, ItemQuery}
import okhttp3.{Call, Callback, OkHttpClient, Request, Response}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Inside, Matchers, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.exceptions.TestFailedException

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object FakeGuardianContentClient {
  private final val ApiKeyProperty = "CAPI_TEST_KEY"
  private val apiKey: String = {
    Option(System.getProperty(ApiKeyProperty)) orElse Option(System.getenv(ApiKeyProperty))
    }.orNull ensuring(_ != null, s"Please supply a $ApiKeyProperty as a system property or an environment variable e.g. sbt -D$ApiKeyProperty=some-api-key")
}

class FakeGuardianContentClient(backoffStrategy: ContentApiBackoff, interval: Long, retries: Int, failCode: Option[Int], alwaysFail: Boolean = false)(implicit executor: ScheduledExecutor) extends GuardianContentClient(FakeGuardianContentClient.apiKey, backoffStrategy) {

  private var attempts = 0

  def attemptCount: Int = attempts

  override def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse] = {

    val reqBuilder = new Request.Builder().url(url)
    val req = headers.foldLeft(reqBuilder) {
      case (r, (name, value)) => r.header(name, value)
    }.build()

    val promise = Promise[HttpResponse]()

    attempts += 1
    http.newCall(req).enqueue(new Callback() {
      override def onFailure(call: Call, e: IOException): Unit = {
        // this is a genuinely unexpected failure - pass it back
        promise.failure(e)
      }
      override def onResponse(call: Call, response: Response): Unit = {
        try {
          failCode match {
            case code: Some[Int] if (HttpResponse.failedButMaybeRecoverable.contains(code.get) && attempts < retries) || alwaysFail =>
              val msg = s"Failed with recoverable result ${code.get}. This is intentional"
              promise.failure(ContentApiRecoverableException(code.get, msg))
            case _ if alwaysFail =>
              val msg = "Failed, unrecoverable. This is intentional"
              promise.failure(ContentApiBackoffException(msg))
            case _ =>
              val msg = response.message()
              promise.success(HttpResponse(response.body().bytes, response.code(), msg))
          }
        } finally {
          response.body().close() // because we _may_ not have processed the response body above
        }
      }
    })

    promise.future
  }

}

class GuardianContentClientBackoffTest extends FlatSpec with Matchers with ScalaFutures with OptionValues with BeforeAndAfterAll with Inside with IntegrationPatience {

  private val TestItemPath = "commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry"

  implicit val executor: ScheduledExecutor = ScheduledExecutor()

  "Client interface" should "establish the backoff strategy" in {
    val myInterval = 250L
    val myAttempts = 3
    val myStrategy = ContentApiBackoff.doublingStrategy(Duration(myInterval, TimeUnit.MILLISECONDS), myAttempts)
    val fakeApi = new FakeGuardianContentClient(myStrategy, myInterval, myAttempts, None)
    val expectedStrategy = Multiple(Duration(myInterval, TimeUnit.MILLISECONDS), 0, myAttempts, 2.0)
    fakeApi.backoffStrategy should be(expectedStrategy)
    fakeApi.shutdown()
  }

  it should "succeed after two 408 retry attempts" in {
    val myInterval = 250L
    val myAttempts = 3
    val failureCode = 408
    val myStrategy = ContentApiBackoff.doublingStrategy(Duration(myInterval, TimeUnit.MILLISECONDS), myAttempts)
    val fakeApi = new FakeGuardianContentClient(myStrategy, myInterval, myAttempts, Some(failureCode))
    val query = ItemQuery(TestItemPath)
    val content = for {
      response <- fakeApi.getResponse(query)
    } yield response.content.get
    content.futureValue.id should be (TestItemPath)
    fakeApi.shutdown()
  }

  it should "fail after three 429 retries" in {
    val myInterval = 500L
    val myRetries = 3 // i.e. try this once, and then make three retry attempts = 4 attempts in total
    val failureCode = 429
    val alwaysFail = true
    val myStrategy = ContentApiBackoff.doublingStrategy(Duration(myInterval, TimeUnit.MILLISECONDS), myRetries)
    val fakeApi = new FakeGuardianContentClient(myStrategy, myInterval, myRetries, Some(failureCode), alwaysFail)
    val query = ItemQuery(TestItemPath)

    val result = for {
      response <- fakeApi.getResponse(query)
    } yield response.content.get

    // there must be a nicer way to handle this
    val expectedExceptionMessage = "The future returned an exception of type: com.gu.contentapi.client.ContentApiBackoffException, with message: Backoff failed after 3 attempts."
    val caught = intercept[TestFailedException] {
      result.futureValue
    }
    assert(caught.getMessage == expectedExceptionMessage)

    fakeApi.shutdown()
  }

  it should "retry (successfully) all recoverable error codes" in {
    val myInterval = 250L
    val myRetries = 2 // i.e. try once, then retry once = 2 attempts total
    val myStrategy = ContentApiBackoff.doublingStrategy(Duration(myInterval, TimeUnit.MILLISECONDS), myRetries)
    val query = ItemQuery(TestItemPath)

    HttpResponse.failedButMaybeRecoverable.foreach(code => {
      val fakeApi = new FakeGuardianContentClient(myStrategy, myInterval, myRetries, Some(code))
      val content = for {
        response <- fakeApi.getResponse(query)
      } yield response.content.get
      content.futureValue.id should be (TestItemPath)
      fakeApi.attemptCount should be (myRetries)
      fakeApi.shutdown()
    })
  }

}
