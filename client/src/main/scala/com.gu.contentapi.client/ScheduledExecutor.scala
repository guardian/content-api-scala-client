package com.gu.contentapi.client

import java.util.concurrent.ThreadPoolExecutor.AbortPolicy
import java.util.concurrent.{
  ThreadFactory,
  Executors,
  ScheduledThreadPoolExecutor,
  ScheduledExecutorService,
  RejectedExecutionHandler,
  RejectedExecutionException
}

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.Promise
import scala.language.implicitConversions

object ScheduledExecutor {
  private val defaultHandler: RejectedExecutionHandler = new AbortPolicy
}

/**
  * A thread pool backed executor for tasks scheduled in the future
  * @param corePoolSize the number of threads to keep in the pool, even
  *                     if they are idle, unless { @code allowCoreThreadTimeOut} is set
  * @param threadFactory the factory to use when the executor
  *                      creates a new thread
  * @param handler the handler to use when execution is blocked
  *                because the thread bounds and queue capacities are reached
  * @throws IllegalArgumentException if { @code corePoolSize < 0}
  * @throws NullPointerException if { @code threadFactory} or
  *                                         { @code handler} is null
  */
class ScheduledExecutor(corePoolSize: Int,
                        threadFactory: ThreadFactory = Executors.defaultThreadFactory,
                        handler: RejectedExecutionHandler = ScheduledExecutor.defaultHandler) {

  private val underlying: ScheduledExecutorService = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory, handler)

  /**
    * Creates a Future and schedules the operation to run after the given delay.
    *
    * @param napTime duration for which to delay execution
    * @return a Future to capture the signal that napTime is over
    * @throws RejectedExecutionException if the task cannot be
    *                                    scheduled for execution
    */

  def sleepFor(napTime: Duration): Future[Unit] = {
    val promise = Promise[Unit]()
    val runnable = new Runnable {
      override def run(): Unit = promise.success(())
    }
    underlying.schedule(runnable, napTime.length, napTime.unit)
    promise.future
  }

}
