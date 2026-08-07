package com.gu.contentapi.client.catseffect

import cats.effect.IO
import com.gu.contentapi.client.Decoder.PageableResponseDecoder
import com.gu.contentapi.client.catseffect.Retrying.retry
import com.gu.contentapi.client.catseffect.bulkqueries.BulkContentRequester
import com.gu.contentapi.client.model.Direction.Next
import com.gu.contentapi.client.model.{ContentApiError, ContentApiQuery, PaginatedApiQuery, SearchQuery}
import com.gu.contentapi.client.{ContentApiClient, Decoder, GuardianContentClient}
import com.twitter.scrooge.ThriftStruct
import fs2.Stream.unfoldChunkLoopEval
import fs2.{Chunk, Stream}
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

import java.net.URI
import java.net.http.HttpClient.Version.HTTP_2
import java.net.http.HttpResponse.BodyHandlers.discarding
import java.net.http.{HttpClient, HttpRequest}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.jdk.OptionConverters._

/** Originally copied from code at
  * https://github.com/guardian/word-usage/blob/7587abcdc06738414655bd05ea5ee4854a4b3925/src/main/scala/com/gu/words/capi/IOCapiClient.scala#L19
  */
class IOCapiClient private (
    protected[catseffect] val underlyingClient: ContentApiClient,
    rateLimiter: TokenBucket
)(implicit
    ec: ExecutionContext,
    logging: LoggerFactory[IO]
) {
  implicit val logger: SelfAwareStructuredLogger[IO] = LoggerFactory[IO].getLogger

  def getResponse[Resp <: ThriftStruct: Decoder](query: ContentApiQuery[Resp]): IO[Resp] =
    retry(
      execute(query),
      desc = query.getClass.getSimpleName,
      detail = query.getUrl(""),
      errorAnalyzer = { case cae: ContentApiError => s"HTTP ${cae.httpStatus} - ${cae.errorResponse}" },
      retries = 5
    )

  def paginatedStream[R <: ThriftStruct, E, T](
      query: PaginatedApiQuery[R, E]
  )(implicit prd: PageableResponseDecoder[R, T]): Stream[IO, T] =
    unfoldChunkLoopEval(query)(getResponse(_).map { resp =>
      (Chunk.from[T](prd.elements(resp).toList), query.followingQueryGiven(resp, Next))
    })

  def bulkContentRequestsBasedOn(baseQuery: SearchQuery) = BulkContentRequester(this, baseQuery)

  private def execute[Resp <: ThriftStruct: Decoder](query: ContentApiQuery[Resp]): IO[Resp] =
    rateLimiter.enforceWithDelay >> IO.fromFuture(IO(underlyingClient.getResponse(query)))
}

object IOCapiClient {

  private val httpClientForTestingRateLimit: HttpClient = HttpClient.newBuilder.version(HTTP_2).build()

  private def getPerMinuteQuotaFor(apiKey: String): Int = httpClientForTestingRateLimit
    .send(HttpRequest.newBuilder(URI.create(s"https://content.guardianapis.com/?api-key=$apiKey")).GET().build(), discarding)
    .headers().firstValueAsLong("x-ratelimit-limit-minute").toScala.get.toInt

  private def createRateLimiterAppropriateTo(apiKey: String): IO[TokenBucket] = {
    val minuteQuota = getPerMinuteQuotaFor(apiKey)
    IO.println(s"CAPI Quota per min: $minuteQuota") >>
      TokenBucket.create(minuteQuota, minuteQuota / 60, 1.second)
  }

  def from(apiKey: String)(implicit ec: ExecutionContext, loggerFactory: LoggerFactory[IO]): IO[IOCapiClient] = for {
    rateLimiter <- createRateLimiterAppropriateTo(apiKey)
  } yield new IOCapiClient(new GuardianContentClient(apiKey), rateLimiter)

}
