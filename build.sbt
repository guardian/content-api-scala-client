name := "content-api-client"

version := "1.15-SNAPSHOT"

organization := "com.gu.openplatform"

scalaVersion := "2.9.1"

crossScalaVersions ++= Seq("2.9.0-1", "2.8.1", "2.8.0")

resolvers ++= Seq(
  "Scala Tools Repository" at "http://scala-tools.org/repo-releases",
  "Guardian GitHub" at "http://guardian.github.com/maven/repo-releases"
)

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "1.6",
  "commons-httpclient" % "commons-httpclient" % "3.1",
  "net.liftweb" %% "lift-json" % "2.4-M4"
)

libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
    val scalaTestVersion = sv match {
       case "2.8.0" => "1.3.1.RC2"
       case "2.8.1" => "1.5.1"
       case "2.9.0-1" => "1.6.1"
       case "2.9.1" => "1.6.1"
       case _ => error("Unsupported Scala version " + sv)
    }
    deps :+ ("org.scalatest" %% "scalatest" % scalaTestVersion % "test")
}

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



