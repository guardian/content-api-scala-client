import sbt._

object Dependencies {
  val scalaVersions = Seq("2.12.19", "2.13.13")
  val capiModelsVersion = "21.0.0-PREVIEW.agschema-org-field.2024-03-01T1602.c5a81993"
  val thriftVersion = "0.19.0"
  val commonsCodecVersion = "1.16.1"
  val scalaTestVersion = "3.0.9"
  val slf4jVersion = "1.7.36"
  val mockitoVersion = "1.10.19"
  val okhttpVersion = "3.14.9"
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
}
