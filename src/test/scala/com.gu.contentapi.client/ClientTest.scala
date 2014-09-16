package com.gu.contentapi.client

import scala.io.Source

trait ClientTest {

  def loadJson(filename: String): String = {
    Source.fromFile("src/test/resources/templates/" + filename).mkString
  }

}
