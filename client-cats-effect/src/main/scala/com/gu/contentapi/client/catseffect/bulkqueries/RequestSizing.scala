package com.gu.contentapi.client.catseffect.bulkqueries

import com.gu.contentapi.client.ContentApiClient
import com.gu.contentapi.client.model.{CapiId, SearchQuery}

import java.net.{URI, URLEncoder}
import java.nio.charset.StandardCharsets.UTF_8
import BulkContentRequester._
import com.gu.contentapi.client.catseffect.bulkqueries.RequestSizing.Calculated.capiIdSizer
import com.gu.contentapi.client.model.SearchQuery.IdSeparator

/** In order to send bulk-queries that are *not* too big (ie no request bigger than
  * [[RequestSizing.MaxCapiUriLength]]), we need to know how much space in the
  * url is:
  *
  *   - left available in the url for CAPI-ids (derived from how much is already used)
  *   - consumed by each CAPI-id that's added
  *
  * Available space can be determined practically using the actual ContentApiClient to construct the url, and
  * we can then verify that figure by calculating a few more urls.
  */
trait RequestSizing {

  /** @return
    *  the length of the part of the url that counts towards the [[RequestSizing.MaxCapiUriLength]] - ie
    *  the path & query params, without "https://content.guardianapis.com/"
    */
  def urlLengthFor(ids: Set[CapiId]): Int
}

object RequestSizing {
  /**
   * This limit includes the path, starting with the slash, the query parameters, and the separating '?'.
   * It does not include the protocol, or domain.
   */
  val MaxCapiUriLength = 4096

  /**
   * Can't ask for more than 50 CAPI ids at a time - the Content API will error:
   * "IDs parameter cannot contain more than 50 items"
   *
   * https://github.com/guardian/content-api/blob/dc64e5fe94bcd7e2a03cdb6d5cf5db3b415fbee4/concierge/src/main/scala/com.gu.contentapi.concierge/parameters/Parameters.scala#L592
   */
  val MaxItemsAllowedInIdsParameter = 50

  def urlEncodedLengthOf(s: String): Int = URLEncoder.encode(s, UTF_8).length

  /** Calculates a bulk-query's url length by getting the actual ContentApiClient to construct the url - this
    * is the actual url that will get sent, so the reported length is truthful.
    */
  class Practical(contentApiClient: ContentApiClient, baseQuery: SearchQuery) extends RequestSizing {
    override def urlLengthFor(ids: Set[CapiId]): Int = {
      val uri = URI.create(contentApiClient.url(baseQuery.withIds(ids)))
      uri.getRawPath.length + 1 + uri.getRawQuery.length
    }
  }

  /** Calculates a bulk-query's url length, based on how much space is taken by the individual CAPI-ids, and
    * exactly how much space is taken up by the rest of the query parameters and path.
    */
  case class Calculated(baseUrlSize: Int) extends RequestSizing {
    override def urlLengthFor(ids: Set[CapiId]): Int = {
      require(ids.nonEmpty)
      baseUrlSize + ids.toSeq.map(capiIdSizer).sum
    }

    val availableUrlSpaceForIds: Int = MaxCapiUriLength - baseUrlSize
  }

  object Calculated {
    private val UrlEncodedLengthOfIdSeparator: Int = urlEncodedLengthOf(IdSeparator)
    val capiIdSizer: CapiId => Int = capiId => urlEncodedLengthOf(capiId.value) + UrlEncodedLengthOfIdSeparator

    private val sampleCapiIds = Seq(
      "us-news/live/2021/jan/26/joe-biden-donald-trump-impeachment-kamala-harris-nancy-pelosi-covid-coronavirus-live-updates",
      "us-news/live/2021/jan/29/joe-biden-donald-trump-impeachment-covid-coronavirus-nancy-pelosi-kamala-harris-live-updates",
      "stage/2019/sep/03/standup-comedy-john-oliver-edinburgh-fringe-festival"
    ).map(CapiId(_))

    def basedOff(reference: RequestSizing): Calculated = {
      val trialCapiId = sampleCapiIds.head
      val calculated = Calculated(reference.urlLengthFor(Set(trialCapiId)) - capiIdSizer(trialCapiId))
      sampleCapiIds.toSet.subsets().filter(_.nonEmpty).foreach { s =>
        val calcSize = calculated.urlLengthFor(s)
        val refSize = reference.urlLengthFor(s)
        require(calcSize == refSize, s"Differing size: $calcSize vs $refSize for $s")
      }
      calculated
    }

    def availableUrlSpaceForIdsGiven(contentApiClient: ContentApiClient, baseQuery: SearchQuery): Int =
      Calculated.basedOff(new Practical(contentApiClient, baseQuery)).availableUrlSpaceForIds
  }

}
