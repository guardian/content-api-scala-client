package com.gu.contentapi.client.utils

import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8

object QueryStringParams {
  /**
   * api-gateway IAM authorisation requires that spaces are encoded as `%20`, not `+`.
   * https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
   */
  def encodeParameter(p: String): String = URLEncoder.encode(p, UTF_8).replace("+", "%20")

  def apply(parameters: Iterable[(String, String)]) =
    if (parameters.isEmpty) {
      ""
    } else "?" + (parameters map {
      case (k, v) => k + "=" + encodeParameter(v)
    } mkString "&")
}
