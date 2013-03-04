package com.gu.openplatform.contentapi.util

import com.gu.openplatform.contentapi.ApiError

/** Identity wrapper, which gives rise to a trivial Monad instance
  */
case class Id[A](runId: A)

object Id extends IdInstances {
  implicit def runId[A](fa: Id[A]): A = fa.runId
}

trait IdInstances {

  implicit val idMonad: Monad[Id] = new Monad[Id] {
    def point[A](a: A) = Id(a)
    def bind[A, B](f: A => Id[B]) = id => f(id.runId)
    def fail[A](error: ApiError) = throw error
  }
}
