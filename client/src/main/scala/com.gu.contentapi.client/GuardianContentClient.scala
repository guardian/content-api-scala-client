package com.gu.contentapi.client

import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3._
import scala.concurrent.{ExecutionContext, Future, Promise}

class GuardianContentClient(val apiKey: String) extends ContentApiClientLogic {
  val targetUrl = "https://content.guardianapis.com"
  
  val http = new OkHttpClient.Builder()
    .connectTimeout(1000, TimeUnit.SECONDS)
    .readTimeout(2000, TimeUnit.SECONDS)
    .followRedirects(true)
    .connectionPool(new ConnectionPool(10, 60, TimeUnit.SECONDS))
    .build()

  override def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse] = {
    val reqBuilder = new Request.Builder().url(url)
    val req = headers.foldLeft(reqBuilder) {
      case (r, (name, value)) => r.header(name, value)
    }

    val promise = Promise[HttpResponse]()

    http.newCall(req.build()).enqueue(new Callback() {
      override def onFailure(call: Call, e: IOException): Unit = promise.failure(e)
      override def onResponse(call: Call, response: Response): Unit = {
        promise.success(HttpResponse(response.body().bytes, response.code(), response.message()))
      }
    })

    promise.future
  }

  /**
   * Shutdown the client and clean up all associated resources.
   *
   * Note: behaviour is undefined if you try to use the client after calling this method.
   */
  def shutdown(): Unit = http.dispatcher().executorService().shutdown()
}

