package com.gu.contentapi.client.catseffect.bulkqueries

import cats.effect.IO
import com.gu.contentapi.client.GuardianContentClientTest
import com.gu.contentapi.client.catseffect.IOCapiClient
import com.gu.contentapi.client.model.SearchQuery
import com.gu.contentapi.client.utils.CapiModelEnrichment.RichContent
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import weaver.SimpleIOSuite

import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import scala.concurrent.ExecutionContext.Implicits.global

object BulkContentRequesterTest extends SimpleIOSuite {

  implicit val logging: LoggerFactory[IO] = Slf4jFactory.create[IO]

  val fixedMoment = Instant.parse("2020-06-01T00:00:00Z")

  test("Should be able to fetch Content specified by an arbitrarily large number of CAPI ids") {
    for {
      client <- IOCapiClient.from(GuardianContentClientTest.apiKey)
      lotsOfCapiIds <- client.paginatedStream(SearchQuery().fromDate(fixedMoment).toDate(fixedMoment.plus(1, DAYS)).pageSize(50)).map(_.capiId).compile.to(Set)
      bulkContentRequester = client.bulkContentRequestsBasedOn(SearchQuery().showTags("all"))
      content <- bulkContentRequester.contentStream(lotsOfCapiIds).map(c => c.capiId -> c).compile.to(Map)
    } yield expect(clue(content.size) == lotsOfCapiIds.size)
  }
}
