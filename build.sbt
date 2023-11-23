import sbt.Keys._
import ReleaseTransformations._
import Dependencies._
import sbtrelease.{Version, versionFormatError}

/* --------------------------------------------------------------------- */

val betaReleaseType = "beta"
val betaReleaseSuffix = "-beta.0"
val snapshotReleaseType = "snapshot"
val snapshotReleaseSuffix = "-SNAPSHOT"


lazy val root = (project in file("."))
  .aggregate(client, defaultClient)
  .settings(commonSettings, publishSettings)
  .settings(
    publish / skip     := true,
    Compile / sources  := Seq.empty,
    Test / sources     := Seq.empty
  )

lazy val client = (project in file("client"))
  .settings(commonSettings, clientSettings, publishSettings)
  .enablePlugins(BuildInfoPlugin)

lazy val defaultClient = (project in file("client-default"))
  .dependsOn(client)
  .settings(commonSettings, defaultClientSettings, publishSettings)

// we apply versionSettingsMaybe to aws too because this project is always released in isolation
// from the others - and we _might_ want to put out a snapshot or beta for that (TBC).
// Bear this in mind if we begin releasing aws in sync with the others
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
  console / initialCommands   := """
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
  releaseUseGlobalVersion := false,
  releaseVersionFile  := baseDirectory.value / "version.sbt",
  libraryDependencies ++= awsDeps,
)

lazy val canOverwrite: Boolean = {
  // allow overwriting of SNAPSHOT releases ONLY
  sys.props.get("RELEASE_TYPE").exists {
    case v if v == snapshotReleaseType => true
    case _ => false
  }
}

lazy val publishSettings: Seq[Setting[_]] = Seq(
  resolvers += Resolver.sonatypeRepo("public"),
  pomIncludeRepository := { _ => false },
  publishTo := sonatypePublishToBundle.value,
  publishConfiguration := publishConfiguration.value.withOverwrite(canOverwrite),
  publishMavenStyle := true,
  Test / publishArtifact   := false,
  releaseVcsSign := true,
  releaseProcess := {
    sys.props.get("RELEASE_TYPE") match {
      case Some("production") => productionReleaseProcess
      case _ => snapshotReleaseProcess
    }
  },
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  pgpSecretRing := pgpPublicRing.value  // <-- I wonder if this is causing the pgp errors some of us struggle with 🤔
)

lazy val commonReleaseProcess = Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  setReleaseVersion,
  runClean,
  runTest,
  // For non cross-build projects, use releaseStepCommand("publishSigned")
  releaseStepCommandAndRemaining("+publishSigned"),
)

lazy val productionReleaseProcess = commonReleaseProcess ++ Seq[ReleaseStep](
  releaseStepCommand("sonatypeBundleRelease"),
)

lazy val snapshotReleaseProcess = commonReleaseProcess

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-u", sys.env.getOrElse("SBT_JUNIT_OUTPUT", "junit"))
