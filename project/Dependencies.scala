import sbt._

object Dependencies {
  val scalaVersions = Seq("2.12.19", "2.13.16")
  val capiModelsVersion = "31.1.0"
  val thriftVersion = "0.20.0"
  val commonsCodecVersion = "1.17.0"
  val scalaTestVersion = "3.2.18"
  val slf4jVersion = "2.0.13"
  val mockitoVersion = "5.12.0"
  val okhttpVersion = "4.12.0"
  val awsSdkVersion = "1.11.280"

  // Note: keep libthrift at a version functionally compatible with that used in content-api-models
  // if build failures occur due to eviction / sbt-assembly mergeStrategy errors
  val clientDeps = Seq(
    "com.gu" %% "content-api-models-scala" % capiModelsVersion,
    "org.apache.thrift" % "libthrift" % thriftVersion,
    "commons-codec" % "commons-codec" % commonsCodecVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test" exclude("org.mockito", "mockito-core"),
    "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % "test",
    "org.slf4j" % "slf4j-api" % slf4jVersion,
    "org.mockito" % "mockito-core" % mockitoVersion
  )

  val defaultClientDeps = Seq(
    "com.squareup.okhttp3" % "okhttp" % okhttpVersion
  )
}
