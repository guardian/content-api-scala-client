package com.gu.contentapi.client.catseffect

import cats.effect.std.Semaphore
import cats.effect.{FiberIO, IO}

import scala.concurrent.duration.FiniteDuration

final class TokenBucket private (
    sem: Semaphore[IO],
    capacity: Long,
    refillAmount: Long,
    refillPeriod: FiniteDuration
) {
  def enforceWithDelay: IO[Unit] = sem.acquire

  private val refillLoop: IO[Unit] =
    (IO.sleep(refillPeriod) >> sem.releaseN(refillAmount.min(capacity))).foreverM

  private def start: IO[FiberIO[Unit]] = refillLoop.start
}

object TokenBucket {
  def create(capacity: Int, refillAmount: Int, refillPeriod: FiniteDuration): IO[TokenBucket] = for {
    sem <- Semaphore[IO](capacity.toLong)
    tb = new TokenBucket(sem, capacity.toLong, refillAmount.toLong, refillPeriod)
    _ <- tb.start
  } yield tb
}
