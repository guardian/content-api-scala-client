package com.gu.openplatform.contentapi.connection

import java.lang.String

import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.HttpClient
import com.gu.openplatform.contentapi.ApiError
import java.net.{URL, HttpURLConnection}
import io.{Codec, Source}

case class HttpResponse(body: String, statusCode: Int, statusMessage: String)

trait Http {
  // this is what the Api client requires of an http connection
  def GET(url: String, headers: Iterable[ (String, String) ] = Nil): HttpResponse
}


// an implementtation using apache http client
trait ApacheHttpClient extends Http {
  var httpClient = new HttpClient

  def GET(url: String, headers: Iterable[ (String, String) ] = Nil): HttpResponse = {
    
    val method = new GetMethod(url)

    headers.foreach { case (k, v) => method.addRequestHeader(k, v) }

    httpClient.executeMethod(method)

    val statusLine = method getStatusLine
    val responseBody = Option(method.getResponseBodyAsStream)
            .map(Source.fromInputStream(_).mkString)
            .getOrElse("")

    method.releaseConnection

    new HttpResponse(responseBody, statusLine.getStatusCode, statusLine.getReasonPhrase)
  }
}


// an implementation using java.net
trait JavaNetHttp extends Http {
  def GET(urlString: String, headers: Iterable[ (String, String) ] = Nil): HttpResponse = {

    val connection = new URL(urlString).openConnection.asInstanceOf[HttpURLConnection]
    headers.foreach { case (k, v) => connection.setRequestProperty(k, v) }

    val src = Source.fromInputStream(connection.getInputStream, "UTF-8")
    val responseBody = src.mkString
    src.close

    new HttpResponse(responseBody, connection.getResponseCode, connection.getResponseMessage)
  }

}


