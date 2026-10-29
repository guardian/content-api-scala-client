import sbt._

object Dependencies {
  val capiVersion = "51.0.0"
  val eTagCachingVersion = "18.0.0"

  val eTagCachingS3Base =
    "com.gu.etag-caching" %% "aws-s3-base" % eTagCachingVersion
  val eTagCachingS3SupportForTesting =
    "com.gu.etag-caching" %% "aws-s3-sdk-v2" % eTagCachingVersion % Test
  val jodaTime = "joda-time" % "joda-time" % "2.14.3"
  val commonsIo = "org.apache.commons" % "commons-io" % "1.3.2"
  val contentApi = "com.gu" %% "content-api-client" % capiVersion
  val contentApiDefault =
    "com.gu" %% "content-api-client-default" % capiVersion % Test
  val mockito = "org.mockito" % "mockito-all" % "1.10.19" % Test
  val scalaTestMockito =
    "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % Test
  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.20" % Test
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
