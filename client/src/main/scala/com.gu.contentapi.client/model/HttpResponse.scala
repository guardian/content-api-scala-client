package com.gu.contentapi.client.model

case class HttpResponse(body: Array[Byte], statusCode: Int, statusMessage: String)

object HttpResponse {
  val failedButMaybeRecoverableCodes = Set(408, 429, 504, 509)
  val successfulResponseCodes = Set(200, 302)

  def isSuccessHttpResponse(httpResponse: HttpResponse) = successfulResponseCodes.contains(httpResponse.statusCode)

  def isRecoverableHttpResponse(httpResponse: HttpResponse) = failedButMaybeRecoverableCodes.contains(httpResponse.statusCode)

}