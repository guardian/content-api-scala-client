package com.gu.contentapi.client

import java.util.concurrent.TimeUnit

import com.gu.contentapi.client.model.{ ContentApiRecoverableException, HttpResponse }

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

sealed abstract class Backoff extends Product with Serializable { self =>
  val scheduledExecutor = new ScheduledExecutor(1)

  def state: Backoff = self match {
    case Exponential(_, n, max, _) if n == max => Failed(max)
    case Exponential(_, n, max, f) =>
      val delay = Duration(Math.pow(2, n) * 250, TimeUnit.MILLISECONDS)
      Exponential(delay, n + 1, max, f)

    case x => x
  }

  def retry(operation: ⇒ Future[HttpResponse])(implicit context: ExecutionContext): Future[HttpResponse] = {
    self.state match {
      case exp: Exponential =>
        scheduledExecutor.sleepFor(exp.delay)
          .flatMap { _ => operation }
          .recoverWith {
            case r: ContentApiRecoverableException => retry(operation)
          }

      case f: Failed => Future.failed(new Exception(s"Retry failed after ${f.attempts} retries"))
    }
  }

}

final case class Exponential private (delay: Duration, attempts: Int, maxAttempts: Int, factor: Double) extends Backoff
final case class Failed private (attempts: Int) extends Backoff

object Backoff {
  // 1. make it as easy as possible for clients: supply default values for all params
  //    the only new code they need to supply in their implementation of ContentApiClient is
  //    `override val backoffStrategy: Backoff = Backoff()` or similar
  def apply(min: Duration = Duration(250, TimeUnit.MILLISECONDS), maxAttempts: Int = 3, factor: Double = 2): Backoff = {
    // 2. if clients supply their own values, enforce some sensible min/max limits
    //    e.g. min. wait time 250ms, multiplier (factor) minimum of 2 and no more than 10 retries (maxAttempts)
    val mx = if (maxAttempts > 10) 10 else maxAttempts
    val fc = if (factor < 2) 2 else factor
    val ln = if (min.toMillis < 250) 250 else min.toMillis

    Exponential(Duration(ln, TimeUnit.MILLISECONDS), 1, mx, fc)
  }
}
