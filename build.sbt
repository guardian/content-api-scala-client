name := "content-api-client"

version := "1.22"

organization := "com.gu.openplatform"

scalaVersion := "2.10.0"

resolvers ++= Seq(
  "Guardian GitHub" at "http://guardian.github.com/maven/repo-releases"
)

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "1.6",
  "commons-httpclient" % "commons-httpclient" % "3.1",
  "net.liftweb" %% "lift-json" % "2.5-M4",
  "net.databinder.dispatch" %% "dispatch-core" % "0.9.5",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

publishTo <<= (version) { version: String =>
    val publishType = if (version.endsWith("SNAPSHOT")) "snapshots" else "releases"
    Some(
        Resolver.file(
            "guardian github " + publishType,
            file(System.getProperty("user.home") + "/guardian.github.com/maven/repo-" + publishType)
        )
    )
}

maxErrors := 20

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

scalacOptions ++= Seq("-deprecation", "-unchecked")



