package com.gu.contentapi.client.catseffect

import cats.effect.IO
import cats.effect.std.Random

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
  )(implicit random: Random[IO]): IO[A] =  io

//  {
//    def loop(n: Int): IO[A] =
//      io.flatTap(_ =>
//        IO.whenA(n > 0)(IO(logger.info(s"$desc: Made ${n + 1}/$retries attempts before success")))
//      ).handleErrorWith { e =>
//        if (n >= retries)
//          IO(logger.error(s"$desc: All $retries retries failed ($detail): ${errorAnalyzer(e)}", e)) >>
//            IO.raiseError(e)
//        else
//          for {
//            _ <- IO.whenA(n == 0)(
//              IO(logger.warn(s"$desc: Initial attempt (out of $retries) failed: ${errorAnalyzer(e)}", e))
//            )
//            baseWaitingTime = 100L << n
//            jitter <- random.betweenLong(0L, baseWaitingTime)
//            result <- IO.sleep((baseWaitingTime + jitter).millis) >> loop(n + 1)
//          } yield result
//      }
//
//    loop(0)
//  }
}
