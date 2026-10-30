import Dependencies.*
import sbtrelease.ReleaseStateTransformations.*
import sbtversionpolicy.withsbtrelease.ReleaseVersion.fromAggregatedAssessedCompatibilityWithLatestRelease

description := "Scala client for The Guardian's Facia JSON API"

ThisBuild / scalaVersion := "2.13.18"

val sonatypeReleaseSettings = Seq(
  releaseVersion := fromAggregatedAssessedCompatibilityWithLatestRelease().value,
  releaseCrossBuild := true, // true if you cross-build the project for multiple Scala versions
  releaseProcess := Seq[ReleaseStep](
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

def artifactProducingSettings(supportScala3: Boolean) = Seq(
  organization := "com.gu",
  licenses := Seq(License.Apache2),
  crossScalaVersions := Seq(scalaVersion.value) ++ Option.when(supportScala3)("3.3.8"),
  scalacOptions := Seq(
    "-release:11",
    "-feature",
    "-deprecation",
    "-Xfatal-warnings"
  ),
  libraryDependencies += scalaTest
)

// fapi-client-core is independent of AWS SDK version.
lazy val fapiClient_core = (project in file("fapi-client-core")).settings(
  libraryDependencies += eTagCachingS3Base,
  artifactProducingSettings(supportScala3 = true)
)

lazy val root = (project in file("."))
  .aggregate(
    fapiClient_core,
    faciaJson_play30,
    fapiClient_play30
  )
  .settings(
    name := "facia-api-client",
    publish / skip := true,
    sonatypeReleaseSettings
  )

def playJsonSpecificProject(module: String, playJsonVersion: PlayJsonVersion) =
  Project(
    s"$module-${playJsonVersion.projectId}",
    file(s"$module-${playJsonVersion.projectId}")
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
      scalaLogging
    ),
    artifactProducingSettings(supportScala3 = true)
  )

def fapiClient(playJsonVersion: PlayJsonVersion) = playJsonSpecificProject("fapi-client", playJsonVersion)
  .settings(
    libraryDependencies ++= Seq(
      eTagCachingS3SupportForTesting,
      contentApi,
      contentApiDefault,
      commercialShared,
      scalaTestMockito
    ),
    artifactProducingSettings(
      supportScala3 = false
    ) // currently blocked by contentApi & commercialShared clients
  )

lazy val faciaJson_play30 = faciaJson(PlayJsonVersion.V30)
lazy val fapiClient_play30 =
  fapiClient(PlayJsonVersion.V30).dependsOn(faciaJson_play30)
