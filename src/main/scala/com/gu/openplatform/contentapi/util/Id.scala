package com.gu.openplatform.contentapi.util

import com.gu.openplatform.contentapi.ApiError

/** Identity monad
  */
case class Id[A](run: A)

object Id extends IdInstances

trait IdInstances {
  implicit def any2Id[A](a: A): Id[A] = Id(a)
  implicit def runId[A](fa: Id[A]): A = fa.run

  implicit val idMonad: Monad[Id] = new Monad[Id] {
    def point[A](a: A) = Id(a)
    def bind[A, B](f: A => Id[B]) = id => f(id.run).run
    def fail[A](error: ApiError) = throw error
  }
}
