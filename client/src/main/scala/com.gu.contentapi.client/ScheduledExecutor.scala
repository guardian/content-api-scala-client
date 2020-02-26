package com.gu.contentapi.client

import java.util.concurrent.{Executors, RejectedExecutionException, ScheduledExecutorService}

import scala.concurrent.{Future, Promise}
import scala.concurrent.duration.Duration
import scala.language.implicitConversions

object ScheduledExecutor {
  def apply(): ScheduledExecutor = {
    new ScheduledExecutor {
      private lazy val underlying: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
      override def sleepFor(napTime: Duration): Future[Unit] = {
        val promise = Promise[Unit]()
        val runnable = new Runnable {
          override def run(): Unit = promise.success(())
        }
        underlying.schedule(runnable, napTime.length, napTime.unit)
        promise.future
      }
    }
  }
}

/**
  * A single threaded executor for tasks scheduled in the future
  * @throws IllegalArgumentException if { @code corePoolSize < 0}
  * @throws NullPointerException if { @code threadFactory} or
  *                                         { @code handler} is null
  */
abstract class ScheduledExecutor {

  /**
    * Creates a Future and schedules the operation to run after the given delay.
    *
    * @param napTime duration for which to delay execution
    * @return a Future to capture the signal that napTime is over
    * @throws RejectedExecutionException if the task cannot be
    *                                    scheduled for execution
    */

  def sleepFor(napTime: Duration): Future[Unit]

}
