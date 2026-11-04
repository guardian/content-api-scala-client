package com.gu.facia.api.integration

import com.gu.facia.client.Environment
import com.gu.facia.client.models.{CollectionJson, ConfigJson, CustomSubnavConfig}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue, Json, Reads}
import software.amazon.awssdk.auth.credentials.{
  AwsCredentialsProviderChain,
  EnvironmentVariableCredentialsProvider,
  ProfileCredentialsProvider
}
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest

import java.nio.charset.StandardCharsets.UTF_8
import java.time.Duration

class CodeJsonCompatibilityTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  private val bucket = "redacted"
  private val environment = Environment.Code

  private val credentials = AwsCredentialsProviderChain.of(
    EnvironmentVariableCredentialsProvider.create(),
    ProfileCredentialsProvider.builder().profileName("<redacted>").build()
  )
  private val s3 = S3Client
    .builder()
    .region(Region.EU_WEST_1)
    .credentialsProvider(credentials)
    .overrideConfiguration(
      ClientOverrideConfiguration.builder().apiCallTimeout(Duration.ofSeconds(30)).build()
    )
    .build()

  override protected def afterAll(): Unit =
    try {
      s3.close()
      credentials.close()
    } finally super.afterAll()

  private case class JsonSizes(
      maxDepth: Int,
      maxStringLength: Int,
      maxFieldNameLength: Int
  )

  private def sizes(json: JsValue, depth: Int = 0): JsonSizes = {
    val (children, stringLength, fieldNameLength) = json match {
      case JsObject(fields) =>
        (fields.values.toSeq, 0, fields.keysIterator.map(_.length).foldLeft(0)(_ max _))
      case JsArray(values) => (values.toSeq, 0, 0)
      case JsString(value) => (Seq.empty[JsValue], value.length, 0)
      case _               => (Seq.empty[JsValue], 0, 0)
    }
    val containerDepth = json match {
      case _: JsObject | _: JsArray => depth + 1
      case _                        => depth
    }
    children.foldLeft(JsonSizes(containerDepth, stringLength, fieldNameLength)) { (result, child) =>
      val childSizes = sizes(child, containerDepth)
      JsonSizes(
        result.maxDepth max childSizes.maxDepth,
        result.maxStringLength max childSizes.maxStringLength,
        result.maxFieldNameLength max childSizes.maxFieldNameLength
      )
    }
  }

  private def checkResource[A: Reads](key: String): Unit = {
    val location = s"s3://$bucket/$key"
    withClue(s"$location: ") {
      val bytes = s3
        .getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(key).build())
        .asByteArray()
      // Match ApiClient's UTF-8 bytes-to-String parsing path.
      val json = Json.parse(new String(bytes, UTF_8))
      val measured = sizes(json)
      info(
        s"$location: bytes=${bytes.length}, maxContainerDepth=${measured.maxDepth}, " +
          s"maxStringChars=${measured.maxStringLength}, maxFieldNameChars=${measured.maxFieldNameLength}"
      )
      val result = json.validate[A]
      withClue(s"Model validation failed: ${result.asEither.left.toOption}") {
        result.isSuccess shouldBe true
      }
    }
  }

  //bytes=1488441, maxContainerDepth=5, maxStringChars=655, maxFieldNameChars=78
  "CODE JSON" should "parse and validate the global config" in {
    checkResource[ConfigJson](environment.configS3Path)
  }

  // bytes=24193, maxContainerDepth=6, maxStringChars=134, maxFieldNameChars=14
  it should "parse and validate the custom navigation config" in {
    checkResource[CustomSubnavConfig](environment.customSubnavS3Path)
  }

  //collection.json: bytes=4365, maxContainerDepth=7, maxStringChars=116, maxFieldNameChars=20
    it should s"parse and validate collection" in {
      //4.3 KB - not sure if I should commit so I've just replaced
      var id = "olwen's collection id"
      checkResource[CollectionJson](environment.collectionS3Path(id))
    }
}
