package com.gu.contentapi.client

import scala.concurrent.{Future, ExecutionContext}

class FakeHttpClient(responses: Map[String, HttpResponse], default: HttpResponse) extends HttpClient {

  override def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext) =
    Future.successful(responses.getOrElse(url, default))
}