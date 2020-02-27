package com.gu.contentapi.client

import java.io.IOException
import java.util.concurrent.TimeUnit

import com.gu.contentapi.client.model.{HttpResponse, ItemQuery}
import okhttp3.{Call, Callback, Request, Response}
import org.scalatest._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.exceptions.TestFailedException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future, Promise}

object FakeGuardianContentClient {
  private final val ApiKeyProperty = "CAPI_TEST_KEY"
  private val apiKey: String = {
    Option(System.getProperty(ApiKeyProperty)) orElse Option(System.getenv(ApiKeyProperty))
    }.orNull ensuring(_ != null, s"Please supply a $ApiKeyProperty as a system property or an environment variable e.g. sbt -D$ApiKeyProperty=some-api-key")
}

class FakeGuardianContentClient(retries: Int, failCode: Option[Int], alwaysFail: Boolean = false) extends GuardianContentClient(FakeGuardianContentClient.apiKey) {

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
            case Some(value) if attempts < retries || alwaysFail =>
              val msg = s"Failed with recoverable result $value. This is intentional"
              promise.success(HttpResponse("Failed".getBytes(), value, msg))
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

  implicit val executor0: ScheduledExecutor = ScheduledExecutor()

  "Client interface" should "succeed after two 408 retry attempts" in {
    val myInterval = 250L
    val myAttempts = 3
    val failureCode = 408
    val fakeApi = new FakeGuardianContentClient(myAttempts, Some(failureCode)) with RetryableContentApiClient {
      override val backoffStrategy: BackoffStrategy = BackoffStrategy.doublingStrategy(Duration(myInterval, TimeUnit.MILLISECONDS), myAttempts)
      override implicit val executor: ScheduledExecutor = executor0
    }

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
    val fakeApi = new FakeGuardianContentClient(myRetries, Some(failureCode), alwaysFail) with RetryableContentApiClient {
      override val backoffStrategy: BackoffStrategy = BackoffStrategy.doublingStrategy(Duration(myInterval, TimeUnit.MILLISECONDS), myRetries)
      override implicit val executor: ScheduledExecutor = executor0
    }
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
    val query = ItemQuery(TestItemPath)

    HttpResponse.failedButMaybeRecoverableCodes.foreach(code => {
      val fakeApi = new FakeGuardianContentClient(myRetries, Some(code)) with RetryableContentApiClient {
        override val backoffStrategy: BackoffStrategy = BackoffStrategy.doublingStrategy(Duration(myInterval, TimeUnit.MILLISECONDS), myRetries)
        override implicit val executor: ScheduledExecutor = executor0
      }
      val content = for {
        response <- fakeApi.getResponse(query)
      } yield response.content.get
      content.futureValue.id should be (TestItemPath)
      fakeApi.attemptCount should be (myRetries)
      fakeApi.shutdown()
    })
  }

}
