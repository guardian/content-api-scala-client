package com.gu.contentapi.client

import scala.concurrent.{Future, ExecutionContext}
import scala.io.Source

trait ClientTest {

  val api = new GuardianContentClient(
    apiKey = "test",
    httpClient = new FakeHttpClient(ExampleResponses.responses, ExampleResponses.notFoundResponse)
  )

  def loadJson(filename: String): String = {
    Source.fromFile("src/test/resources/templates/" + filename).mkString
  }

}
