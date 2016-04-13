import sbtrelease.ReleaseStateTransformations._

name:="content-api-client"

enablePlugins(BuildInfoPlugin)
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
  )
publishTo <<= version { v =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := { _ => false }
description := "Scala client for the Guardian's Content API"
scalaVersion := "2.11.8"
releasePublishArtifactsAction := PgpKeys.publishSigned.value
organization := "com.gu"
licenses := Seq("Apache v2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))
scmInfo := Some(ScmInfo(
  url("https://github.com/guardian/content-api-scala-client"),
  "scm:git:git@github.com:guardian/content-api-scala-client.git"
))
javacOptions ++= Seq("-source", "1.7", "-target", "1.7")
scalacOptions ++= Seq("-deprecation", "-unchecked")
releaseProcess := Seq(
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)
buildInfoKeys := Seq[BuildInfoKey](version)
buildInfoPackage := "com.gu.contentapi.buildinfo"
buildInfoObject := "CapiBuildInfo"
libraryDependencies ++= Seq(
  "com.gu" % "content-api-models" % "8.4",
  "org.apache.thrift" % "libthrift" % "0.9.3",
  "com.twitter" %% "scrooge-core" % "4.6.0",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "org.json4s" %% "json4s-ext" % "3.3.0",
  "joda-time" % "joda-time" % "2.3",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.3",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "com.google.guava" % "guava" % "19.0" % "test"
)
scroogeThriftOutputFolder in Compile := sourceManaged.value / "thrift"
scroogeThriftDependencies in Compile ++= Seq(
  "content-api-models",
  "story-packages-model-thrift",
  "content-atom-model-thrift"
)
// See: https://github.com/twitter/scrooge/issues/199
scroogeThriftSources in Compile ++= {
  (scroogeUnpackDeps in Compile).value.flatMap { dir => (dir ** "*.thrift").get }
}
