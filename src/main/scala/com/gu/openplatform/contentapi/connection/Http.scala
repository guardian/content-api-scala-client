package com.gu.openplatform.contentapi.connection

import io.Codec
import dispatch._
import com.ning.http.client._
import providers.netty.{NettyAsyncHttpProvider, NettyConnectionsPool}
import scala.concurrent.{Future, ExecutionContext}
import org.joda.time.ReadableInstant
import java.net.URLEncoder
import org.joda.time.format.ISODateTimeFormat
import com.gu.openplatform.contentapi.ApiError

case class HttpResponse(body: String, statusCode: Int, statusMessage: String)

trait Http {
  implicit val codec = Codec("UTF-8")
  def GET(url: String, headers: Iterable[(String, String)] = Nil): Future[HttpResponse]

  def fetch(url: String, parameters: Map[String, String]): Future[String] = {
    // api-key =
    require(!url.contains('?'), "must not specify parameters in url")

    val queryString = parameters map { case (k, v) => k + "=" + URLEncoder.encode(v, "UTF-8") } mkString "&"
    val target = url + "?" + queryString

    for {
      response <- GET(target, List("User-Agent" -> "scala-api-client", "Accept" -> "application/json"))
    } yield if (List(200, 302) contains response.statusCode)
      response.body
    else
      throw new ApiError(response.statusCode, response.statusMessage)
  }
}

case class Proxy(host: String, port: Int)

trait Dispatch {
  implicit def executionContext: ExecutionContext

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

  protected def get(urlString: String, headers: Iterable[(String, String)] = Nil): Future[HttpResponse] = {
    val request = {
      val r = url(urlString)
      headers.foreach{case (name, value) => r.setHeader(name, value)}
      r.build
    }
    Client(request, httpResponseHandler)
  }

  protected def httpResponseHandler = new FunctionHandler(r =>
    HttpResponse(r.getResponseBody("utf-8"), r.getStatusCode, r.getStatusText)
  )

  def close() = Client.client.close()
}

trait DispatchAsyncHttp extends Http with Dispatch {
  def GET(urlString: String, headers: Iterable[(String, String)] = Nil): Future[HttpResponse] =
    get(urlString, headers)
}
