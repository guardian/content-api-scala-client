import sbt.Keys._
import ReleaseTransformations._
import Dependencies._
import sbtversionpolicy.withsbtrelease.ReleaseVersion

licenses := Seq(License.Apache2)
organization := "com.gu"
organizationName := "Guardian News & Media Ltd"
organizationHomepage := Some(url("https://www.theguardian.com/"))
val ghProject = "content-api-client"

lazy val root = (project in file("."))
  .aggregate(client, defaultClient)
  .settings(commonSettings)
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
  .settings(commonSettings, clientSettings)
  .enablePlugins(BuildInfoPlugin)

lazy val defaultClient = (project in file("client-default"))
  .dependsOn(client)
  .settings(commonSettings, defaultClientSettings)


lazy val commonSettings: Seq[Setting[_]] = Seq(
  crossScalaVersions      := scalaVersions,
  scalaVersion            := scalaVersions.max,
  scalacOptions           ++= Seq("-deprecation", "-unchecked", "-release:8"),
)

lazy val clientSettings: Seq[Setting[_]] = Seq(
  name                := ghProject,
  description         := "Scala client for the Guardian's Content API",
  buildInfoKeys       := Seq[BuildInfoKey](version),
  buildInfoPackage    := "com.gu.contentapi.buildinfo",
  buildInfoObject     := "CapiBuildInfo",
  libraryDependencies ++= clientDeps
)

lazy val defaultClientSettings: Seq[Setting[_]] = Seq(
  name                :=  ghProject + "-default",
  description         := "Default scala client for the Guardian's Content API",
  libraryDependencies ++= clientDeps ++ defaultClientDeps,
  console / initialCommands   := """
    import com.gu.contentapi.client._
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.Await
    import scala.concurrent.duration._
  """
)


Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-u", sys.env.getOrElse("SBT_JUNIT_OUTPUT", "junit"))
