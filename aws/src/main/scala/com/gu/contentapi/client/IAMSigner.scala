package com.gu.contentapi.client

import java.net.URI

import com.amazonaws.DefaultRequest
import com.amazonaws.auth.{AWS4Signer, AWSCredentialsProvider}
import com.amazonaws.http.HttpMethodName

import collection.JavaConverters._

/**
  * For api-gateway authorization.
  */
class IAMSigner(credentialsProvider: AWSCredentialsProvider, awsRegion: String) {

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
    * @param uri      Request URI, including params
    * @return         Updated set of headers, including the authorisation headers
    */
  def addIAMHeaders(headers: Map[String, String], uri: URI): Map[String, String] = {
    val requestToSign = {
      val req = new DefaultRequest(serviceName)

      //api-gateway will break the compressed json response if we don't supply an accept header
      val headersWithAccept =
        if (headers.contains("accept") || headers.contains("Accept")) headers
        else headers + ("accept" -> "application/json")

      req.setHeaders(headersWithAccept.asJava)
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

    signer.sign(requestToSign, credentialsProvider.getCredentials)
    requestToSign.getHeaders.asScala.toMap
  }
}
