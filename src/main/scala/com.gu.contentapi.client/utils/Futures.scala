package com.gu.contentapi.client.utils

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Futures {
  def fromTry[A](t: Try[A]): Future[A] = t match {
    case Success(a) => Future.successful(a)
    case Failure(error) => Future.failed(error)
  }
}
