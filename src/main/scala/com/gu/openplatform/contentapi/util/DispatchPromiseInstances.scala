package com.gu.openplatform.contentapi.util

import dispatch.{Http, Promise}
import com.gu.openplatform.contentapi.ApiError

object DispatchPromiseInstances {

  implicit val promiseInstance: Monad[Promise] = new Monad[Promise] {

    def point[A](a: A) = Http.promise(a)

    def bind[A, B](f: (A) => Promise[B]) = _ flatMap f

    def fail[A](error: ApiError) = Http.promise(throw error)
  }

}
