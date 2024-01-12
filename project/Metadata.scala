import sbt._
import sbt.Keys._

object Metadata {
  val ghUser = "guardian"
  val ghProject = "content-api-client"

  lazy val settings = Seq(
    organization := "com.gu",
    organizationName := "Guardian News & Media Ltd",
    organizationHomepage := Some(url("https://www.theguardian.com/")),

    startYear := Some(2017),
    licenses := Seq(License.Apache2),

    scmInfo := Some(ScmInfo(
      url(s"https://github.com/$ghUser/$ghProject"),
      s"scm:git@github.com:$ghUser/$ghProject.git"
    )),

    homepage := scmInfo.value.map(_.browseUrl)
  )

//  val clientDevs =
//    List( Developer(id="jennysivapalan", name="Jenny Sivapalan", email="", url=url("https://github.com/jennysivapalan"))
//        , Developer(id="maxharlow", name="Max Harlow", email="", url=url("https://github.com/maxharlow"))
//        , Developer(id="nicl", name="Nic Long", email="", url=url("https://github.com/nicl"))
//        , Developer(id="mchv", name="Mariot Chauvin", email="", url=url("https://github.com/mchv"))
//        , Developer(id="LATaylor-guardian", name="Luke Taylor", email="", url=url("https://github.com/LATaylor-guardian"))
//        , Developer(id="cb372", name="Chris Birchall", email="", url=url("https://github.com/cb372"))
//        , Developer(id="tomrf1", name="Tom Forbes", email="", url=url("https://github.com/tomrf1"))
//        , Developer(id="regiskuckaertz", name="Regis Kuckaertz", email="", url=url("https://github.com/regiskuckaertz"))
//        , Developer(id="JustinPinner", name="Justin Pinner", email="", url=url("https://github.com/JustinPinner"))
//        )
//
//  val awsDevs =
//    List( Developer(id="tomrf1", name="Tom Forbes", email="", url=url("https://github.com/tomrf1"))
//        )
}