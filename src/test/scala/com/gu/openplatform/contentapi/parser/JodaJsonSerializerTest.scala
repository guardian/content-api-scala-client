package com.gu.openplatform.contentapi.parser

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import net.liftweb.json.JsonParser._
import net.liftweb.json.JsonAST._
import org.joda.time.{DateTimeZone, DateTime}


class JodaJsonSerializerTest extends FlatSpec with ShouldMatchers {
  implicit val formats = net.liftweb.json.DefaultFormats + new JodaJsonSerializer

  case class SimpleTest(dt: DateTime)

  val jsonString = """ { "dt":"2010-08-27T10:30:05+01:00" } """

  "joda serializer" should "deserialize date time" in {
    val json = parse(jsonString)
    val result = json.extract[SimpleTest]
    result.dt should be(new DateTime(2010, 8, 27, 10, 30, 5, 0))
  }
}