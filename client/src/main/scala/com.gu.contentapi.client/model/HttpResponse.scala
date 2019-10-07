package com.gu.contentapi.client.model

import scala.concurrent.Future

case class HttpResponse(body: Array[Byte], statusCode: Int, statusMessage: String)

object HttpResponse {
  val isSuccessful = Set(200, 302)
  val failedButMaybeRecoverable = Set(408, 429, 503, 504, 509)
  val recoverableErrorMessages: List[(Int, String)] = List(
    (408 -> "Request Timeout"),
    (429 -> "Too Many Requests"),
    (503 -> "Service Unavailable"),
    (504 -> "Gateway Timeout"),
    (509 -> "Bandwidth Limit Exceeded")
  )

  private def genExceptionMessage(statusCode: Int): String = {
    recoverableErrorMessages.find(_._1 == statusCode).map(_._2).getOrElse(s"Unexpected HTTP Code: $statusCode")
  }

  def check: HttpResponse => Future[Array[Byte]] = {
    case HttpResponse(body, statusCode, _) if isSuccessful(statusCode) => Future.successful(body)
    case HttpResponse(_, statusCode, _) if failedButMaybeRecoverable(statusCode) =>
      throw ContentApiRecoverableException(statusCode, genExceptionMessage(statusCode))
    case response => Future.failed(ContentApiError(response))
  }
}