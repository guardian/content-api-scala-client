import sbtrelease.ReleaseStateTransformations._
import sbtversionpolicy.withsbtrelease.ReleaseVersion

name:= "content-api-firehose-client"
organization := "com.gu"
scalaVersion := "2.12.21"
crossScalaVersions := Seq(scalaVersion.value, "2.13.18")
scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-Xfatal-warnings", "-release:11")
Compile / doc / scalacOptions  := Nil

enablePlugins(plugins.JUnitXmlReportPlugin)
Test / testOptions +=
  Tests.Argument(TestFrameworks.ScalaTest, "-u", s"test-results/scala-${scalaVersion.value}", "-o")

organization := "com.gu"
licenses := Seq(License.Apache2)

releaseVersion := ReleaseVersion.fromAssessedCompatibilityWithLatestRelease().value
releaseCrossBuild := true
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

val jacksonVersion = "2.17.2"

libraryDependencies ++= Seq(
  "com.gu" %% "content-api-models-scala" % "46.0.0",
  "com.gu" %% "thrift-serializer" % "5.0.7",
  "software.amazon.kinesis" % "amazon-kinesis-client" % "3.2.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.6",
  "com.twitter" %% "scrooge-core" % "21.12.0",
  "at.yawk.lz4" % "lz4-java" % "1.10.4", // Necessary while the ExclusionRule for org.lz4:lz4-java is necessary
  "org.scalatest" %% "scalatest" % "3.2.20" % Test,

  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion
) ++ Seq("aws-json-protocol", "kinesis").map(artifact => "software.amazon.awssdk" % artifact % "2.29.47")

excludeDependencies ++= Seq(
  /**
   * This ExclusionRule for `org.lz4:lz4-java` (vulnerability: https://github.com/advisories/GHSA-cmp6-m4wj-q63q)
   * is necessary while our dependencies would still pull it in. The replacement for the dependency is
   * `at.yawk.lz4:lz4-java`, a fork that still uses the same class names, eg [[net.jpountz.lz4.LZ4BlockInputStream]].
   *
   * As of KCL v3.2.1, **without this ExclusionRule**:
   *
   * % sbt "whatDependsOn org.lz4 lz4-java"
   * [info] org.lz4:lz4-java:1.8.0
   * [info]   +-org.apache.kafka:kafka-clients:3.6.1
   * [info]     +-software.amazon.glue:schema-registry-serde:1.1.24
   * [info]       +-software.amazon.kinesis:amazon-kinesis-client:3.2.1
   * [info]         +-com.gu:content-api-firehose-client_2.12:1.0.32-SNAPSHOT [S]
   *
   * Note, KCL v3.3.0 & above already perform the exclusion and replacement with `at.yawk.lz4:lz4-java`
   * for us.
   */
  ExclusionRule(
    organization = "org.lz4", // https://github.com/advisories/GHSA-cmp6-m4wj-q63q
    name = "lz4-java"
  )
)