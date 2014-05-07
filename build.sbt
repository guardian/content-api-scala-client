import sbtrelease._
import ReleaseStateTransformations._

releaseSettings

sonatypeSettings

name := "content-api-client"

organization := "com.gu"

scalaVersion := "2.10.3"

crossScalaVersions := Seq("2.11.0", "2.10.3")

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "1.6",
  "commons-httpclient" % "commons-httpclient" % "3.1",
  "org.json4s" %% "json4s-native" % "3.2.9",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.1",
  "org.scalatest" %% "scalatest" % "2.1.5" % "test"
)


maxErrors := 20

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

scalacOptions ++= Seq("-deprecation", "-unchecked")

description := "Scala client for the Guardian's Content API"

scmInfo := Some(ScmInfo(
  url("https://github.com/guardian/content-api-scala-client"),
  "scm:git:git@github.com:guardian/content-api-scala-client.git"
))

/** TODO add every contributor to Content API client here */
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
        <id>JustinPinner</id>
        <name>Justin Pinner</name>
        <url>https://github.com/JustinPinner</url>
      </developer>
    </developers>
  )

licenses := Seq("Apache V2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

ReleaseKeys.crossBuild := true

ReleaseKeys.releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(
    action = state => Project.extract(state).runTask(PgpKeys.publishSigned, state)._1,
    enableCrossBuild = true
  ),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(state => Project.extract(state).runTask(SonatypeKeys.sonatypeReleaseAll, state)._1),
  pushChanges
)
