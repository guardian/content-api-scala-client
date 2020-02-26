package com.gu.contentapi.client.model

case class HttpResponse(body: Array[Byte], statusCode: Int, statusMessage: String)

object HttpResponse {
  val failedButMaybeRecoverable = Set(408, 429, 503, 504, 509)
  val isSuccessful = Set(200, 302)

  object IsSuccessHttpResponse {
    def unapply(response: HttpResponse): Boolean = isSuccessful.contains(response.statusCode)
  }

  object IsRecoverableHttpResponse {
    def unapply(response: HttpResponse): Boolean = failedButMaybeRecoverable.contains(response.statusCode)
  }

}