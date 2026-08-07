package com.gu.contentapi.client.catseffect.bulkqueries

import cats.effect.IO
import com.gu.contentapi.client.catseffect.IOCapiClient
import com.gu.contentapi.client.catseffect.bulkqueries.RequestSizing.Calculated.{availableUrlSpaceForIdsGiven, capiIdSizer}
import com.gu.contentapi.client.catseffect.bulkqueries.RequestSizing.MaxItemsAllowedInIdsParameter
import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.client.model.{CapiId, SearchQuery}
import com.madgag.algo.packing.binpacking.BinPacking.Size.CardinalityConstrained
import com.madgag.algo.packing.binpacking.OfflineAlgorithm.FFD
import com.madgag.algo.packing.binpacking._
import fs2.Stream
import fs2.Stream.emits

/**
 * This class provides safe bulk-requesting of `Content` by `CapiId`.
 *
 * When requesting an arbitrary number of `CapiId`s, a single request can easily exceed one of two hard limits:
 *
 *   - the url can grow larger than the 4096 character '''url-size limit''' imposed by the Content API HTTP endpoints,
 *     and get a [[https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status/414 HTTP 414 URI Too Long]] error
 *     response.
 *   - the ids parameter itself has a '''max-ids-per-request limit''' of 50 CAPI ids.
 *
 * This class works out how much url-space is available, and [[https://github.com/rtyley/bin-packing packs]] the
 * requested `CapiId`s into the lowest-possible number of requests, respecting both the url-size and
 * max-ids-per-request limits (sometimes, CAPI ids _can_ be short enough to fit more than 50 into the request url -
 * exceeding the max-ids-per-request limit but not the url-size limit).
 *
 * Creating an instance of `BulkContentRequester` performs some initial space calculations. You can retain the
 * instance and then use it over and over again with different sets of CAPI ids.
 */
class BulkContentRequester private(client: IOCapiClient, baseQuery: SearchQuery) {

  private val packer: Packer[CapiId, CardinalityConstrained] = Packer(Setup(
    binSize = CardinalityConstrained(
      size = availableUrlSpaceForIdsGiven(client.underlyingClient, baseQuery),
      cardinality = MaxItemsAllowedInIdsParameter
    ),
    sizer = capiId => CardinalityConstrained.item(capiIdSizer(capiId))
  ), FFD)

  /** @param capiIds
    *   The set of CAPI-ids can be large - it is not constrained by URL-length
    */
  def contentStream(capiIds: Set[CapiId]): Stream[IO, Content] =
    emits(pack(capiIds).toSeq).parEvalMapUnorderedUnbounded(bulkGet).flatMap(emits)

  /** @return
   *   CAPI-ids grouped so that the query will not exceed either the url-size or max-ids-per-request limits
   */
  private def pack(capiIds: Set[CapiId]): Set[Set[CapiId]] = capiIds.packWith(packer)

  /** Note: This method does not check any size limits - if the set of `CapiId`s is too large, the
   * Content API service will return an HTTP error response.
   */
  private def bulkGet(capiIds: Set[CapiId]): IO[Seq[Content]] =
    client.getResponse(baseQuery.withIds(capiIds)).map(_.results.toSeq)
}

object BulkContentRequester {
  def apply(client: IOCapiClient, baseQuery: SearchQuery): BulkContentRequester = new BulkContentRequester(
    client,
    baseQuery.pageSize(MaxItemsAllowedInIdsParameter) // default page-size is 10, too small - we can pack more CAPI ids than that into a request
  )
}
