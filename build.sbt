name := "content-api-client"

version := "2.1-SNAPSHOT"

organization := "com.gu.openplatform"

scalaVersion := "2.10.1"

crossScalaVersions := Seq("2.10.1", "2.9.3")

resolvers ++= Seq(
  "Guardian GitHub" at "http://guardian.github.com/maven/repo-releases"
)

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "1.6",
  "commons-httpclient" % "commons-httpclient" % "3.1",
  "net.liftweb" %% "lift-json" % "2.5-RC2" cross CrossVersion.binaryMapped {
    case "2.9.3" => "2.9.2"
    case "2.10.1"  => "2.10"
  },
  "net.databinder.dispatch" %% "dispatch-core" % "0.10.0",
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
