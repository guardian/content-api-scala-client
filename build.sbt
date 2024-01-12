import sbt.Keys._
import ReleaseTransformations._
import Dependencies._
import sbtversionpolicy.withsbtrelease.ReleaseVersion
import sbtrelease.{Version, versionFormatError}

/* --------------------------------------------------------------------- */

//val snapshotReleaseType = "snapshot"
//val snapshotReleaseSuffix = "-SNAPSHOT"

//lazy val versionSettingsMaybe = {
//  sys.props.get("RELEASE_TYPE").map {
//    case v if v == snapshotReleaseType => snapshotReleaseSuffix
//    case _ => ""
//  }.map { suffix =>
//    releaseVersion := {
//      ver => Version(ver).map(_.withoutQualifier.string).map(_.concat(suffix)).getOrElse(versionFormatError(ver))
//    }
//  }.toSeq
//}

lazy val root = (project in file("."))
  .aggregate(client, defaultClient)
  .settings(commonSettings)
  .settings(
    publish / skip := true,
    //releaseVersionFile := baseDirectory.value / "version.sbt",
    Compile / sources := Seq.empty,
    Test / sources := Seq.empty,
    releaseVersion := ReleaseVersion.fromAggregatedAssessedCompatibilityWithLatestRelease().value,
    releaseProcess := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      setNextVersion,
      commitNextVersion
    )
  )

lazy val client = (project in file("client"))
  .settings(commonSettings, clientSettings)
  .enablePlugins(BuildInfoPlugin)

lazy val defaultClient = (project in file("client-default"))
  .dependsOn(client)
  .settings(commonSettings, defaultClientSettings)

/* --------------------------------------------------------------------- */

lazy val commonSettings: Seq[Setting[_]] = Metadata.settings ++ Seq(
  crossScalaVersions      := scalaVersions,
  scalaVersion            := scalaVersions.max,
  javacOptions            ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions           ++= Seq("-unchecked", "-release:11"),
)

lazy val clientSettings: Seq[Setting[_]] = Seq(
  name                := Metadata.ghProject,
  description         := "Scala client for the Guardian's Content API",
  //developers          := Metadata.clientDevs,
  buildInfoKeys       := Seq[BuildInfoKey](version),
  buildInfoPackage    := "com.gu.contentapi.buildinfo",
  buildInfoObject     := "CapiBuildInfo",
  libraryDependencies ++= clientDeps
)

lazy val defaultClientSettings: Seq[Setting[_]] = Seq(
  name                := Metadata.ghProject + "-default",
  description         := "Default scala client for the Guardian's Content API",
  //developers          := Metadata.clientDevs,
  libraryDependencies ++= clientDeps ++ defaultClientDeps,
  console / initialCommands   := """
    import com.gu.contentapi.client._
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.Await
    import scala.concurrent.duration._
  """
)


//lazy val publishSettings: Seq[Setting[_]] = Seq(
//  resolvers += Resolver.sonatypeRepo("public"),
//  pomIncludeRepository := { _ => false },
//  publishTo := sonatypePublishToBundle.value,
//  publishMavenStyle := true,
//  Test / publishArtifact   := false,
//  releaseVcsSign := true,
//  releaseProcess := {
//    sys.props.get("RELEASE_TYPE") match {
//      case Some("production") => productionReleaseProcess
//      case _ => snapshotReleaseProcess
//    }
//  }
//)

//lazy val commonReleaseProcess = Seq[ReleaseStep](
//  checkSnapshotDependencies,
//  inquireVersions,
//  setReleaseVersion,
//  runClean,
//  runTest,
//  // For non cross-build projects, use releaseStepCommand("publishSigned")
//  releaseStepCommandAndRemaining("+publishSigned")
//)
//
//lazy val productionReleaseProcess = commonReleaseProcess ++ Seq[ReleaseStep](
//  releaseStepCommand("sonatypeBundleRelease")
//)
//
//lazy val snapshotReleaseProcess = commonReleaseProcess


Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-u", sys.env.getOrElse("SBT_JUNIT_OUTPUT", "junit"))