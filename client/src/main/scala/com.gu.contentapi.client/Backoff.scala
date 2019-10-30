package com.gu.contentapi.client

import java.util.concurrent.TimeUnit

import com.gu.contentapi.client.model.{ ContentApiRecoverableException, HttpResponse }

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

abstract class Backoff extends Product with Serializable { self =>

  def state: Backoff = self match {
    case Exponential(_, n, max) if n == max => RetryFailed(max)
    case Exponential(d, n, max) =>
      val delay = Duration(Math.pow(2, n) * d.toMillis, TimeUnit.MILLISECONDS)
      Exponential(delay, n + 1, max)
    case Multiple(_, n, max, _) if n == max => RetryFailed(max)
    case Multiple(d, n, max, f) =>
      val delay = Duration(f * d.toMillis, TimeUnit.MILLISECONDS)
      Multiple(delay, n + 1, max, f)
    case x => x
  }

  def retry(operation: ⇒ Future[HttpResponse])(implicit context: ExecutionContext): Future[HttpResponse] = {
    def delayedRetry[T <: Retrying](backoff: T) = {
      Backoff.scheduledExecutor.sleepFor(backoff.delay)
        .flatMap { _ => operation }
        .recoverWith {
          case _: ContentApiRecoverableException => retry(operation)
        }
    }

    self.state match {
      case exp: Exponential => delayedRetry(exp)
      case mul: Multiple => delayedRetry(mul)
      case f: RetryFailed => Future.failed(new Exception(s"Retry failed after ${f.attempts} retries"))
    }
  }

}

abstract class Retrying extends Backoff {
  val delay: Duration
}

sealed case class Exponential private (delay: Duration, attempts: Int, maxAttempts: Int) extends Retrying
sealed case class Multiple private (delay: Duration, attempts: Int, maxAttempts: Int, factor: Double) extends Retrying
sealed case class RetryFailed private(attempts: Int) extends Backoff

object Backoff {
  private val defaultMaxAttempts = 3
  private val defaultExponentialMinimumInterval = 100L
  private val defaultMultiplierMinimumInterval = 250L
  private val defaultMinimumMultiplierFactor = 2.0

  private lazy val scheduledExecutor = new ScheduledExecutor(1)

  def exponentialStrategy(d: Duration, maxAttempts: Int): Exponential = exponential(d, maxAttempts)
  def doublingStrategy(d: Duration, maxAttempts: Int): Multiple = multiple(d, maxAttempts, 2.0)
  def multiplierStrategy(d: Duration, maxAttempts: Int, multiplier: Double): Multiple = multiple(d, maxAttempts, multiplier)

  private def exponential(
    min: Duration = Duration(defaultExponentialMinimumInterval, TimeUnit.MILLISECONDS),
    maxAttempts: Int = defaultMaxAttempts
  ): Exponential = {
    val ln = if (min.toMillis < defaultExponentialMinimumInterval) defaultExponentialMinimumInterval else min.toMillis
    val mx = if (maxAttempts > 0) maxAttempts else 1
    Exponential(Duration(ln, TimeUnit.MILLISECONDS), 1, mx)
  }

  private def multiple(
    min: Duration = Duration(defaultMultiplierMinimumInterval, TimeUnit.MILLISECONDS),
    maxAttempts: Int = defaultMaxAttempts,
    factor: Double
  ): Multiple = {
    val ln = if (min.toMillis < defaultMultiplierMinimumInterval) defaultMultiplierMinimumInterval else min.toMillis
    val mx = if (maxAttempts > 0) maxAttempts else 1
    val fc = if (factor < defaultMinimumMultiplierFactor) defaultMinimumMultiplierFactor else factor
    Multiple(Duration(ln, TimeUnit.MILLISECONDS), 1, mx, fc)
  }
}
