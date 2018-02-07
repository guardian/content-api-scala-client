package com.gu.computation

trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

trait Apply[F[_]] extends Functor[F] {
  def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]
}

trait Applicative[F[_]] extends Apply[F] {
  def pure[A](a: A): F[A]
}

trait FlatMap[F[_]] extends Apply[F] {
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
}

trait Monad[M[_]] extends Applicative[M] with FlatMap[M] {}

trait MonadError[M[_], E] extends Monad[M] {
  def throwError[A](e: E): M[A]

  def catchError[A](ma: M[A])(f: E => M[A]): M[A]
}

trait Semigroup[S] {
  def append(s1: S, s2: S): S
}

trait Monoid[M] extends Semigroup[M] {
  def mempty: M
}