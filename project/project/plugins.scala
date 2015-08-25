import sbt._

object ContentApiClientPlugins extends Build {

  lazy val plugins = Project(
    id = "content-api-client-plugins",
    base = file(".")
  )
  .settings(
    addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5"),
    addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.2"),
    addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.2.1"),
    addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.4.0"),
    addSbtPlugin("com.twitter" %% "scrooge-sbt-plugin" % "3.16.3")
  )

}
