package com.gu.facia.client

import com.gu.etagcaching.aws.s3.{ObjectId, S3ByteArrayFetching}
import com.gu.etagcaching.fetching.{ETaggedData, Fetching, Missing, MissingOrETagged}
import com.gu.facia.client.lib.ResourcesHelper
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}
import scala.util.hashing.MurmurHash3

/** This is used only for testing, it's a dummy implementation of S3ByteArrayFetching that just loads blobs
  * from the `src/test/resources` folder, rather than hitting S3.
  */
object FakeS3Fetching extends S3ByteArrayFetching with ResourcesHelper {
  private def pretendETagFor(bytes: Array[Byte]): String =
    MurmurHash3.bytesHash(bytes).toHexString

  override def fetch(
      objectId: ObjectId
  ): Future[MissingOrETagged[Array[Byte]]] = Future.successful {
    slurpBytes(objectId.key).fold(Missing: MissingOrETagged[Array[Byte]]) { bytes =>
      ETaggedData(pretendETagFor(bytes), bytes)
    }
  }

  override def fetchOnlyIfETagChanged(
      objectId: ObjectId,
      oldETag: String
  ): Future[Option[MissingOrETagged[Array[Byte]]]] = {
    implicit val ec: ExecutionContext = ExecutionContext.parasitic

    fetch(objectId).map {
      case taggedData: ETaggedData[_] =>
        Option.unless(oldETag == taggedData.eTag)(
          taggedData
        ) // simulate a Not-Modified response, if there's no change in ETag
      case x => Some(x)
    }
  }
}

class ApiClientSpec
    extends AnyFlatSpec
    with Matchers
    with OptionValues
    with ScalaFutures
    with IntegrationPatience {
  import scala.concurrent.ExecutionContext.Implicits.global

  val bucketName = "not used"
  val cachingClient: ApiClient =
    ApiClient.withCaching(bucketName, Environment.Dev, FakeS3Fetching)

  s"caching ApiClient" should "fetch the config" in {
    val config = cachingClient.config.futureValue

    config.collections should have size 334
    config.fronts should have size 79
  }

  it should "fetch a collection" in {
    val collectionOpt =
      cachingClient.collection("2409-31b3-83df0-de5a").futureValue

    collectionOpt.value.live should have size 8
  }

  it should "fetch the custom subnav config" in {
    val subnavConfigOpt = cachingClient.subnavConfig().futureValue

    val subnav = subnavConfigOpt.value.live.head
    subnav.id shouldBe "football-nav"
    subnav.header.headerText shouldBe "Football"
    subnav.links should have size 2
    subnav.pages should have size 1
    subnavConfigOpt.value.draft shouldBe empty
  }
}
