package com.gu.contentapi.client.utils

import java.net.URLEncoder

object QueryStringParams {
  def apply(parameters: Iterable[(String, String)]) = {
    def encodeParameter(p: String): String = p match {
      case other => URLEncoder.encode(other, "UTF-8")
    }

    if (parameters.isEmpty) {
      ""
    } else "?" + (parameters map {
      case (k, v) => k + "=" + encodeParameter(v)
    } mkString "&")
  }
}