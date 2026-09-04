package com.gu.contentapi.client.utils

import java.net.URLEncoder

object QueryStringParams {
  def apply(parameters: Iterable[(String, String)]) = {
    /**
      * api-gateway IAM authorisation requires that spaces are encoded as `%20`, not `+`.
      * https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
      */
    def encodeParameter(p: String): String = URLEncoder.encode(p, "UTF-8").replace("+", "%20")

    if (parameters.isEmpty) {
      ""
    } else "?" + (parameters map {
      case (k, v) => k + "=" + encodeParameter(v)
    } mkString "&")
  }
}
