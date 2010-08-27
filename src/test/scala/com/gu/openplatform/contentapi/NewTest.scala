
package com.gu.openplatform.contentapi

import connection.Http
import model.json.{SearchResponse}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FeatureSpec

import net.liftweb.json._

class NewTest extends FeatureSpec with ShouldMatchers {

  implicit val formats = net.liftweb.json.DefaultFormats

  feature("new json stuff") {

    scenario("get stuff") {

      val result = Http GET "http://content.guardianapis.com/search.json?show-tags=all"

      println(result.body)

      val json = JsonParser.parse(result.body)

      val parsed = (json \ "response").extract[SearchResponse]

      println(parsed)

    }
  }
}

