package com.gu.contentapi.client.model

import scala.concurrent.Future

case class HttpResponse(body: Array[Byte], statusCode: Int, statusMessage: String)

object HttpResponse {
  val isSuccessful = Set(200, 302) 
  
  def check: HttpResponse => Future[Array[Byte]] = {
    case HttpResponse(body, statusCode, _) if isSuccessful(statusCode) => Future.successful(body)
    case response => Future.failed(ContentApiError(response))
  }
}