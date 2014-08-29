package com.gu.contentapi.client.parser

import scala.io.Source

object JsonFileLoader {

  def loadFile(filename: String): String = {
    Source.fromFile("src/test/resources/templates/" + filename).mkString
  }

}
