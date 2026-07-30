package com.gu.contentapi.client.catseffect.bulkqueries

import com.gu.contentapi.client.catseffect.bulkqueries.BulkContentRequester.RichSearchQuery
import com.gu.contentapi.client.catseffect.bulkqueries.RequestSizing.{MaxCapiUriLength, Practical}
import com.gu.contentapi.client.model.{CapiId, SearchQuery}
import com.gu.contentapi.client.{ContentApiClient, GuardianContentClient}
import org.scalatest.Inspectors
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class RequestSizingTest extends AnyFlatSpec with should.Matchers with Inspectors {

  private val baseQuery: SearchQuery = ContentApiClient.search.lang("en")
  private val client: ContentApiClient =
    new GuardianContentClient(apiKey = "test") // TODO - obtain api key from environment?

  val idAtTheSizeLimit = CapiId(Seq.fill(1013)("a").mkString("/"))
  val idThatIsJustOneCharacterOverTheLimit = CapiId("b" + idAtTheSizeLimit.value)

  val practicalRequestSizer = new Practical(client, baseQuery)

  it should "Calculate the length of the correct parts of the url - the parts that the length limit applies to" in {
    practicalRequestSizer.urlLengthFor(
      Set(CapiId("a"))
    ) shouldEqual 48 // if the query or api-key has changed, update
    practicalRequestSizer.urlLengthFor(Set(idAtTheSizeLimit)) shouldEqual MaxCapiUriLength
    practicalRequestSizer.urlLengthFor(
      Set(idThatIsJustOneCharacterOverTheLimit)
    ) shouldEqual MaxCapiUriLength + 1

    val fullUrl = client.url(baseQuery.withIds(Set(idAtTheSizeLimit)))
    fullUrl.length should be > MaxCapiUriLength // the limit does not include the protocol or domain of the actual url
  }

//  "Verify the behaviour of the limit on the actual production Content API" in {
//    val testingClient: HttpClient = HttpClient.newBuilder.version(HTTP_2).build()
//    def statusCodeFor(capiId: CapiId): Int = {
//      val fullUrl = client.url(baseQuery.withIds(Set(capiId)))
//      testingClient
//        .send(HttpRequest.newBuilder(URI.create(fullUrl)).GET().build(), discarding)
//        .statusCode
//    }
//
//    statusCodeFor(idAtTheSizeLimit) should be_!==(414)
//    statusCodeFor(idThatIsJustOneCharacterOverTheLimit) should be_==(414)
//  }

  it should "Create a verifiable calculator of request size, allowing us to understand how much capacity is taken by the base url" in {
    val calculated = RequestSizing.Calculated.basedOff(practicalRequestSizer)
    forAll(sampleIds.subsets().filter(_.nonEmpty).toSeq) { s: Set[CapiId] =>
      val calcSize = calculated.urlLengthFor(s)
      val practicalSize = practicalRequestSizer.urlLengthFor(s)
      calcSize shouldEqual practicalSize
    }
  }

  val sampleIds = Set(
    "us-news/live/2021/jan/19/joe-biden-inauguration-donald-trump-pardons-impeachment-covid-coronavirus-live-updates",
    "us-news/live/2021/jan/26/joe-biden-donald-trump-impeachment-kamala-harris-nancy-pelosi-covid-coronavirus-live-updates",
    "us-news/live/2021/jan/13/donald-trump-impeachment-nancy-pelosi-joe-biden-mike-pence-congress-covid-coronavirus-live-updates",
    "us-news/live/2021/feb/02/donald-trump-impeachment-joe-biden-immigration-covid-coronavirus-live-updates",
    "us-news/live/2021/feb/22/joe-biden-coronavirus-covid-white-house-texas-donald-trump-live-updates",
    "us-news/live/2021/feb/08/donald-trump-impeachment-trial-senate-covid-coronavirus-joe-biden-live-updates"
  ).map(CapiId(_))
}
