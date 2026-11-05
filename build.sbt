import sbt.Keys._
import ReleaseTransformations._
import Dependencies._
import sbtversionpolicy.withsbtrelease.ReleaseVersion

val ghProject = "content-api-client"

lazy val root = (project in file("."))
  .aggregate(client, defaultClient, catsEffectClient, firehoseClient, faciaClients)
  .settings(
    publish / skip := true,
    releaseVersion := ReleaseVersion.fromAggregatedAssessedCompatibilityWithLatestRelease().value,
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
  .settings(artifactProductionSettings(false), clientSettings)
  .enablePlugins(BuildInfoPlugin)

lazy val defaultClient = (project in file("client-default"))
  .dependsOn(client)
  .settings(artifactProductionSettings(false), defaultClientSettings)
  .settings(
    name                :=  ghProject + "-default"
  )

lazy val catsEffectClient = (project in file("client-cats-effect"))
  .dependsOn(client, defaultClient % "compile->compile;test->test")
  .settings(artifactProductionSettings(false), defaultClientSettings)
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
  .settings(artifactProductionSettings(false)).settings(
    name                := "content-api-firehose-client",
    description         := "Firehose client for the CAPI Crier feed",
    libraryDependencies ++= Seq(
      capiModels,
      "com.gu" %% "thrift-serializer" % "5.0.8",
      "software.amazon.kinesis" % "amazon-kinesis-client" % "3.4.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.6",
      // "com.twitter" %% "scrooge-core" % "21.12.0",
      "at.yawk.lz4" % "lz4-java" % "1.10.4", // Necessary while the ExclusionRule for org.lz4:lz4-java is necessary
      scalaTest,
    ) ++ Seq("aws-json-protocol", "kinesis").map(artifact => "software.amazon.awssdk" % artifact % "2.57.2") ++ Seq(
      "jackson-databind", "jackson-annotations", "jackson-core"
    ).map(artifact => "com.fasterxml.jackson.core" % artifact % "2.17.3")

  )

lazy val faciaClients = (project in file("facia-client"))
  .aggregate(
    fapiClient_core,
    faciaJson_play30,
    fapiClient_play30
  )
  .settings(
    name := "facia-api-client",
    description := "Scala client for The Guardian's Facia JSON API",
    publish / skip := true
  )

// fapi-client-core is independent of AWS SDK version.
lazy val fapiClient_core = (project in file("facia-client/fapi-client-core")).settings(
  libraryDependencies += eTagCachingS3Base,
  artifactProductionSettings(supportScala3 = true)
)

def playJsonSpecificProject(module: String, playJsonVersion: PlayJsonVersion) =
  Project(
    s"$module-${playJsonVersion.projectId}",
    file(s"facia-client/$module-${playJsonVersion.projectId}")
  )
    .settings(
      sourceDirectory := baseDirectory.value / s"../$module/src"
    )

def faciaJson(playJsonVersion: PlayJsonVersion) = playJsonSpecificProject("facia-json", playJsonVersion)
  .dependsOn(fapiClient_core)
  .settings(
    libraryDependencies ++= Seq(
      jodaTime,
      commonsIo,
      playJsonVersion.lib,
      scalaLogging,
      scalaTestMockito
    ),
    artifactProductionSettings(supportScala3 = true)
  )

def fapiClient(playJsonVersion: PlayJsonVersion) = playJsonSpecificProject("fapi-client", playJsonVersion)
  .dependsOn(defaultClient)
  .settings(
    libraryDependencies ++= Seq(
      eTagCachingS3SupportForTesting,
      commercialShared,
      scalaTestMockito
    ),
    artifactProductionSettings(
      supportScala3 = false
    ) // currently blocked by contentApi & commercialShared clients
  )

lazy val faciaJson_play30 = faciaJson(PlayJsonVersion.V30)
lazy val fapiClient_play30 =
  fapiClient(PlayJsonVersion.V30).dependsOn(faciaJson_play30)

def artifactProductionSettings(supportScala3: Boolean) = Seq(
  organization := "com.gu",
  licenses := Seq(License.Apache2),
  scalaVersion := scalaVersions.max,
  crossScalaVersions := scalaVersions ++ Option.when(supportScala3)("3.3.8"),
  scalacOptions := Seq(
    "-release:11",
    "-feature",
    "-deprecation",
    "-Xfatal-warnings",
    "-unchecked"
  ),
  libraryDependencies += scalaTest
)

Test / scalacOptions ++= Seq(
  "-language:reflectiveCalls"
)

lazy val clientSettings: Seq[Setting[?]] = Seq(
  name                := ghProject,
  description         := "Scala client for the Guardian's Content API",
  buildInfoKeys       := Seq[BuildInfoKey](version),
  buildInfoPackage    := "com.gu.contentapi.buildinfo",
  buildInfoObject     := "CapiBuildInfo",
  libraryDependencies ++= capiClientDeps
)

lazy val defaultClientSettings: Seq[Setting[?]] = Seq(
  description         := "Default scala client for the Guardian's Content API",
  libraryDependencies ++= capiClientDeps ++ defaultClientDeps,
  console / initialCommands   := """
    import com.gu.contentapi.client._
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.Await
    import scala.concurrent.duration._
  """
)
