package com.gu.openplatform.contentapi.util

import com.gu.openplatform.contentapi.ApiError

object IdInstances {

  implicit val idMonad: Monad[Id] = new Monad[Id] {
    def point[A](a: A) = a
    def bind[A, B](f: A => Id[B]) = f
    def fail[A](error: ApiError) = throw error
  }
}
