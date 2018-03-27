package com.gu.contentapi.client.model

import com.gu.contentapi.client.model.v1.ErrorResponse
import com.gu.contentapi.client.thrift.ThriftDeserializer
import scala.util.Try

case class ContentApiError(httpStatus: Int, httpMessage: String, errorResponse: Option[ErrorResponse] = None) extends Exception(httpMessage)

object ContentApiError {
  def apply(response: HttpResponse): ContentApiError = {
    val errorResponse = Try(ThriftDeserializer.deserialize(response.body, ErrorResponse)).toOption
    ContentApiError(response.statusCode, response.statusMessage, errorResponse)
  }
}

