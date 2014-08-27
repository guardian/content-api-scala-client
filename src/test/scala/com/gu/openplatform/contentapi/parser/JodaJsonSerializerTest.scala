package com.gu.openplatform.contentapi.parser

import org.joda.time.DateTime
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse


class JodaJsonSerializerTest extends UnitSpec {
  implicit val formats = DefaultFormats + new JodaJsonSerializer

  case class SimpleTest(dt: DateTime)

  val jsonString = """ { "dt":"2010-08-27T10:30:05+01:00" } """

  "joda serializer" should "deserialize date time" in {
    val json = parse(jsonString)
    val result = json.extract[SimpleTest]
    result.dt should be(new DateTime(2010, 8, 27, 10, 30, 5, 0))
  }
}