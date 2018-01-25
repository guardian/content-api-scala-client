package com.gu.contentapi.client

import java.net.URLEncoder

/**
  * Requests to an IAM-authorised api-gateway must conform to AWS sig4v specification
  */
object IAMEncoder {

  private def encode(v: String) = URLEncoder.encode(v, "UTF-8").replace("+", "%20")

  /**
    * URL query params must be encoded correctly for the api-gateway authorisation to work.
    * Note - the URL passed to `IAMSigner.addIAMHeaders` must match the URL of the request.
    *
    * @param params Map of query params
    * @return Query params string
    */
  def encodeParams(params: Map[String, Seq[String]]): String = {
    params.map { case (key, value) =>
      s"${encode(key)}=${encode(value.mkString(","))}"
    }.mkString("&")
  }

  /**
    * URL query params must be encoded correctly for the api-gateway authorisation to work.
    * Note - the URL passed to `IAMSigner.addIAMHeaders` must match the URL of the request.
    *
    * @param params Query params string
    * @return Query params string
    */
  def encodeParams(params: String): String =
    params.split("&")
      .map(_.split("="))
      .collect { case Array(k, v) => s"${encode(k)}=${encode(v)}" }
      .mkString("&")
}
