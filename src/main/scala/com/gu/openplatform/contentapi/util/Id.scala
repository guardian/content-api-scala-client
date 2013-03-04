package com.gu.openplatform.contentapi.util

import com.gu.openplatform.contentapi.ApiError

/** Identity monad
  */
case class Id[A](runIdentity: A)

object Id extends IdInstances {
  implicit def runId[A](fa: Id[A]): A = fa.runIdentity
}

trait IdInstances {

  implicit val idMonad: Monad[Id] = new Monad[Id] {
    def point[A](a: A) = Id(a)
    def bind[A, B](f: A => Id[B]) = id => f(id.runIdentity)
    def fail[A](error: ApiError) = throw error
  }
}
