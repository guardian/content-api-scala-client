package com.gu.contentapi.client

import java.util.concurrent.TimeUnit

import com.gu.contentapi.client.Retry.RetryAttempt
import com.gu.contentapi.client.model.HttpResponse
import com.gu.contentapi.client.model.HttpResponse.IsRecoverableHttpResponse

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

case class ContentApiBackoffException(message: String) extends RuntimeException(message, null, false, false)

abstract class BackoffStrategy extends Product with Serializable { self =>

  def increment: BackoffStrategy = self match {
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
}

abstract class Retryable extends BackoffStrategy {
  val delay: Duration
  val attempts: Int
  val maxAttempts: Int
}

final case class Exponential private (delay: Duration, attempts: Int, maxAttempts: Int) extends Retryable
final case class Multiple private (delay: Duration, attempts: Int, maxAttempts: Int, factor: Double) extends Retryable
final case class Constant private (delay: Duration, attempts: Int, maxAttempts: Int) extends Retryable
final case class RetryFailed private(attempts: Int) extends BackoffStrategy

object BackoffStrategy {
  private val defaultMaxAttempts = 3
  private val defaultExponentialMinimumInterval = 100L
  private val defaultMinimumInterval = 250L
  private val defaultMinimumMultiplierFactor = 2.0

  def exponentialStrategy(delay: Duration, maxAttempts: Int): Exponential = exponential(delay, maxAttempts)
  def doublingStrategy(delay: Duration, maxAttempts: Int): Multiple = multiple(delay, maxAttempts, factor = 2.0)
  def multiplierStrategy(delay: Duration, maxAttempts: Int, multiplier: Double): Multiple = multiple(delay, maxAttempts, multiplier)
  def constantStrategy(delay: Duration, maxAttempts: Int): Constant = constant(delay, maxAttempts)

  private def exponential(
    min: Duration = Duration(defaultExponentialMinimumInterval, TimeUnit.MILLISECONDS),
    maxAttempts: Int = defaultMaxAttempts
  ): Exponential = {
    val ln = Math.max(min.toMillis, defaultExponentialMinimumInterval)
    val mx = if (maxAttempts > 0) maxAttempts else 1
    Exponential(Duration(ln, TimeUnit.MILLISECONDS), 0, mx)
  }

  private def multiple(
    min: Duration = Duration(defaultMinimumInterval, TimeUnit.MILLISECONDS),
    maxAttempts: Int = defaultMaxAttempts,
    factor: Double
  ): Multiple = {
    val ln = Math.max(min.toMillis, defaultMinimumInterval)
    val mx = if (maxAttempts > 0) maxAttempts else 1
    val fc = if (factor < defaultMinimumMultiplierFactor) defaultMinimumMultiplierFactor else factor
    Multiple(Duration(ln, TimeUnit.MILLISECONDS), 0, mx, fc)
  }

  private def constant(
    min: Duration = Duration(defaultMinimumInterval, TimeUnit.MILLISECONDS),
    maxAttempts: Int = defaultMaxAttempts
  ): Constant = {
    val ln = Math.max(min.toMillis, defaultMinimumInterval)
    val mx = if (maxAttempts > 0) maxAttempts else 1
    Constant(Duration(ln, TimeUnit.MILLISECONDS), 0, mx)
  }
}

object Retry {
  type RetryAttempt = Int
  def withRetry[A](backoffStrategy: BackoffStrategy, retryPredicate: A => Boolean)(operation: RetryAttempt => Future[A])(implicit executor: ScheduledExecutor, ec: ExecutionContext): Future[A] = {
    def loop(backoffStrategy: BackoffStrategy): Future[A] = backoffStrategy match {
      case r: Retryable => operation(r.attempts).flatMap {
          case result if retryPredicate(result) => executor.sleepFor(r.delay).flatMap(_ => loop(backoffStrategy.increment))
          case result => Future.successful(result)
        }
      case RetryFailed(attempts) =>
        Future.failed(ContentApiBackoffException(s"Backoff failed after $attempts attempts"))
    }
    loop(backoffStrategy)
  }
}

object HttpRetry {

  private def canRetry(implicit executionContext: ExecutionContext): HttpResponse => Boolean = {
    case IsRecoverableHttpResponse() => true
    case _ => false
  }

  def withRetry(backoffStrategy: BackoffStrategy)(operation: RetryAttempt => Future[HttpResponse])(implicit executor: ScheduledExecutor, ec: ExecutionContext): Future[HttpResponse] =
    Retry.withRetry(backoffStrategy, canRetry)(operation)
}