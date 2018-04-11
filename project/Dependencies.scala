import sbt._

object Dependencies {
  val scalaVersions = Seq("2.11.12", "2.12.5")

  val CapiModelsVersion = "12.0"

  val clientDeps = Seq(
    "com.gu" %% "content-api-models-scala" % CapiModelsVersion,
    "org.scalatest" %% "scalatest" % "3.0.5" % "test" exclude("org.mockito", "mockito-core"),
    "org.mockito" % "mockito-all" % "1.10.19" % "test"
  )

  val defaultClientDeps = Seq(
    "com.squareup.okhttp3" % "okhttp" % "3.9.1",
    "org.slf4j" % "slf4j-api" % "1.7.25"
  )

  val awsDeps = Seq(
    "com.amazonaws" % "aws-java-sdk-core" % "1.11.280",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test"
  )
}