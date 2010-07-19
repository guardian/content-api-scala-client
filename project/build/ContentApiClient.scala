import sbt._

class ContentApiClient(info: ProjectInfo) extends DefaultProject(info) with IdeaProject {

  val joda = "joda-time" % "joda-time" % "1.6" withSources()
  val httpClient = "commons-httpclient" % "commons-httpclient" % "3.1"
  
  val scalaTest = "org.scalatest" % "scalatest" % "1.2" % "test" withSources()
}
