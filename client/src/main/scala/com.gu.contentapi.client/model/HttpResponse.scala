package com.gu.contentapi.client.model

import scala.concurrent.Future

case class HttpResponse(body: Array[Byte], statusCode: Int, statusMessage: String)

object HttpResponse {
  val isSuccessful = Set(200, 302)
  val failedButMaybeRecoverable = Set(408, 429, 503, 504, 509)

  private def genException(statusCode: Int): Exception = {
    statusCode match {
      case e: Int if e == 408 => ContentApiRecoverableException(statusCode, "Request Timeout")
      case e: Int if e == 429 => ContentApiRecoverableException(statusCode, "Too Many Requests")
      case e: Int if e == 503 => ContentApiRecoverableException(statusCode, "Service Unavailable")
      case e: Int if e == 504 => ContentApiRecoverableException(statusCode, "Gateway Timeout")
      case e: Int if e == 509 => ContentApiRecoverableException(statusCode, "Bandwidth Limit Exceeded")
      case e => new Exception(s"$e - Unhandled Exception")
    }
  }

  def check: HttpResponse => Future[Array[Byte]] = {
    case HttpResponse(body, statusCode, _) if isSuccessful(statusCode) => Future.successful(body)
    case HttpResponse(_, statusCode, _) if failedButMaybeRecoverable(statusCode) => throw genException(statusCode)
    case response => Future.failed(ContentApiError(response))
  }
}