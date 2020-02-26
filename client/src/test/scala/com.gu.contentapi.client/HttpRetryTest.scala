package com.gu.contentapi.client

import com.gu.contentapi.client.BackoffStrategy.constantStrategy
import com.gu.contentapi.client.model.HttpResponse
import org.scalatest.{AsyncWordSpecLike, Matchers}

import scala.concurrent.Future
import scala.concurrent.duration._

class HttpRetryTest extends AsyncWordSpecLike with Matchers {

  "withRetry" should {
    val maxAttempts = 5
    val backoffStrategy = constantStrategy(delay = 100.millis, maxAttempts = maxAttempts)
    implicit val schedEx: ScheduledExecutor = ScheduledExecutor()
    val successResponse = HttpResponse(Array(), 200, "")
    val failure = HttpResponse(Array(), 429, "")

    "not retry if we get success response" in {
      val httpResponses = List(Future.successful(successResponse), Future.successful(failure), Future.successful(successResponse.copy(statusCode = 503))).iterator
      for {
        result <- HttpRetry.withRetry(backoffStrategy) { _ =>
          httpResponses.next()
        }
      } yield {
        result shouldBe successResponse
      }
    }

    "retry if we get ContentApiRecoverableException" in {
      val httpResponses = List(Future.successful(failure), Future.successful(successResponse)).iterator
      for {
        result <- HttpRetry.withRetry(backoffStrategy) { _ =>
          httpResponses.next()
        }
      } yield {
        result shouldBe successResponse
      }
    }
  }

}
