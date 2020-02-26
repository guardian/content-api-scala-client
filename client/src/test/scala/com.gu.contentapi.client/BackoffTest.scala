package com.gu.contentapi.client

import java.util.concurrent.TimeUnit

import com.gu.contentapi.client.model._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class BackoffTest extends FlatSpec with Matchers with ScalaFutures with OptionValues with BeforeAndAfterAll with Inside with Inspectors {
  private def NANOS = TimeUnit.NANOSECONDS
  private def MILLIS = TimeUnit.MILLISECONDS

  implicit val schedEx: ScheduledExecutor = ScheduledExecutor()

  def clientWithBackoff(strategy: BackoffStrategy) = new ContentApiClient {

    override implicit val executor: ScheduledExecutor = schedEx
    override val backoffStrategy: BackoffStrategy = strategy

    val apiKey = "TEST-API-KEY"

    def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext) = {
      Future.successful(HttpResponse(Array(), 500, "status"))
    }
  }

  "Client interface" should "have the expected doubling backoff strategy" in {
    val myInterval = 250L
    val myRetries = 3
    val myStrategy = BackoffStrategy.doublingStrategy(Duration(myInterval, MILLIS), myRetries)
    val myApi = clientWithBackoff(myStrategy)
    val expectedStrategy = Multiple(Duration(myInterval, MILLIS), 0, myRetries, 2.0)
    myApi.backoffStrategy should be(expectedStrategy)
  }

  it should "have the expected minimum doubling backoff properties" in {
    // 10 NANOS should become 250 MILLIS
    val myInterval = 10L
    val myRetries = 20
    val myStrategy = BackoffStrategy.doublingStrategy(Duration(myInterval, NANOS), myRetries)
    val myApi = clientWithBackoff(myStrategy)
    val expectedStrategy = Multiple(Duration(250L, MILLIS), 0, myRetries, 2.0)
    myApi.backoffStrategy should be(expectedStrategy)
  }

  it should "not allow a doubling strategy with zero retries" in {
    val myInterval = 250L
    val myRetries = 0
    val myStrategy = BackoffStrategy.doublingStrategy(Duration(myInterval, MILLIS), myRetries)
    val myApi = clientWithBackoff(myStrategy)
    val expectedStrategy = Multiple(Duration(myInterval, MILLIS), 0, 1, 2.0)
    myApi.backoffStrategy should be(expectedStrategy)
  }

  "Client interface with an exponential backoff" should "respect user's parameters if they meet minimum limits" in {
    // an exponential backoff allows a minimum interval of 100 MILLIS
    val myInterval = 100L
    val myRetries = 10
    val myStrategy = BackoffStrategy.exponentialStrategy(Duration(myInterval, MILLIS), myRetries)
    val myApi = clientWithBackoff(myStrategy)
    val expectedStrategy = Exponential(Duration(myInterval, MILLIS), 0, myRetries)
    myApi.backoffStrategy should be(expectedStrategy)
  }

  it should "protect the backend from abuse by too-low limits" in {
    // an attempt to delay just 10 MILLIS should be set to minimum of 100 MILLIS
    val myInterval = 10L
    val myRetries = 1
    val myStrategy = BackoffStrategy.exponentialStrategy(Duration(myInterval, MILLIS), myRetries)
    val myApi = clientWithBackoff(myStrategy)
    val expectedStrategy = Exponential(Duration(100L, MILLIS), 0, myRetries)
    myApi.backoffStrategy should be(expectedStrategy)
  }

  it should "not allow an exponential strategy with zero retries" in {
    val myInterval = 10L
    val myRetries = 0
    val myStrategy = BackoffStrategy.exponentialStrategy(Duration(myInterval, MILLIS), myRetries)
    val myApi = clientWithBackoff(myStrategy)
    val expectedStrategy = Exponential(Duration(100L, MILLIS), 0, 1)
    myApi.backoffStrategy should be(expectedStrategy)
  }

  it should "respect user's parameters even if they request ridiculous upper limits" in {
    val myInterval = 500L
    val myRetries = 20
    val myStrategy = BackoffStrategy.exponentialStrategy(Duration(myInterval, MILLIS), myRetries)
    val myApi = clientWithBackoff(myStrategy)
    val expectedStrategy = Exponential(Duration(myInterval, MILLIS), 0, myRetries)
    myApi.backoffStrategy should be(expectedStrategy)
  }

  "Client interface with a constant wait backoff strategy" should "be initialised correctly" in {
    // an exponential backoff allows a minimum interval of 100 MILLIS
    val myInterval = 500L
    val myRetries = 5
    val myStrategy = BackoffStrategy.constantStrategy(Duration(myInterval, MILLIS), myRetries)
    val myApi = clientWithBackoff(myStrategy)
    val expectedStrategy = Constant(Duration(myInterval, MILLIS), 0, myRetries)
    myApi.backoffStrategy should be(expectedStrategy)
  }

  it should "respect minimum parameters" in {
    val myInterval = 1L
    val myRetries = 100
    val myStrategy = BackoffStrategy.constantStrategy(Duration(myInterval, MILLIS), myRetries)
    val myApi = clientWithBackoff(myStrategy)
    val expectedStrategy = Constant(Duration(250L, MILLIS), 0, myRetries)
    myApi.backoffStrategy should be(expectedStrategy)
  }

  it should "not allow a constant strategy with zero retries" in {
    val myInterval = 250L
    val myRetries = 0
    val myStrategy = BackoffStrategy.constantStrategy(Duration(myInterval, MILLIS), myRetries)
    val myApi = clientWithBackoff(myStrategy)
    val expectedStrategy = Constant(Duration(myInterval, MILLIS), 0, 1)
    myApi.backoffStrategy should be(expectedStrategy)
  }

  "When invoked, a doubling backoff strategy" should "increment properly" in {
    val myInterval = 250L
    val myRetries = 4
    val myStrategy = BackoffStrategy.doublingStrategy(Duration(myInterval, MILLIS), myRetries)
    val myApi = clientWithBackoff(myStrategy)

    val firstRetry = myApi.backoffStrategy.increment
    firstRetry should be(Multiple(Duration(250L, MILLIS), 1, myRetries, 2.0))

    val secondRetry = firstRetry.increment
    secondRetry should be(Multiple(Duration(500L, MILLIS), 2, myRetries, 2.0))

    val thirdRetry = secondRetry.increment
    thirdRetry should be(Multiple(Duration(1000L, MILLIS), 3, myRetries, 2.0))

    val fourthRetry = thirdRetry.increment
    fourthRetry should be(Multiple(Duration(2000L, MILLIS), 4, myRetries, 2.0))

    val fifthRetry = fourthRetry.increment
    fifthRetry should be(com.gu.contentapi.client.RetryFailed(4))
  }

  "When invoked, a multiplier backoff strategy" should "increment backoff values correctly with custom factor" in {
    val myInterval = 350L
    val myFactor = 3.0
    val myRetries = 3
    val myStrategy = BackoffStrategy.multiplierStrategy(Duration(myInterval, MILLIS), myRetries, myFactor)
    val myApi = clientWithBackoff(myStrategy)

    val firstRetry = myApi.backoffStrategy.increment
    firstRetry should be(Multiple(Duration(350L, MILLIS), 1, myRetries, myFactor))

    val secondRetry = firstRetry.increment
    secondRetry should be(Multiple(Duration(1050L, MILLIS), 2, myRetries, myFactor))

    val thirdRetry = secondRetry.increment
    thirdRetry should be(Multiple(Duration(3150L, MILLIS), 3, myRetries, myFactor))

    val fourthRetry = thirdRetry.increment
    fourthRetry should be(com.gu.contentapi.client.RetryFailed(3))
  }

  "When invoked, an exponential backoff strategy" should "increment backoff values correctly" in {
    val myInterval = 100L
    val myRetries = 4
    val myStrategy = BackoffStrategy.exponentialStrategy(Duration(myInterval, MILLIS), myRetries)
    val myApi = clientWithBackoff(myStrategy)

    val firstRetry = myApi.backoffStrategy.increment
    firstRetry should be(Exponential(Duration(100L, MILLIS), 1, myRetries))

    val secondRetry = firstRetry.increment
    secondRetry should be(Exponential(Duration(200L, MILLIS), 2, myRetries))

    val thirdRetry = secondRetry.increment
    thirdRetry should be(Exponential(Duration(800L, MILLIS), 3, myRetries))

    val fourthRetry = thirdRetry.increment
    fourthRetry should be(Exponential(Duration(6400L, MILLIS), 4, myRetries))

    val fifthRetry = fourthRetry.increment
    fifthRetry should be(com.gu.contentapi.client.RetryFailed(4))
  }

  "When invoked, a constant wait backoff strategy" should "increment itself correctly" in {
    val myInterval = 1000L
    val myRetries = 3
    val myStrategy = BackoffStrategy.constantStrategy(Duration(myInterval, MILLIS), myRetries)
    val myApi = clientWithBackoff(myStrategy)

    val firstRetry = myApi.backoffStrategy.increment
    firstRetry should be(Constant(Duration(1000L, MILLIS), 1, myRetries))

    val secondRetry = firstRetry.increment
    secondRetry should be(Constant(Duration(1000L, MILLIS), 2, myRetries))

    val thirdRetry = secondRetry.increment
    thirdRetry should be(Constant(Duration(1000L, MILLIS), 3, myRetries))

    val fourthRetry = thirdRetry.increment
    fourthRetry should be(com.gu.contentapi.client.RetryFailed(3))
  }

}
