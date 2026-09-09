import sbt.Keys._
import ReleaseTransformations._
import Dependencies._
import sbtversionpolicy.withsbtrelease.ReleaseVersion


val ghProject = "content-api-client"

lazy val root = (project in file("."))
  .aggregate(client, defaultClient, catsEffectClient, firehoseClient)
  .settings(
    publish / skip := true,
    // releaseVersion := ReleaseVersion.fromAggregatedAssessedCompatibilityWithLatestRelease().value,
    releaseProcess := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      setNextVersion,
      commitNextVersion
    )
  )

lazy val client = (project in file("client"))
  .settings(artifactProductionSettings, clientSettings)
  .enablePlugins(BuildInfoPlugin)

lazy val defaultClient = (project in file("client-default"))
  .dependsOn(client)
  .settings(artifactProductionSettings, defaultClientSettings)
  .settings(
    name                :=  ghProject + "-default"
  )

lazy val catsEffectClient = (project in file("client-cats-effect"))
  .dependsOn(client, defaultClient % "compile->compile;test->test")
  .settings(artifactProductionSettings, defaultClientSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.13.0",
      "org.typelevel" %% "cats-effect" % "3.7.1",
      "org.typelevel" %% "log4cats-slf4j"   % "2.8.0",
      "co.fs2" %% "fs2-core" % "3.13.0",
      "com.madgag" %% "bin-packing" % "3.0.0",
      "org.typelevel" %% "weaver-cats" % "0.13.0" % Test
    ),
    name                :=  ghProject + "-cats-effect"
  )

lazy val firehoseClient = (project in file("firehose-client"))
  .settings(artifactProductionSettings).settings(
    name                := "content-api-firehose-client",
    description         := "Firehose client for the CAPI Crier feed",
    libraryDependencies ++= Seq(
      capiModels,
      "com.gu" %% "thrift-serializer" % "5.0.7",
      "software.amazon.kinesis" % "amazon-kinesis-client" % "3.4.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.6",
      // "com.twitter" %% "scrooge-core" % "21.12.0",
      "at.yawk.lz4" % "lz4-java" % "1.10.4", // Necessary while the ExclusionRule for org.lz4:lz4-java is necessary
      scalaTest,
    ) ++ Seq("aws-json-protocol", "kinesis").map(artifact => "software.amazon.awssdk" % artifact % "2.49.5") ++ Seq(
      "jackson-databind", "jackson-annotations", "jackson-core"
    ).map(artifact => "com.fasterxml.jackson.core" % artifact % "2.17.3")

  )

lazy val artifactProductionSettings: Seq[Setting[?]] = Seq(
  crossScalaVersions      := scalaVersions,
  scalaVersion            := scalaVersions.max,
  scalacOptions           ++= Seq("-deprecation", "-unchecked", "-release:11"),
  licenses                := Seq(License.Apache2),
  organization            := "com.gu"
)

lazy val clientSettings: Seq[Setting[?]] = Seq(
  name                := ghProject,
  description         := "Scala client for the Guardian's Content API",
  buildInfoKeys       := Seq[BuildInfoKey](version),
  buildInfoPackage    := "com.gu.contentapi.buildinfo",
  buildInfoObject     := "CapiBuildInfo",
  libraryDependencies ++= clientDeps
)

lazy val defaultClientSettings: Seq[Setting[?]] = Seq(
  description         := "Default scala client for the Guardian's Content API",
  libraryDependencies ++= clientDeps ++ defaultClientDeps,
  console / initialCommands   := """
    import com.gu.contentapi.client._
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.Await
    import scala.concurrent.duration._
  """
)

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-u", s"test-results/scala-${scalaVersion.value}", "-o")
