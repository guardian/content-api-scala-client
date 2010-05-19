package com.gu.openplatform.contentapi.connection

import io.Source
import java.lang.String

import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.HttpClient

object Http {
  val httpClient = new HttpClient

  def GET(url: String): HttpResponse = {
    Console.err.println("Getting: " + url)

    val method = new GetMethod(url)
    method.setFollowRedirects(false)

    httpClient.executeMethod(method)

    val statusLine = method getStatusLine
    val responseBody = Source.fromInputStream(method.getResponseBodyAsStream).mkString

    method.releaseConnection

    if (List(200, 302) contains statusLine.getStatusCode) {
      new HttpResponse(responseBody, statusLine.getStatusCode)
    } else {
      Console.err.println(" => response '%s'" format statusLine)
      throw new ApiError(statusLine.getStatusCode, statusLine.getReasonPhrase)
    }
  }
}

class HttpResponse(val body: String, val status: Int)