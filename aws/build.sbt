import sbtrelease.ReleaseStateTransformations._

name:="content-api-client-aws"

resolvers += Resolver.sonatypeRepo("releases")

pomExtra := (
  <url>https://github.com/guardian/content-api-scala-client</url>
  <developers>
    <developer>
      <id>tomrf1</id>
      <name>Tom Forbes</name>
      <url>https://github.com/tomrf1</url>
    </developer>
  </developers>
  )
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := { _ => false }
description := "AWS helper functionality for using Guardian's Content API scala client"
scalaVersion := "2.12.4"
crossScalaVersions := Seq("2.11.12", scalaVersion.value)
releasePublishArtifactsAction := PgpKeys.publishSigned.value
organization := "com.gu"
licenses := Seq("Apache v2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))
scmInfo := Some(ScmInfo(
  url("https://github.com/guardian/content-api-scala-client"),
  "scm:git:git@github.com:guardian/content-api-scala-client.git"
))
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
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

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-core" % "1.11.259",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
