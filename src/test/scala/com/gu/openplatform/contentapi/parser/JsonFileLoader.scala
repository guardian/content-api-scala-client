package com.gu.openplatform.contentapi.parser

import io.Source

object JsonFileLoader {
  def loadFile(filename: String) = {
    Option(getClass.getResourceAsStream(filename))
            .map(Source.fromInputStream(_, "UTF-8").mkString)
            .getOrElse(error("could not load file " + filename))
  }

}