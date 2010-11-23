import java.io.File
import sbt._

class ContentApiClient(info: ProjectInfo) extends DefaultProject(info) {

  val joda = "joda-time" % "joda-time" % "1.6" withSources()
  val httpClient = "commons-httpclient" % "commons-httpclient" % "3.1"

  val lift_json = "net.liftweb" % "lift-json_2.8.0" % "2.1" withSources()

  val scalaTest = "org.scalatest" % "scalatest" % "1.2" % "test" withSources()


  override def compileOptions = super.compileOptions ++ Seq(Unchecked)

  override def managedStyle = ManagedStyle.Maven

  val publishTo =
    if (projectVersion.value.toString.contains("-SNAPSHOT"))
      Resolver.file("guardian github snapshots", new File(System.getProperty("user.home")
            + "/guardian.github.com/maven/repo-snapshots"))
    else
      Resolver.file("guardian github releases", new File(System.getProperty("user.home")
            + "/guardian.github.com/maven/repo-releases"))

  override def packageSrcJar= defaultJarPath("-sources.jar")
  val sourceArtifact = Artifact.sources(artifactID)
  override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageSrc)
}
