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
  private def SECONDS = TimeUnit.SECONDS
  private def exponentialBackoff(d: Duration, attempts: Int) = Backoff.exponential(d, attempts)
  private def doublingBackoff(d: Duration, attempts: Int) = Backoff.doubling(d, attempts)
  private def multiplierBackoff(d: Duration, attempts: Int, factor: Double) = Backoff.multiple(d, attempts, factor)

  def clientWithBackoff(strategy: Backoff) = new ContentApiClient {

    override val backoffStrategy: Backoff = strategy

    val apiKey = "TEST-API-KEY"

    def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext) = {
      Future.successful(HttpResponse(Array(), 500, "status"))
    }
  }

  "Client interface" should "be created with expected doubling backoff strategy" in {
    val myInterval = 250L
    val myRetries = 3
    val myApi = clientWithBackoff(doublingBackoff(Duration(myInterval, MILLIS), myRetries))
    val expectedStrategy = Multiple(Duration(myInterval, MILLIS), 1, myRetries, 2.0)
    myApi.backoffStrategy should be(expectedStrategy)
  }

  it should "be created with expected minimum and maximum doubling backoff properties" in {
    val myInterval = 10L
    val myRetries = 20
    val myApi = clientWithBackoff(doublingBackoff(Duration(myInterval, NANOS), myRetries))
    val expectedStrategy = Multiple(Duration(250L, MILLIS), 1, 10, 2.0)
    myApi.backoffStrategy should be(expectedStrategy)
  }

  "Client interface with an exponential backoff" should "respect user's parameters if they are within acceptable limits" in {
    val myInterval = 100L
    val myRetries = 5
    val myApi = clientWithBackoff(Backoff.exponential(Duration(myInterval, MILLIS), myRetries))
    val expectedStrategy = Exponential(Duration(myInterval, MILLIS), 1, myRetries)
    myApi.backoffStrategy should be(expectedStrategy)
  }

  "When invoked, a doubling backoff strategy" should "increment properly" in {
    val myInterval = 250L
    val myRetries = 4
    val myApi = clientWithBackoff(Backoff.doubling(Duration(myInterval, MILLIS), myRetries))

    val firstRetry = myApi.backoffStrategy.state
    firstRetry should be(Multiple(Duration(500L, MILLIS), 2, myRetries, 2.0))

    val secondRetry = firstRetry.state
    secondRetry should be(Multiple(Duration(1L, SECONDS), 3, myRetries, 2.0))

    val thirdRetry = secondRetry.state
    thirdRetry should be(Multiple(Duration(2L, SECONDS), 4, myRetries, 2.0))

    val fourthRetry = thirdRetry.state
    fourthRetry should be(com.gu.contentapi.client.Failed(4))
  }

  "When invoked, a multiplier backoff strategy" should "increment backoff values correctly with custom factor" in {
    val myInterval = 350L
    val myFactor = 3.0
    val myRetries = 3
    val myApi = clientWithBackoff(Backoff.multiple(Duration(myInterval, MILLIS), myRetries, myFactor))

    val firstRetry = myApi.backoffStrategy.state
    firstRetry should be(Multiple(Duration(1050L, MILLIS), 2, myRetries, myFactor))

    val secondRetry = firstRetry.state
    secondRetry should be(Multiple(Duration(3150L, MILLIS), 3, myRetries, myFactor))

    val thirdRetry = secondRetry.state
    thirdRetry should be(com.gu.contentapi.client.Failed(3))
  }

  "When invoked, an exponential backoff strategy" should "increment backoff values correctly" in {
    val myInterval = 100L
    val myRetries = 4
    val myApi = clientWithBackoff(Backoff.exponential(Duration(myInterval, MILLIS), myRetries))

    val firstRetry = myApi.backoffStrategy.state
    firstRetry should be(Exponential(Duration(200L, MILLIS), 2, myRetries))

    val secondRetry = firstRetry.state
    secondRetry should be(Exponential(Duration(800L, MILLIS), 3, myRetries))

    val thirdRetry = secondRetry.state
    thirdRetry should be(Exponential(Duration(6400L, MILLIS), 4, myRetries))

    val fourthRetry = thirdRetry.state
    fourthRetry should be(com.gu.contentapi.client.Failed(4))
  }

}
