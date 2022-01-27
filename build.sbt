import sbt.Keys._
import ReleaseTransformations._
import Dependencies._
import sbtrelease.{Version, versionFormatError}

/* --------------------------------------------------------------------- */

val betaReleaseType = "beta"
val betaReleaseSuffix = "-beta.0"
val snapshotReleaseType = "snapshot"
val snapshotReleaseSuffix = "-SNAPSHOT"

lazy val versionSettingsMaybe = {
  // Set by e.g. sbt -DRELEASE_TYPE=candidate|snapshot.
  // For production release, don't set a RELEASE_TYPE variable
  sys.props.get("RELEASE_TYPE").map {
    case v if v == betaReleaseType => betaReleaseSuffix
    case v if v == snapshotReleaseType => snapshotReleaseSuffix
  }.map { suffix =>
    releaseVersion := {
      ver => Version(ver).map(_.withoutQualifier.string).map(_.concat(suffix)).getOrElse(versionFormatError(ver))
    }
  }.toSeq
}

lazy val root = (project in file("."))
  .aggregate(client, defaultClient)
  .settings(commonSettings, versionSettingsMaybe, publishSettings)
  .settings(
    publish / skip     := true,
    releaseVersionFile := baseDirectory.value / "version.sbt",
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
  .settings(commonSettings, awsSettings, versionSettingsMaybe, publishSettings)

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
  releaseProcess := releaseProcessSteps,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  pgpSecretRing := pgpPublicRing.value  // <-- I wonder if this is causing the pgp errors some of us struggle with 🤔
)

lazy val checkReleaseType: ReleaseStep = ReleaseStep({ st: State =>
  val releaseType = sys.props.get("RELEASE_TYPE").map {
    case v if v == betaReleaseType => betaReleaseType.toUpperCase
    case v if v == snapshotReleaseType => snapshotReleaseType.toUpperCase
  }.getOrElse("PRODUCTION")

  SimpleReader.readLine(s"This will be a $releaseType release. Continue? (y/n) [N]: ") match {
    case Some(v) if Seq("Y", "YES").contains(v.toUpperCase) => // we don't care about the value - it's a flow control mechanism
    case _ => sys.error(s"Release aborted by user!")
  }
  // we haven't changed state, just pass it on if we haven't thrown an error from above
  st
})

lazy val releaseProcessSteps: Seq[ReleaseStep] = {
  val commonSteps = Seq(
    checkReleaseType,
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest
  )

  val prodSteps: Seq[ReleaseStep] = Seq(
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommand("sonatypeBundleRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )

  /*
  SNAPSHOT versions are published directly to Sonatype snapshot repo and no local bundle is assembled
  Also, we cannot use `sonatypeBundleUpload` or `sonatypeRelease` commands which are usually wrapped up
  within a call to `sonatypeBundleRelease` (https://github.com/xerial/sbt-sonatype#publishing-your-artifact).

  Therefore SNAPSHOT versions are not promoted to Maven Central and consumers will have to ensure they have the
  appropriate resolver entry in their build.sbt, e.g.

  resolvers += Resolver.sonatypeRepo("snapshots")

  To make this work, start SBT with the snapshot releaseType;
    sbt -DRELEASE_TYPE=snapshot

  */
  val snapshotSteps: Seq[ReleaseStep] = Seq(
    setReleaseVersion,
    releaseStepCommandAndRemaining("+publishSigned"),
    setNextVersion
  )

  /*
  Beta assemblies can be published to Sonatype and Maven.

  To make this work, start SBT with the candidate releaseType;
    sbt -DRELEASE_TYPE=beta

  This gets around the "problem" of sbt-sonatype assuming that a -SNAPSHOT build should not be delivered to Maven.

  In this mode, the version number will be presented as e.g. 1.2.3-beta.0, but the git tagging and version-updating
  steps are not triggered, so it's up to the developer to keep track of what was released and manipulate subsequent
  release and next versions appropriately.
  */
  val betaSteps: Seq[ReleaseStep] = Seq(
    setReleaseVersion,
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommand("sonatypeBundleRelease"),
    setNextVersion
  )

  // remember to set with sbt -DRELEASE_TYPE=snapshot|beta if running a non-prod release
  commonSteps ++ (sys.props.get("RELEASE_TYPE") match {
    case Some(v) if v == snapshotReleaseType => snapshotSteps // this deploys -SNAPSHOT build to sonatype snapshot repo only
    case Some(v) if v == betaReleaseType => betaSteps // this enables a release candidate build to sonatype and Maven
    case None => prodSteps  // our normal deploy route
  })

}
