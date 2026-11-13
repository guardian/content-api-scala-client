import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import com.gu.contentapi.client.GuardianContentClient
import com.gu.etagcaching.aws.sdkv2.s3.S3ObjectFetching
import com.gu.facia.api.FAPI
import com.gu.facia.client.{ApiClient, Environment}
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProviderChain, SystemPropertyCredentialsProvider, EnvironmentVariableCredentialsProvider, ProfileCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient

import scala.concurrent.Await
import scala.concurrent.duration._

implicit val executionContext = scala.concurrent.ExecutionContext.global

// demo config
val apiKey = "INSERT_CONTENT_API_KEY_HERE"
val awsProfileName = "cmsFronts"

implicit val capiClient =  new GuardianContentClient(apiKey)
implicit val apiClient: ApiClient = {
  val credentialsProvider =  AwsCredentialsProviderChain.of(
    EnvironmentVariableCredentialsProvider.create(),
    SystemPropertyCredentialsProvider.create(),
    ProfileCredentialsProvider.builder().profileName(awsProfileName).build()
  )

  lazy val amazonS3Client = S3AsyncClient
    .builder()
    .httpClient(NettyNioAsyncHttpClient.builder().build())
    .credentialsProvider(credentialsProvider)
    .region(Region.EU_WEST_1)
    .build()

  ApiClient.withCaching(
    "aws-frontend-store",
    Environment.Dev,
    S3ObjectFetching.byteArraysWith(amazonS3Client)
  )
}

val frontResult = FAPI.frontForPath("uk/business").fold(
  { err => s"error: $err"},
  { front => s"found front: $front" }
)
Await.result(frontResult, 1.second)
val frontsResult = FAPI.getFronts().fold(
  { err => s"error fetching fronts: $err"},
  { fronts => s"found fronts: $fronts" }
)
Await.result(frontsResult, 1.second)
