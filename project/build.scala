import sbt._
import sbt.Keys._
import sbtbuildinfo.BuildInfoPlugin
import sbtbuildinfo.BuildInfoPlugin.BuildInfoKey
import sbtbuildinfo.BuildInfoKeys._
import sbtrelease._
import sbtrelease.ReleasePlugin._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleaseStateTransformations._
import xerial.sbt.Sonatype._
import xerial.sbt.Sonatype.SonatypeKeys._
import com.typesafe.sbt.pgp.PgpKeys._
import com.twitter.scrooge.ScroogeSBT

object ContentApiClientBuild extends Build {

  lazy val project = Project(
    id = "content-api-client",
    base = file(".")
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(ScroogeSBT.newSettings: _*)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](version),

    buildInfoPackage := "com.gu.contentapi.buildinfo",
    buildInfoObject := "CapiBuildInfo"
  )
  .settings(releaseSettings: _*)
  .settings(sonatypeSettings: _*)
  .settings(
    ScroogeSBT.scroogeThriftOutputFolder in Compile := sourceManaged.value / "thrift",
    unmanagedResourceDirectories in Compile += { baseDirectory.value / "src/main/thrift" },
    scalaVersion := "2.11.7",
    crossScalaVersions := Seq("2.11.7", "2.10.5"),
    organization := "com.gu",
    name := "content-api-client",
    description := "Scala client for the Guardian's Content API",
    licenses := Seq("Apache v2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
    scmInfo := Some(ScmInfo(
      url("https://github.com/guardian/content-api-scala-client"),
      "scm:git:git@github.com:guardian/content-api-scala-client.git"
    )),
    maxErrors := 20,
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    scalacOptions ++= Seq("-deprecation", "-unchecked"),

    libraryDependencies ++= Seq(
      "org.json4s" %% "json4s-native" % "3.3.0.RC4",
      "org.json4s" %% "json4s-ext" % "3.3.0.RC4",
      "joda-time" % "joda-time" % "2.3",
      "net.databinder.dispatch" %% "dispatch-core" % "0.11.3",
      "org.apache.thrift" % "libthrift" % "0.9.2",
      "com.twitter" %% "scrooge-core" % "3.20.0",
      "org.scalatest" %% "scalatest" % "2.2.1" % "test"
    ),
    pomExtra := (
      <url>https://github.com/guardian/content-api-scala-client</url>
      <developers>
        <developer>
          <id>jennysivapalan</id>
          <name>Jenny Sivapalan</name>
          <url>https://github.com/jennysivapalan</url>
        </developer>
        <developer>
          <id>maxharlow</id>
          <name>Max Harlow</name>
          <url>https://github.com/maxharlow</url>
        </developer>
        <developer>
          <id>nicl</id>
          <name>Nic Long</name>
          <url>https://github.com/nicl</url>
        </developer>
        <developer>
          <id>mchv</id>
          <name>Mariot Chauvin</name>
          <url>https://github.com/mchv</url>
        </developer>
        <developer>
          <id>LATaylor-guardian</id>
          <name>Luke Taylor</name>
          <url>https://github.com/LATaylor-guardian</url>
        </developer>
        <developer>
          <id>cb372</id>
          <name>Chris Birchall</name>
          <url>https://github.com/cb372</url>
        </developer>
      </developers>
    ),
    crossBuild := true,
    releaseProcess := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      ReleaseStep(
        action = state => Project.extract(state).runTask(publishSigned, state)._1,
        enableCrossBuild = true
      ),
      setNextVersion,
      commitNextVersion,
      ReleaseStep(state => Project.extract(state).runTask(sonatypeReleaseAll, state)._1),
      pushChanges
    )
  )

}
