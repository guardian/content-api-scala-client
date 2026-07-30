package com.gu.contentapi.client.catseffect.bulkqueries

import cats.effect.IO
import com.gu.contentapi.client.catseffect.IOCapiClient
import com.gu.contentapi.client.catseffect.bulkqueries.RequestSizing.Calculated.capiIdSizer
import com.gu.contentapi.client.catseffect.bulkqueries.RequestSizing.{Calculated, MaxNumItemsAllowedInIdsParameter, Practical}
import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.client.model.{CapiId, SearchQuery}
import com.madgag.algo.packing.binpacking.BinPacking.Size.CardinalityConstrained
import com.madgag.algo.packing.binpacking.OfflineAlgorithm.FFD
import com.madgag.algo.packing.binpacking._
import fs2.Stream
import fs2.Stream.emits

/**
 * Safe bulk-requesting of `Content` by `CapiId`.
 *
 * When requesting an arbitrary number of `CapiId`s, the request url can easily grow larger than the 4096 character
 * url-size-limit imposed by the Content API HTTP endpoints, and get a
 * [[https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status/414 HTTP 414 URI Too Long]] error response.
 *
 * This class works out how much url-space is available, and [[https://github.com/rtyley/bin-packing packs]] the
 * requested `CapiId`s into the lowest-possible number of requests.
 *
 * You can create a `BulkContentRequester`, which performs the initial space calculations, and then use it over and
 * over again.
 */
class BulkContentRequester private(client: IOCapiClient, baseQuery: SearchQuery) {

  private val packer: Packer[CapiId, CardinalityConstrained] = Packer(Setup(
    binSize = CardinalityConstrained(
      size = Calculated.basedOff(new Practical(client.underlyingClient, baseQuery)).availableUrlSpaceForIds,
      cardinality = MaxNumItemsAllowedInIdsParameter
    ),
    sizer = capiId => CardinalityConstrained.item(capiIdSizer(capiId))
  ), FFD)

  /** @param capiIds
    *   The set of capi-ids can be large - it is not constrained by URL-length
    */
  def contentStream(capiIds: Set[CapiId]): Stream[IO, Content] =
    emits(pack(capiIds).toSeq).parEvalMapUnorderedUnbounded(bulkGet).flatMap(emits)

  /** @return
   *   CAPI-ids grouped so that the query-url for each group is *not* over the size limit
   */
  private def pack(capiIds: Set[CapiId]): Set[Set[CapiId]] = capiIds.packWith(packer)

  /** Note: this method does not enforce URL-length safety - if the capi-ids are collectively too long, CAPI
    * will return HTTP `414 URI Too Long` error response.
    */
  private def bulkGet(capiIds: Set[CapiId]): IO[Seq[Content]] =
    client.getResponse(baseQuery.withIds(capiIds)).map(_.results.toSeq)
}

object BulkContentRequester {
  def apply(client: IOCapiClient, baseQuery: SearchQuery): BulkContentRequester = new BulkContentRequester(
    client,
    baseQuery.pageSize(MaxNumItemsAllowedInIdsParameter) // default page-size is 10, too small - we can pack more CAPI ids than that into a request
  )
}
