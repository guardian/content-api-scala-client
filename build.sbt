import sbt.Keys._
import ReleaseTransformations._
import Dependencies._

/* --------------------------------------------------------------------- */

lazy val root = (project in file("."))
  .aggregate(client, defaultClient, aws)
  .settings(commonSettings, publishSettings)
  .settings(
    skip in publish    := true,
    releaseVersionFile := baseDirectory.value / "version.sbt",
    sources in Compile := Seq.empty,
    sources in Test    := Seq.empty
  )

lazy val client = (project in file("client"))
  .settings(commonSettings, clientSettings, publishSettings)
  .enablePlugins(BuildInfoPlugin)

lazy val defaultClient = (project in file("client-default"))
  .dependsOn(client)
  .settings(commonSettings, defaultClientSettings, publishSettings)

lazy val aws = (project in file("aws"))
  .settings(commonSettings, awsSettings, publishSettings)

/* --------------------------------------------------------------------- */

lazy val commonSettings: Seq[Setting[_]] = Metadata.settings ++ Seq(
  crossScalaVersions      := scalaVersions,
  scalaVersion            := scalaVersions.max,
  javacOptions            ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions           ++= Seq("-deprecation", "-unchecked"),
)

lazy val clientSettings: Seq[Setting[_]] = Seq(
  name                := Metadata.ghProject,
  description         := "Scala client for the Guardian's Content API",
  developers          := Metadata.clientDevs,
  buildInfoKeys       := Seq[BuildInfoKey](version),
  buildInfoPackage    := "com.gu.contentapi.buildinfo",
  buildInfoObject     := "CapiBuildInfo",
  libraryDependencies ++= clientDeps
)

lazy val defaultClientSettings: Seq[Setting[_]] = Seq(
  name                := Metadata.ghProject + "-default",
  description         := "Default scala client for the Guardian's Content API",
  developers          := Metadata.clientDevs,
  libraryDependencies ++= clientDeps ++ defaultClientDeps,
  initialCommands in console := """
    import com.gu.contentapi.client._
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.Await
    import scala.concurrent.duration._
  """
)

lazy val awsSettings: Seq[Setting[_]] = Seq(
  name                := Metadata.ghProject + "-aws",
  description         := "AWS helper functionality for using Guardian's Content API scala client",
  developers          := Metadata.awsDevs,
  releaseVersionFile  := baseDirectory.value / "version.sbt",
  libraryDependencies ++= awsDeps,
)

lazy val publishSettings: Seq[Setting[_]] = Seq(
  resolvers += Resolver.sonatypeRepo("public"),
  pomIncludeRepository := { _ => false },
  publishTo := sonatypePublishToBundle.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  releaseVcsSign := true,
  releaseProcess := releaseSteps,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  pgpSecretRing := pgpPublicRing.value
)

lazy val releaseSteps: Seq[ReleaseStep] = Seq(
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
