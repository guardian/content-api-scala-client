package com.gu.contentapi.client.model

import scala.concurrent.Future

case class HttpResponse(body: Array[Byte], statusCode: Int, statusMessage: String)

object HttpResponse {
  val isSuccessful = Set(200, 302)
  val failedButMaybeRecoverable = Set(408, 429, 503, 504, 509)

  def check: HttpResponse => Future[HttpResponse] = {
    case r @ HttpResponse(_, statusCode, _) if isSuccessful(statusCode) => Future.successful(r)
    case HttpResponse(_, statusCode, statusMessage) if failedButMaybeRecoverable(statusCode) =>
      Future.failed(ContentApiRecoverableException(statusCode, statusMessage))
    case response => Future.failed(ContentApiError(response))
  }
}