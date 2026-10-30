package com.gu.facia.client

import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec

import java.lang.Class.forName

/** Ideally, the whole of `facia-scala-client` would be independent of AWS SDK version - we'd _like_ consumers
  * of this library to be able to use whatever AWS SDK version they want, without us pulling in dependency on
  * either SDK version. For `facia-scala-client`, this is an attainable goal, as the only AWS API action it
  * performs is fetching from S3, and https://github.com/guardian/etag-caching provides the
  * `S3ByteArrayFetching` abstraction that encapsulates this action without tying to a specific AWS SDK
  * version.
  *
  * Due to legacy code compatibility, we can't completely remove AWS SDK v1 from `fapi-client` for now, but we
  * _have_ removed it from `fapi-client-core`. This `SdkIndependenceTest` keeps us honest, and ensures we
  * don't accidentally re-add the dependency.
  */
class SdkIndependenceTest extends AnyFlatSpec {

  private def assertClassIsUnavailable(className: String): Assertion =
    assertThrows[ClassNotFoundException](forName(className))

  "fapi-client-core" should "be independent of AWS SDKs (ie should not rely on AWS SDK v1 or v2)" in {
    assertClassIsUnavailable("com.amazonaws.services.s3.AmazonS3") // AWS SDK v1
    assertClassIsUnavailable(
      "software.amazon.awssdk.auth.credentials.AwsCredentialsProvider"
    ) // AWS SDK v2
  }
  it should "be independent of Play-Json, or at least that would be nice" in {
    assertClassIsUnavailable("play.api.libs.json.Format")
  }
}
