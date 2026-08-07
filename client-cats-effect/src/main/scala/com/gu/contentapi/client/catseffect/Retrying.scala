package com.gu.contentapi.client.catseffect

import cats.effect.IO
import cats.effect.std.Random
import org.typelevel.log4cats.SelfAwareStructuredLogger

import scala.concurrent.duration._

object Retrying {

  /** Log:
   *   - Initial failure
   *   - Final failure
   *   - Success after a failure
   *
   * To reduce log noise, don't log failures after initial failure, unless it was the last attempt.
   */
  def retry[A](
    io: IO[A],
    desc: String,
    detail: String,
    errorAnalyzer: PartialFunction[Throwable, String],
    retries: Int
  )(implicit random: Random[IO], l: SelfAwareStructuredLogger[IO]): IO[A] = for {
    seriesId <- random.nextLong
    result <- {
      val logger = l.withModifiedString(s => s"$desc: $s").addContext(Map("retry.series.id" -> seriesId.toString))

      def loop(n: Int): IO[A] =
        io.flatTap(_ =>
          IO.whenA(n > 0)(logger.info(s"Made ${n + 1}/$retries attempts before success"))
        ).handleErrorWith { e =>
          if (n >= retries)
            logger.error(e)(s"All $retries retries failed ($detail): ${errorAnalyzer(e)}") >>
              IO.raiseError(e)
          else
            for {
              _ <- IO.whenA(n == 0)(logger.warn(e)(s"Initial attempt (out of $retries) failed: ${errorAnalyzer(e)}"))
              baseWaitingTime = 100L << n
              jitter <- random.betweenLong(0L, baseWaitingTime)
              result <- IO.sleep((baseWaitingTime + jitter).millis) >> loop(n + 1)
            } yield result
        }

      logger.info("Does this work") >> loop(0)
    }
  } yield result

}
