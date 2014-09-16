package com.gu.contentapi.client

import scala.io.Source
import com.gu.contentapi.client.connection.DispatchAsyncHttp

trait ClientTest {

  val api = new Api with DispatchAsyncHttp {
    override val apiKey = Some("none")
  }

  def loadJson(filename: String): String = {
    Source.fromFile("src/test/resources/templates/" + filename).mkString
  }

}
