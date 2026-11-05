import sbt._

object Dependencies {
  val scalaVersions = Seq("2.13.18")
  val capiModelsVersion = "53.0.0"
  val thriftVersion = "0.24.0"
  val commonsCodecVersion = "1.22.1"
  val scalaTestVersion = "3.2.20"
  val slf4jVersion = "2.0.18"
  val mockitoVersion = "5.12.0"
  val okhttpVersion = "4.12.0"

  val capiModels: ModuleID = "com.gu" %% "content-api-models-scala" % capiModelsVersion
  val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % scalaTestVersion % Test `exclude`("org.mockito", "mockito-core")
  val scalaTestMockito = "org.scalatestplus" %% "mockito-5-23" % "3.2.20.0" % Test

  // Note: keep libthrift at a version functionally compatible with that used in content-api-models
  // if build failures occur due to eviction / sbt-assembly mergeStrategy errors
  val capiClientDeps = Seq(
    capiModels,
    "org.apache.thrift" % "libthrift" % thriftVersion,
    "commons-codec" % "commons-codec" % commonsCodecVersion,
    scalaTest,
    scalaTestMockito,
    "org.slf4j" % "slf4j-api" % slf4jVersion,
    "org.mockito" % "mockito-core" % mockitoVersion
  )

  val defaultClientDeps = Seq(
    "com.squareup.okhttp3" % "okhttp" % okhttpVersion
  )

  // facia-scala-client
  val eTagCachingVersion = "18.0.0"
  val eTagCachingS3Base =
    "com.gu.etag-caching" %% "aws-s3-base" % eTagCachingVersion
  val eTagCachingS3SupportForTesting =
    "com.gu.etag-caching" %% "aws-s3-sdk-v2" % eTagCachingVersion % Test
  val jodaTime = "joda-time" % "joda-time" % "2.14.3"
  val commonsIo = "org.apache.commons" % "commons-io" % "1.3.2"
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.6"
  val commercialShared = "com.gu" %% "commercial-shared" % "6.1.8"

  case class PlayJsonVersion(
    majorMinorVersion: String,
    groupId: String,
    exactPlayJsonVersion: String
  ) {
    val projectId = s"play$majorMinorVersion"

    val lib: ModuleID = groupId %% "play-json" % exactPlayJsonVersion
  }

  object PlayJsonVersion {
    val V30 = PlayJsonVersion("30", "org.playframework", "3.0.6")
  }
}
