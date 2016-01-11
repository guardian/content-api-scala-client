package com.gu.contentapi.client

import java.nio.charset.StandardCharsets

import com.google.common.io.Resources

trait ClientTest {

  val api = new GuardianContentClient("test")

  def loadJson(filename: String): String = {
    Resources.toString(Resources.getResource(s"templates/$filename"), StandardCharsets.UTF_8)
  }

}
