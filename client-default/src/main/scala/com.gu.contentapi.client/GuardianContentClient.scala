package com.gu.contentapi.client

import com.gu.contentapi.client.model.HttpResponse
import java.io.IOException

import okhttp3._

import scala.concurrent.{ExecutionContext, Future, Promise}
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration

object GuardianContentClient {
  def apply(apiKey: String): GuardianContentClient = {
    implicit val executor = ScheduledExecutor()
    val strategy = ContentApiBackoff.doublingStrategy(Duration(250L, TimeUnit.MILLISECONDS), 5)
    new GuardianContentClient(apiKey, strategy)(executor)
  }

  def apply(apiKey: String, backoffStrategy: ContentApiBackoff)(implicit executor: ScheduledExecutor): GuardianContentClient =
    new GuardianContentClient(apiKey, backoffStrategy)

}

class GuardianContentClient private[client] (val apiKey: String, val backoffStrategy: ContentApiBackoff)(implicit executor0: ScheduledExecutor) extends ContentApiClient {

  override implicit val executor: ScheduledExecutor = executor0

  protected def httpClientBuilder = new OkHttpClient.Builder()
    .connectTimeout(1, TimeUnit.SECONDS)
    .readTimeout(2, TimeUnit.SECONDS)
    .followRedirects(true)
    .connectionPool(new ConnectionPool(10, 60, TimeUnit.SECONDS))

  protected val http = httpClientBuilder.build

  def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse] = {

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

  /** Shutdown the client and clean up all associated resources.
    *
    * Note: behaviour is undefined if you try to use the client after calling this method.
    */
  def shutdown(): Unit = http.dispatcher().executorService().shutdown()

}

