package com.gu.contentapi.client.catseffect.bulkqueries

import cats.effect.IO
import com.gu.contentapi.client.catseffect.IOCapiClient
import com.gu.contentapi.client.catseffect.bulkqueries.BulkContentRequester.RichSearchQuery
import com.gu.contentapi.client.catseffect.bulkqueries.RequestSizing.Calculated.capiIdSizer
import com.gu.contentapi.client.catseffect.bulkqueries.RequestSizing.{Calculated, IdSeparator, Practical}
import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.client.model.{CapiId, SearchQuery}
import com.madgag.algo.packing.binpacking.OfflineAlgorithm.FFD
import com.madgag.algo.packing.binpacking._
import fs2.Stream
import fs2.Stream.emits

class BulkContentRequester(client: IOCapiClient, baseQuery: SearchQuery) {

  private val packer: Packer[CapiId] = {
    val binCapacity =
      Calculated.basedOff(new Practical(client.underlyingClient, baseQuery)).availableUrlSpaceForIds
    Packer(Setup(binCapacity, capiIdSizer), FFD)
  }

  /** @return
    *   CAPI-ids grouped so that the query-url for each group is *not* over the size limit
    */
  private def pack(capiIds: Set[CapiId]): Set[Set[CapiId]] = capiIds.packWith(packer)

  /** @param capiIds
    *   The set of capi-ids can be large - it is not constrained by URL-length
    */
  def contentStream(capiIds: Set[CapiId]): Stream[IO, Content] =
    emits(pack(capiIds).toSeq).parEvalMapUnorderedUnbounded(bulkGet).flatMap(emits)

  /** Note: this method does not enforce URL-length safety - if the capi-ids are collectively too long, CAPI
    * will return HTTP `414 URI Too Long` error response.
    */
  private def bulkGet(capiIds: Set[CapiId]): IO[Seq[Content]] =
    client.getResponse(baseQuery.withIds(capiIds)).map(_.results.toSeq)
}

object BulkContentRequester {
  implicit class RichSearchQuery(sq: SearchQuery) {
    def withIds(capiIds: Set[CapiId]): SearchQuery = sq.ids(capiIds.map(_.value).mkString(IdSeparator))
  }
}
