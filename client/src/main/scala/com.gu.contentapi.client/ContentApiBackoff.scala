package com.gu.contentapi.client

import java.util.concurrent.TimeUnit

import com.gu.contentapi.client.model.{ContentApiRecoverableException, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

case class ContentApiBackoffException(message: String) extends RuntimeException(message, null, false, false)

abstract class ContentApiBackoff(implicit executor: ScheduledExecutor) extends Product with Serializable { self =>

  def increment: ContentApiBackoff = self match {
    // check max retries reached
    case Exponential(_, n, max) if n == max => RetryFailed(max)
    case Multiple(_, n, max, _) if n == max => RetryFailed(max)
    case Constant(_, n, max) if n == max => RetryFailed(max)
    // setup next delay cycle
    case Exponential(d, n, max) =>
      val delay = if (n == 0) Duration(d.toMillis, TimeUnit.MILLISECONDS) else Duration(Math.pow(2, n) * d.toMillis, TimeUnit.MILLISECONDS)
      Exponential(delay, n + 1, max)
    case Multiple(d, n, max, f) =>
      val delay = if (n == 0) Duration(d.toMillis, TimeUnit.MILLISECONDS) else Duration(f * d.toMillis, TimeUnit.MILLISECONDS)
      Multiple(delay, n + 1, max, f)
    case Constant(d, n, max) =>
      Constant(d, n + 1, max)
    case x => x
  }

  def currentState: ContentApiBackoff = self

  def execute(operation: => Future[HttpResponse])(implicit context: ExecutionContext): Future[HttpResponse] = {
    self match {
      case r: Retryable if(r.attempts == 0) => ContentApiBackoff.attempt(self.increment, operation)
      case r: Retryable => executor.sleepFor(r.delay).flatMap { _ => ContentApiBackoff.attempt(self.increment, operation) }
      case _: RetryFailed => Future.failed(ContentApiBackoffException("Backoff failed after retries"))
    }
  }

}

abstract class Retryable(implicit executor: ScheduledExecutor) extends ContentApiBackoff {
  val delay: Duration
  val attempts: Int
  val maxAttempts: Int
}

final case class Exponential private (delay: Duration, attempts: Int, maxAttempts: Int)(implicit executor: ScheduledExecutor) extends Retryable
final case class Multiple private (delay: Duration, attempts: Int, maxAttempts: Int, factor: Double)(implicit executor: ScheduledExecutor) extends Retryable
final case class Constant private (delay: Duration, attempts: Int, maxAttempts: Int)(implicit executor: ScheduledExecutor) extends Retryable
final case class RetryFailed private(attempts: Int)(implicit executor: ScheduledExecutor) extends ContentApiBackoff

object ContentApiBackoff {
  private val defaultMaxAttempts = 3
  private val defaultExponentialMinimumInterval = 100L
  private val defaultMinimumInterval = 250L
  private val defaultMinimumMultiplierFactor = 2.0

  def exponentialStrategy(d: Duration, maxAttempts: Int)(implicit executor: ScheduledExecutor): Exponential = exponential(d, maxAttempts)
  def doublingStrategy(d: Duration, maxAttempts: Int)(implicit executor: ScheduledExecutor): Multiple = multiple(d, maxAttempts, 2.0)
  def multiplierStrategy(d: Duration, maxAttempts: Int, multiplier: Double)(implicit executor: ScheduledExecutor): Multiple = multiple(d, maxAttempts, multiplier)
  def constantStrategy(d: Duration, maxAttempts: Int)(implicit executor: ScheduledExecutor): Constant = constant(d, maxAttempts)

  private def exponential(
    min: Duration = Duration(defaultExponentialMinimumInterval, TimeUnit.MILLISECONDS),
    maxAttempts: Int = defaultMaxAttempts
  )(implicit executor: ScheduledExecutor): Exponential = {
    val ln = Math.max(min.toMillis, defaultExponentialMinimumInterval)
    val mx = if (maxAttempts > 0) maxAttempts else 1
    Exponential(Duration(ln, TimeUnit.MILLISECONDS), 0, mx)
  }

  private def multiple(
    min: Duration = Duration(defaultMinimumInterval, TimeUnit.MILLISECONDS),
    maxAttempts: Int = defaultMaxAttempts,
    factor: Double
  )(implicit executor: ScheduledExecutor): Multiple = {
    val ln = Math.max(min.toMillis, defaultMinimumInterval)
    val mx = if (maxAttempts > 0) maxAttempts else 1
    val fc = if (factor < defaultMinimumMultiplierFactor) defaultMinimumMultiplierFactor else factor
    Multiple(Duration(ln, TimeUnit.MILLISECONDS), 0, mx, fc)
  }

  private def constant(
    min: Duration = Duration(defaultMinimumInterval, TimeUnit.MILLISECONDS),
    maxAttempts: Int = defaultMaxAttempts
  )(implicit executor: ScheduledExecutor): Constant = {
    val ln = Math.max(min.toMillis, defaultMinimumInterval)
    val mx = if (maxAttempts > 0) maxAttempts else 1
    Constant(Duration(ln, TimeUnit.MILLISECONDS), 0, mx)
  }

  private def attempt(backoff: ContentApiBackoff, operation: ⇒ Future[HttpResponse])(implicit context: ExecutionContext): Future[HttpResponse] = {
    operation
      .recoverWith {
        case _: ContentApiRecoverableException => backoff.execute(operation)
      }
  }

}
