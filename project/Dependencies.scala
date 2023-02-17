import sbt._

object Dependencies {
  val scalaVersions = Seq("2.12.10", "2.13.1")
  val capiModelsVersion = "17.5.1"
  val thriftVersion = "0.15.0"
  val commonsCodecVersion = "1.10"
  val scalaTestVersion = "3.0.8"
  val slf4jVersion = "1.7.25"
  val mockitoVersion = "1.10.19"
  val okhttpVersion = "3.9.1"
  val awsSdkVersion = "1.11.280"

  // Note: keep libthrift at a version functionally compatible with that used in content-api-models
  // if build failures occur due to eviction / sbt-assembly mergeStrategy errors
  val clientDeps = Seq(
    "com.gu" %% "content-api-models-scala" % capiModelsVersion,
    "org.apache.thrift" % "libthrift" % thriftVersion,
    "commons-codec" % "commons-codec" % commonsCodecVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test" exclude("org.mockito", "mockito-core"),
    "org.slf4j" % "slf4j-api" % slf4jVersion,
    "org.mockito" % "mockito-all" % mockitoVersion % "test"
  )

  val defaultClientDeps = Seq(
    "com.squareup.okhttp3" % "okhttp" % okhttpVersion
  )

  val awsDeps = Seq(
    "com.amazonaws" % "aws-java-sdk-core" % awsSdkVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  )
}
