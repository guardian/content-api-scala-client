package com.gu.contentapi.client

import com.gu.contentapi.client.BackoffStrategy.constantStrategy
import org.scalatest.{AsyncWordSpecLike, Matchers, RecoverMethods}

import scala.concurrent.Future
import scala.concurrent.duration._

class RetryTest extends AsyncWordSpecLike with Matchers with RecoverMethods {

  "withRetry" should {
    val maxAttempts = 5
    val backoffStrategy = constantStrategy(delay = 100.millis, maxAttempts = maxAttempts)
    implicit val schedEx: ScheduledExecutor = ScheduledExecutor()

    "not retry if operation result is not retryable" in {
      val attemptValues: Iterator[Int] = (1 to maxAttempts).iterator
      for {
        result <- Retry.withRetry[Any](backoffStrategy, _ => false){ _ =>
          Future.successful(attemptValues.next())
        }
      } yield {
        result shouldBe 1
      }
    }

    "retry if operation result is retryable" in {
      val attemptValues: Iterator[Int] = (1 to maxAttempts).iterator
      for {
        result <- Retry.withRetry[Int](backoffStrategy, _ != 4) { _ =>
          Future.successful(attemptValues.next())
        }
      } yield {
        result shouldBe 4
      }
    }

    "return failed Future if retry count exhausted" in {
      val attemptValues: Iterator[Int] = (1 to maxAttempts * 2).iterator
      val validateFailure = recoverToSucceededIf[ContentApiBackoffException] {
        Retry.withRetry[Int](backoffStrategy, _ => true) { _ =>
          Future.successful(attemptValues.next())
        }
      }
      for {
        _ <- validateFailure
      } yield {
        attemptValues.next() shouldBe maxAttempts + 1 + 1 // max retry attempts + 1 original attempts + 1 (as it is iterator)
      }
    }
  }

}
