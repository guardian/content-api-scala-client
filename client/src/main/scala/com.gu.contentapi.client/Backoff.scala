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
  def apply(min: Duration, maxAttempts: Int = 3, factor: Double = 2): Backoff =
    Exponential(min, 1, maxAttempts, factor)
}
