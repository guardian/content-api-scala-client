import sbt.Keys._
import ReleaseTransformations._
import Dependencies._
import sbtversionpolicy.withsbtrelease.ReleaseVersion


val ghProject = "content-api-client"

lazy val verifyDependencies = taskKey[Unit]("Verify that only the maintained LZ4 artifact is resolved")

lazy val root = (project in file("."))
  .aggregate(client, defaultClient, catsEffectClient, firehoseClient)
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
  .settings(artifactProductionSettings, clientSettings)
  .enablePlugins(BuildInfoPlugin)

lazy val defaultClient = (project in file("client-default"))
  .dependsOn(client)
  .settings(artifactProductionSettings, defaultClientSettings)
  .settings(
    name                :=  ghProject + "-default"
  )

lazy val catsEffectClient = (project in file("client-cats-effect"))
  .dependsOn(client, defaultClient % "compile->compile;test->test")
  .settings(artifactProductionSettings, defaultClientSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.13.0",
      "org.typelevel" %% "cats-effect" % "3.7.1",
      "org.typelevel" %% "log4cats-slf4j"   % "2.8.0",
      "co.fs2" %% "fs2-core" % "3.13.0",
      "com.madgag" %% "bin-packing" % "3.0.0",
      "org.typelevel" %% "weaver-cats" % "0.13.0" % Test
    ),
    name                :=  ghProject + "-cats-effect"
  )

lazy val firehoseClient = (project in file("firehose-client"))
  .settings(artifactProductionSettings).settings(
    name                := "content-api-firehose-client",
    description         := "Firehose client for the CAPI Crier feed",
    libraryDependencies ++= Seq(
      capiModels,
      "com.gu" %% "thrift-serializer" % "5.0.8",
      "software.amazon.kinesis" % "amazon-kinesis-client" % "3.4.3",
      // Used directly by KinesisStreamReader; KCL still brings an affected older release.
      "com.google.guava" % "guava" % "33.7.2-jre",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.6",
      // "com.twitter" %% "scrooge-core" % "21.12.0",
      "at.yawk.lz4" % "lz4-java" % "1.11.4",
      // Glue's Kafka client still selects an affected Snappy release.
      "org.xerial.snappy" % "snappy-java" % "1.1.10.9",
      scalaTest,
    ) ++ Seq("schema-registry-serde", "schema-registry-common")
      .map(artifact => "software.amazon.glue" % artifact % glueSchemaRegistryVersion) ++
      Seq("aws-json-protocol", "kinesis").map(artifact => "software.amazon.awssdk" % artifact % "2.57.2") ++ Seq(
      "jackson-databind", "jackson-annotations", "jackson-core"
    ).map(artifact => "com.fasterxml.jackson.core" % artifact % "2.17.3"),
    // Both artifacts provide net.jpountz.*; never publish a graph containing both.
    excludeDependencies += ExclusionRule("org.lz4", "lz4-java"),
    // zstd-jni follows native Zstd versions, not Java API versions.
    libraryDependencySchemes += "com.github.luben" % "zstd-jni" % VersionScheme.Always,
    verifyDependencies := {
      val configurations = update.value.configurations
      configurations.foreach { configuration =>
        val modules = configuration.modules.filterNot(_.evicted).map(_.module)
        require(!modules.exists(m => m.organization == "org.lz4" && m.name == "lz4-java"),
          s"Unmaintained org.lz4:lz4-java found in ${configuration.configuration}")
      }
      require(configurations.exists(_.modules.exists(m =>
        !m.evicted && m.module.organization == "at.yawk.lz4" && m.module.name == "lz4-java")),
        "Missing maintained at.yawk.lz4:lz4-java replacement")
    },
    // sbt 2's test is an input task; CI runs the non-incremental testFull task.
    Test / test := (Test / test).dependsOn(verifyDependencies).evaluated,
    Test / testFull := Def.uncached((Test / testFull).dependsOn(verifyDependencies).value)

  )

lazy val artifactProductionSettings: Seq[Setting[?]] = Seq(
  crossScalaVersions      := scalaVersions,
  scalaVersion            := scalaVersions.max,
  scalacOptions           ++= Seq("-deprecation", "-unchecked", "-release:11"),
  licenses                := Seq(License.Apache2),
  organization            := "com.gu"
)

lazy val clientSettings: Seq[Setting[?]] = Seq(
  name                := ghProject,
  description         := "Scala client for the Guardian's Content API",
  buildInfoKeys       := Seq[BuildInfoKey](version),
  buildInfoPackage    := "com.gu.contentapi.buildinfo",
  buildInfoObject     := "CapiBuildInfo",
  libraryDependencies ++= clientDeps
)

lazy val defaultClientSettings: Seq[Setting[?]] = Seq(
  description         := "Default scala client for the Guardian's Content API",
  libraryDependencies ++= clientDeps ++ defaultClientDeps,
  console / initialCommands   := """
    import com.gu.contentapi.client._
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.Await
    import scala.concurrent.duration._
  """
)
