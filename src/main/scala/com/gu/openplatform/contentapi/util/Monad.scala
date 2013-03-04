package com.gu.openplatform.contentapi.util

import com.gu.openplatform.contentapi.ApiError

/** Monad trait with failure specialized to ApiError
  */
trait Monad[F[_]] {
  def point[A](a: A): F[A]
  def bind[A, B](f: A => F[B]): F[A] => F[B]
  def fail[A](error: ApiError): F[A]
}

final class MonadOps[M[_], A](ma: M[A])(implicit M: Monad[M]) {
  def map[B](f: A => B): M[B] = M.bind(f andThen M.point)(ma)
  def flatMap[B](f: A => M[B]): M[B] = M.bind(f)(ma)
}

object MonadOps {

  implicit def monadOps[M[_]:Monad, A](ma: M[A]): MonadOps[M, A] = new MonadOps(ma)

  def point[M[_], A](a: A)(implicit M: Monad[M]): M[A] = M.point(a)
  def fail[M[_], A](error: ApiError)(implicit M: Monad[M]): M[A] = M.fail(error)
}
