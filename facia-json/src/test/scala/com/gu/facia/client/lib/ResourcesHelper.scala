package com.gu.facia.client.lib

import com.gu.facia.client.FaciaSuccess
import org.apache.commons.io.IOUtils

import java.nio.charset.StandardCharsets.UTF_8

trait ResourcesHelper {
  def slurpBytes(path: String): Option[Array[Byte]] =
    Option(getClass.getClassLoader.getResource(path)).map(url => IOUtils.toByteArray(url.openStream()))

  def slurp(path: String): Option[String] =
    slurpBytes(path).map(bytes => new String(bytes, UTF_8))

  def slurpOrDie(path: String) =
    slurpBytes(path).map(FaciaSuccess.apply).getOrElse {
      throw new RuntimeException(s"Required resource $path not on class path")
    }
}
