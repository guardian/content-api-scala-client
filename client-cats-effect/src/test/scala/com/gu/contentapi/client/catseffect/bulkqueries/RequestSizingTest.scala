package com.gu.contentapi.client.catseffect.bulkqueries

import com.gu.contentapi.client.catseffect.bulkqueries.RequestSizing.Calculated.urlEncodedLengthOf
import com.gu.contentapi.client.catseffect.bulkqueries.RequestSizing.{MaxCapiUriLength, Practical}
import com.gu.contentapi.client.model.{CapiId, SearchQuery}
import com.gu.contentapi.client.{ContentApiClient, GuardianContentClient, GuardianContentClientTest}
import org.scalatest.Inspectors
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.net.URI
import java.net.http.HttpClient.Version.HTTP_2
import java.net.http.HttpResponse.BodyHandlers.discarding
import java.net.http.{HttpClient, HttpRequest}

class RequestSizingTest extends AnyFlatSpec with should.Matchers with Inspectors {

  private val baseQuery: SearchQuery = ContentApiClient.search.lang("en")
  private val apiKey = GuardianContentClientTest.apiKey
  private val client: ContentApiClient = new GuardianContentClient(apiKey)
  private val expectedUrlSizeWithJustOneSingleCharCapiId = 44 + apiKey.length
  val idAtTheSizeLimit: CapiId = {
    val targetLengthForCapiIdThatIsOnTheLimit = MaxCapiUriLength - expectedUrlSizeWithJustOneSingleCharCapiId + 1
    val pathWithManySlashesThatWillExpandWhenUrlEncoded = Seq.fill(1000)("a").mkString("/")
    val remainingChars = targetLengthForCapiIdThatIsOnTheLimit -
      urlEncodedLengthOf(pathWithManySlashesThatWillExpandWhenUrlEncoded)
    CapiId(("b" * remainingChars) + pathWithManySlashesThatWillExpandWhenUrlEncoded)
  }
  val idThatIsJustOneCharacterOverTheLimit = CapiId("b" + idAtTheSizeLimit.value)

  val practicalRequestSizer = new Practical(client, baseQuery)
  val calculatedRequestSizer = RequestSizing.Calculated.basedOff(practicalRequestSizer)

  it should "calculate the length of the correct parts of the url - the parts that the length limit applies to" in {
    practicalRequestSizer.urlLengthFor(Set(CapiId("a"))) shouldEqual expectedUrlSizeWithJustOneSingleCharCapiId
    practicalRequestSizer.urlLengthFor(Set(idAtTheSizeLimit)) shouldEqual MaxCapiUriLength
    practicalRequestSizer.urlLengthFor(Set(idThatIsJustOneCharacterOverTheLimit)) shouldEqual MaxCapiUriLength + 1

    val fullUrl = client.url(baseQuery.withIds(Set(idAtTheSizeLimit)))
    fullUrl.length should be > MaxCapiUriLength // the limit does not include the protocol or domain of the actual url
  }

  it should "verify the behaviour of the limit on the actual production Content API" in {
    val testingClient: HttpClient = HttpClient.newBuilder.version(HTTP_2).build()
    def statusCodeFor(capiId: CapiId): Int = {
      val fullUrl = client.url(baseQuery.withIds(Set(capiId)))
      testingClient
        .send(HttpRequest.newBuilder(URI.create(fullUrl)).GET().build(), discarding)
        .statusCode
    }

    statusCodeFor(idAtTheSizeLimit) should not be 414
    statusCodeFor(idThatIsJustOneCharacterOverTheLimit) shouldBe 414
  }

  it should "create a verifiable calculator of request size, allowing us to understand how much capacity is taken by the base url" in {

    forAll(sampleIds.subsets().filter(_.nonEmpty).toSeq) { s: Set[CapiId] =>
      val calcSize = calculatedRequestSizer.urlLengthFor(s)
      val practicalSize = practicalRequestSizer.urlLengthFor(s)
      calcSize shouldEqual practicalSize
    }
  }

  it should "match the practical length of fields that contain spaces, as api-gateway IAM auth requires them to be '%20'" in {
    val s = Set(CapiId("pretty unusual"))
    val calcSize = calculatedRequestSizer.urlLengthFor(s)
    val practicalSize = practicalRequestSizer.urlLengthFor(s)
    calcSize shouldEqual practicalSize
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
