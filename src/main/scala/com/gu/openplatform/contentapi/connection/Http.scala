package com.gu.openplatform.contentapi.connection

import java.lang.String

import org.apache.commons.httpclient.methods.GetMethod
import java.net.{URL, HttpURLConnection}
import org.apache.commons.httpclient.{MultiThreadedHttpConnectionManager, HttpClient}
import io.{Codec, Source}

case class HttpResponse(body: String, statusCode: Int, statusMessage: String)

trait Http {
  implicit val codec = Codec("UTF-8")
  // this is what the Api client requires of an http connection
  def GET(url: String, headers: Iterable[ (String, String) ] = Nil): HttpResponse
}


// an implementation using apache http client, note this just uses the default connection manager
// and does not support multithreaded use.
trait ApacheHttpClient extends Http {
  val httpClient = new HttpClient

  def setProxy(host: String, port: Int) {
    httpClient.getHostConfiguration().setProxy(host, port)
  }

  def GET(url: String, headers: Iterable[ (String, String) ] = Nil): HttpResponse = {
    val method = new GetMethod(url)
    try {

      headers.foreach { case (k, v) => method.addRequestHeader(k, v) }

      httpClient.executeMethod(method)

      val statusLine = method getStatusLine
      val responseBody = Option(method.getResponseBodyAsStream)
              .map(Source.fromInputStream(_).mkString)
              .getOrElse("")

      new HttpResponse(responseBody, statusLine.getStatusCode, statusLine.getReasonPhrase)
    } finally {
      method.releaseConnection
    }
  }
}

// an implementation using the MultiThreadedHttpConnectionManager
// note this defaults to 2 connections in accordance to the http spec
// feel free to up this number by calling maxConnections
trait MultiThreadedApacheHttpClient extends ApacheHttpClient {
  val connectionManager = new MultiThreadedHttpConnectionManager
  override val httpClient = new HttpClient(connectionManager)

  def maxConnections(i: Int) {
    connectionManager.getParams.setMaxTotalConnections(i)
    connectionManager.getParams.setDefaultMaxConnectionsPerHost(i)
  }

  def setConnectionTimeout(seconds: Int) {
    connectionManager.getParams.setConnectionTimeout(seconds * 1000)
  }

  def setSocketTimeout(seconds: Int) {
    connectionManager.getParams.setSoTimeout(seconds * 1000)
  }
}


// an implementation using java.net
trait JavaNetHttp extends Http {
  def GET(urlString: String, headers: Iterable[ (String, String) ] = Nil): HttpResponse = {

    val connection = new URL(urlString).openConnection.asInstanceOf[HttpURLConnection]
    headers.foreach { case (k, v) => connection.setRequestProperty(k, v) }

    val src = Source.fromInputStream(connection.getInputStream)
    val responseBody = src.mkString
    src.close

    new HttpResponse(responseBody, connection.getResponseCode, connection.getResponseMessage)
  }

}


