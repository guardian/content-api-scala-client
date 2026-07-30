package com.gu.contentapi.client.catseffect

import cats.effect.std.Semaphore
import cats.effect.{FiberIO, IO}

import scala.concurrent.duration.FiniteDuration

/**
 * Token buckets can be used for rate-limiting - so we can throttle the rate of our requests to the
 * CAPI service.
 *
 * [[https://en.wikipedia.org/wiki/Token_bucket]]
 */
final class TokenBucket private (
    sem: Semaphore[IO],
    capacity: Long,
    refillAmount: Long,
    refillPeriod: FiniteDuration
) {
  def enforceWithDelay: IO[Unit] = sem.acquire

  private val refillLoop: IO[Unit] =
    (IO.sleep(refillPeriod) >> sem.available.flatMap { available =>
      val maximumAddablePermits = (capacity - available).max(0)
      sem.releaseN(refillAmount min maximumAddablePermits)
    }).foreverM

  private def start: IO[FiberIO[Unit]] = refillLoop.start
}

object TokenBucket {
  def create(capacity: Int, refillAmount: Int, refillPeriod: FiniteDuration): IO[TokenBucket] = for {
    sem <- Semaphore[IO](capacity.toLong)
    tb = new TokenBucket(sem, capacity.toLong, refillAmount.toLong, refillPeriod)
    _ <- tb.start
  } yield tb
}
