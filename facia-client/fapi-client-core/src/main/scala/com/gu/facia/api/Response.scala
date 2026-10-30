package com.gu.facia.api

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

case class Response[+A] protected (underlying: Future[Either[ApiError, A]]) {
  def map[B](f: A => B)(implicit ec: ExecutionContext): Response[B] =
    flatMap(a => Response.Right(f(a)))

  def flatMap[B](
      f: A => Response[B]
  )(implicit ec: ExecutionContext): Response[B] = Response {
    asFuture.flatMap {
      case scala.util.Right(a) => f(a).asFuture
      case scala.Left(e)       => Future.successful(scala.Left(e))
    }
  }

  def withFilter(f: A => Boolean, orLeft: ApiError = FilteredOut)(implicit
      ex: ExecutionContext
  ): Response[A] = flatMap { a =>
    if (f(a))
      Response.Right(a)
    else
      Response.Left(orLeft)
  }
  def filter(f: A => Boolean, orLeft: ApiError = FilteredOut)(implicit
      ex: ExecutionContext
  ): Response[A] = withFilter(f, orLeft)

  def fold[B](failure: ApiError => B, success: A => B)(implicit
      ec: ExecutionContext
  ): Future[B] = {
    asFuture.map(_.fold(failure, success))
  }

  def mapError(
      pf: ApiError => ApiError
  )(implicit ec: ExecutionContext): Response[A] = Response {
    fold(err => Left(pf(err)), Right(_))
  }

  def recover[A1 >: A](
      pf: ApiError => A1
  )(implicit ec: ExecutionContext): Response[A1] = Response {
    fold(err => Right(pf(err)), Right(_))
  }

  def asFuture(implicit ec: ExecutionContext) = {
    underlying recover { case err =>
      val apiError = Unexpected(err.getMessage, Some(err))
      scala.Left(apiError)
    }
  }
}
object Response {
  def Right[A](a: A): Response[A] =
    Response(Future.successful(scala.Right(a)))

  def Left[A](err: ApiError): Response[A] =
    Response(Future.successful(scala.Left(err)))

  def fromOption[A](optA: Option[A], orLeft: ApiError): Response[A] =
    optA.map(a => Right(a)).getOrElse(Left(orLeft))

  object Async {
    def Right[A](fa: Future[A])(implicit ec: ExecutionContext): Response[A] =
      Response(fa.map(scala.Right(_)))

    def Left[A](ferr: Future[ApiError])(implicit
        ec: ExecutionContext
    ): Response[A] =
      Response(ferr.map(scala.Left(_)))
  }

  /** Collects responses together, or fails with the first error encountered
    */
  def traverse[A](
      responses: List[Response[A]]
  )(implicit ec: ExecutionContext): Response[List[A]] = Response {
    Future.traverse(responses)(_.asFuture).flatMap { eithers =>
      @tailrec
      def loop(rs: List[Either[ApiError, A]], acc: List[A]): Response[List[A]] = {
        if (rs.isEmpty) Response.Right(acc.reverse)
        else
          rs.head match {
            case Left(apiErr) => Response.Left(apiErr)
            case Right(a)     => loop(rs.tail, a :: acc)
          }
      }
      loop(eithers, Nil).asFuture
    }
  }
}

sealed trait ApiError {
  def message: String
  def cause: Option[Throwable]
}
case class NotFound(
    message: String = "Not found",
    cause: Option[Throwable] = None
) extends ApiError
case class Unexpected(message: String, cause: Option[Throwable] = None) extends ApiError
case class JsonError(message: String, cause: Option[Throwable] = None) extends ApiError
case class DataError(message: String, cause: Option[Throwable] = None) extends ApiError
case class CapiError(message: String, cause: Option[Throwable] = None) extends ApiError
case class HttpError(message: String, cause: Option[Throwable] = None) extends ApiError
case class UrlConstructError(message: String, cause: Option[Throwable] = None) extends ApiError
case object FilteredOut extends ApiError {
  def message = "Filtered out"
  def cause = None
}
