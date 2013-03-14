package com.gu.openplatform.contentapi.util

import concurrent.{ExecutionContext, Future}
import com.gu.openplatform.contentapi.ApiError

object FutureInstances {

  implicit def futureMonad(implicit ex: ExecutionContext): Monad[Future] = new Monad[Future] {

    def point[A](a: A) = Future.successful(a)

    override def map[A, B](f: A => B) = _ map f

    def bind[A, B](f: A => Future[B]) = _ flatMap f

    def fail[A](error: ApiError) = Future.failed(error)
  }

}
