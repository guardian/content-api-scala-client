package com.gu.contentapi.client

import java.util.concurrent.TimeUnit

import com.gu.contentapi.client.model.{ContentApiRecoverableException, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

abstract class ContentApiBackoff extends Product with Serializable { self =>

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
      case r: Retryable => ContentApiBackoff.scheduledExecutor.sleepFor(r.delay).flatMap { _ => ContentApiBackoff.attempt(self.increment, operation) }
      case f: RetryFailed => Future.failed(new Exception(s"Retry failed after ${f.attempts} retries"))
    }
  }

}

abstract class Retryable extends ContentApiBackoff {
  val delay: Duration
  val attempts: Int
  val maxAttempts: Int
}

sealed case class Exponential private (delay: Duration, attempts: Int, maxAttempts: Int) extends Retryable
sealed case class Multiple private (delay: Duration, attempts: Int, maxAttempts: Int, factor: Double) extends Retryable
sealed case class Constant private (delay: Duration, attempts: Int, maxAttempts: Int) extends Retryable
sealed case class RetryFailed private(attempts: Int) extends ContentApiBackoff

object ContentApiBackoff {
  private val defaultMaxAttempts = 3
  private val defaultExponentialMinimumInterval = 100L
  private val defaultMinimumInterval = 250L
  private val defaultMinimumMultiplierFactor = 2.0

  private lazy val scheduledExecutor = new ScheduledExecutor

  def exponentialStrategy(d: Duration, maxAttempts: Int): Exponential = exponential(d, maxAttempts)
  def doublingStrategy(d: Duration, maxAttempts: Int): Multiple = multiple(d, maxAttempts, 2.0)
  def multiplierStrategy(d: Duration, maxAttempts: Int, multiplier: Double): Multiple = multiple(d, maxAttempts, multiplier)
  def constantStrategy(d: Duration, maxAttempts: Int): Constant = constant(d, maxAttempts)

  private def exponential(
    min: Duration = Duration(defaultExponentialMinimumInterval, TimeUnit.MILLISECONDS),
    maxAttempts: Int = defaultMaxAttempts
  ): Exponential = {
    val ln = if (min.toMillis < defaultExponentialMinimumInterval) defaultExponentialMinimumInterval else min.toMillis
    val mx = if (maxAttempts > 0) maxAttempts else 1
    Exponential(Duration(ln, TimeUnit.MILLISECONDS), 0, mx)
  }

  private def multiple(
    min: Duration = Duration(defaultMinimumInterval, TimeUnit.MILLISECONDS),
    maxAttempts: Int = defaultMaxAttempts,
    factor: Double
  ): Multiple = {
    val ln = if (min.toMillis < defaultMinimumInterval) defaultMinimumInterval else min.toMillis
    val mx = if (maxAttempts > 0) maxAttempts else 1
    val fc = if (factor < defaultMinimumMultiplierFactor) defaultMinimumMultiplierFactor else factor
    Multiple(Duration(ln, TimeUnit.MILLISECONDS), 0, mx, fc)
  }

  private def constant(
    min: Duration = Duration(defaultMinimumInterval, TimeUnit.MILLISECONDS),
    maxAttempts: Int = defaultMaxAttempts
  ): Constant = {
    val ln = if (min.toMillis < defaultMinimumInterval) defaultMinimumInterval else min.toMillis
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
