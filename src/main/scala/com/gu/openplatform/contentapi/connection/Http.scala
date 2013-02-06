package com.gu.openplatform.contentapi.connection

import java.lang.String

import org.apache.commons.httpclient.methods.GetMethod
import java.net.{URL, HttpURLConnection}
import org.apache.commons.httpclient.{MultiThreadedHttpConnectionManager, HttpClient}
import io.{Codec, Source}
import dispatch._
import com.ning.http.client._
import providers.netty.{NettyAsyncHttpProvider, NettyConnectionsPool}


case class HttpResponse(body: String, statusCode: Int, statusMessage: String)

trait Http {
  implicit val codec = Codec("UTF-8")
  // this is what the Api client requires of an http connection
  def GET(url: String, headers: Iterable[ (String, String) ] = Nil): HttpResponse
}

case class Proxy(host: String, port: Int)

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
trait MultiThreadedApacheHttpClient extends ApacheHttpClient {
  val connectionManager = new MultiThreadedHttpConnectionManager
  override val httpClient = new HttpClient(connectionManager)

  maxConnections(10)

  def maxConnections(i: Int) {
    connectionManager.getParams.setMaxTotalConnections(i)
    connectionManager.getParams.setDefaultMaxConnectionsPerHost(i)
  }

  def setConnectionTimeout(ms: Int) {
    connectionManager.getParams.setConnectionTimeout(ms)
  }

  def setSocketTimeout(ms: Int) {
    connectionManager.getParams.setSoTimeout(ms)
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

trait DispatchHttp extends Http {

  lazy val maxConnections: Int = 10
  lazy val connectionTimeoutInMs: Int = 1000
  lazy val requestTimeoutInMs: Int = 2000
  lazy val proxy: Option[Proxy] = None
  lazy val compressionEnabled: Boolean = true

  lazy val config = {
    val c = new AsyncHttpClientConfig.Builder()
      .setAllowPoolingConnection(true)
      .setMaximumConnectionsPerHost(maxConnections)
      .setMaximumConnectionsTotal(maxConnections)
      .setConnectionTimeoutInMs(connectionTimeoutInMs)
      .setRequestTimeoutInMs(requestTimeoutInMs)
      .setCompressionEnabled(compressionEnabled)
      .setFollowRedirects(true)
    proxy.foreach(p => c.setProxyServer(new ProxyServer(p.host, p.port)))
    c.build
  }

  object Client extends dispatch.Http {
    override val client = {
      val connectionPool = new NettyConnectionsPool(new NettyAsyncHttpProvider(config))
      new AsyncHttpClient(new AsyncHttpClientConfig.Builder(config).setConnectionsPool(connectionPool).build)
    }
  }

  def GET(urlString: String, headers: Iterable[ (String, String) ] = Nil): HttpResponse = {

    val request = {
      val r = url(urlString)
      headers.foreach{case (name, value) => r.setHeader(name, value)}
      r.build
    }

    Client(request, httpResponseHandler)()
  }

  def httpResponseHandler = new FunctionHandler(r =>
    HttpResponse(r.getResponseBody("utf-8"), r.getStatusCode, r.getStatusText)
  )

  def close() = Client.client.close()
}


