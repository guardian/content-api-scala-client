package com.gu.contentapi.client

import com.amazonaws.DefaultRequest
import com.amazonaws.auth.{AWS4Signer, AWSCredentials}
import com.amazonaws.http.HttpMethodName
import collection.JavaConverters._

/**
  * For api-gateway authorization.
  */
class IAMSigner(credentials: AWSCredentials, awsRegion: String) {

  private val serviceName = "execute-api"

  private val signer = {
    val sig = new AWS4Signer()
    sig.setRegionName(awsRegion)
    sig.setServiceName(serviceName)
    sig
  }

  /**
    * Returns the given set of headers, updated to include AWS sig4v signed headers based on the request and the credentials
    *
    * @param headers  Current set of request headers
    * @param url      Request URL, including params
    * @return         Updated set of headers, including the authorisation headers
    */
  def addIAMHeaders(headers: Map[String, String], url: String): Map[String, String] = {
    val requestToSign = {
      val req = new DefaultRequest(serviceName)

      val uri = new java.net.URI(url)
      req.setHeaders(headers.asJava)
      req.setEndpoint(new java.net.URI(s"${uri.getScheme}://${uri.getHost}"))
      req.setHttpMethod(HttpMethodName.GET)
      req.setResourcePath(uri.getPath)

      if (uri.getQuery != null) {
        req.setParameters(uri.getQuery.split("&").toList.flatMap { s =>
          s.split("=").toList match {
            case k :: (v :: Nil) => Some(k -> List(v).asJava)
            case _ => None
          }
        }.toMap.asJava)
      }
      req
    }

    signer.sign(requestToSign, credentials)
    requestToSign.getHeaders.asScala.toMap + ("Accept-Encoding" -> "identity")
  }
}
