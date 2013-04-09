package com.gu.openplatform.contentapi.util

import dispatch.{Http, Promise}
import com.gu.openplatform.contentapi.ApiError
import concurrent.{Future, ExecutionContext}

object DispatchPromiseInstances {

  implicit def promiseMonad(implicit ctx: ExecutionContext): Monad[Promise] = new Monad[Promise] {

    def point[A](a: A) = Http.promise(a)

    override def map[A, B](f: A => B) = _ map f

    def bind[A, B](f: A => Promise[B]) = _ flatMap f

    def fail[A](error: ApiError): Promise[A] = Future.failed(error)
  }

}
