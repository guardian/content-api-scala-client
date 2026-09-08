package com.gu.contentapi.firehose.kinesis

import com.gu.contentapi.firehose.kinesis.KinesisStreamReader.{ checkApiAccess, kinesisClientFor }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import software.amazon.awssdk.auth.credentials.{ AwsCredentialsProvider, AwsCredentialsProviderChain, EnvironmentVariableCredentialsProvider, ProfileCredentialsProvider }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.Region.EU_WEST_1

class KinesisStreamReaderTest extends AnyFlatSpec with Matchers {
  def credentialsForDevAndCI(devProfile: String, ciCreds: AwsCredentialsProvider): AwsCredentialsProviderChain =
    AwsCredentialsProviderChain.of(ciCreds, ProfileCredentialsProvider.builder().profileName(devProfile).build())

  val region: Region = EU_WEST_1
  val creds = credentialsForDevAndCI("capi", EnvironmentVariableCredentialsProvider.create())
  val client = kinesisClientFor(creds, region)

  "KinesisStreamReader" should "be able to execute a successful AWS SDK API call and not suffer JSON protocol conflicts" in {
    checkApiAccess(client, "content-api-firehose-v2-CODE")
  }

  "checkApiAccess" should "genuinely access the AWS API, and only pass if the call succeeds" in {
    assertThrows[Exception](checkApiAccess(client, "this-does-not-exist"))
  }
}