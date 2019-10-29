package com.gu.contentapi.client

import java.util.concurrent.TimeUnit

import com.gu.contentapi.client.model.{ ContentApiRecoverableException, HttpResponse }

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

sealed abstract class Backoff extends Product with Serializable { self =>

  def state: Backoff = self match {
    case Exponential(_, n, max) if n == max => Failed(max)
    case Exponential(d, n, max) =>
      val delay = Duration(Math.pow(2, n) * d.toMillis, TimeUnit.MILLISECONDS)
      Exponential(delay, n + 1, max)
    case Multiple(_, n, max, _) if n == max => Failed(max)
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
      case f: Failed => Future.failed(new Exception(s"Retry failed after ${f.attempts} retries"))
    }
  }

}

abstract class Retrying extends Backoff {
  val delay: Duration
}

final case class Exponential private (delay: Duration, attempts: Int, maxAttempts: Int) extends Retrying
final case class Multiple private (delay: Duration, attempts: Int, maxAttempts: Int, factor: Double) extends Retrying
final case class Failed private (attempts: Int) extends Backoff

object Backoff {
  private val defaultMaxAttempts = 3
  private val defaultMultipleMaxAttempts = 10
  private val defaultExponentialMinimumInterval = 100L
  private def defaultExponentialMaxAttempts = 5
  private val defaultMultiplierDuration = 250L
  private val defaultMultiplierMinimumInterval = 250L
  private val defaultMinimumMultiplierFactor = 2.0

  lazy val scheduledExecutor = new ScheduledExecutor(1)

  def apply(): Backoff = doubling()

  def exponential(
    min: Duration = Duration(defaultExponentialMinimumInterval, TimeUnit.MILLISECONDS),
    maxAttempts: Int = defaultMaxAttempts
  ): Backoff = {
    val mx = if (maxAttempts > defaultExponentialMaxAttempts) defaultExponentialMaxAttempts else maxAttempts
    val ln = if (min.toMillis < defaultExponentialMinimumInterval) defaultExponentialMinimumInterval else min.toMillis
    Exponential(Duration(ln, TimeUnit.MILLISECONDS), 1, mx)
  }

  def doubling(
    min: Duration = Duration(defaultMultiplierDuration, TimeUnit.MILLISECONDS),
    maxAttempts: Int = defaultMaxAttempts
  ): Backoff = {
    val mx = if (maxAttempts > defaultMultipleMaxAttempts) defaultMultipleMaxAttempts else maxAttempts
    val ln = if (min.toMillis < defaultMultiplierMinimumInterval) defaultMultiplierMinimumInterval else min.toMillis
    Multiple(Duration(ln, TimeUnit.MILLISECONDS), 1, mx, 2.0)
  }

  def multiple(
    min: Duration = Duration(defaultMultiplierMinimumInterval, TimeUnit.MILLISECONDS),
    maxAttempts: Int = defaultMaxAttempts,
    factor: Double
  ): Backoff = {
    val mx = if (maxAttempts > defaultMultipleMaxAttempts) defaultMultipleMaxAttempts else maxAttempts
    val fc = if (factor < defaultMinimumMultiplierFactor) defaultMinimumMultiplierFactor else factor
    val ln = if (min.toMillis < defaultMultiplierMinimumInterval) defaultMultiplierMinimumInterval else min.toMillis
    Multiple(Duration(ln, TimeUnit.MILLISECONDS), 1, mx, fc)
  }
}
