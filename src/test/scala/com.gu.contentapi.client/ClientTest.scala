package com.gu.contentapi.client

import scala.io.Source

trait ClientTest {

  val api = new Api {
    override val apiKey = Some("test")
  }

  def loadJson(filename: String): String = {
    Source.fromFile("src/test/resources/templates/" + filename).mkString
  }

}
