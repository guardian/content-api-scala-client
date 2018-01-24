package com.gu.contentapi.client

import org.scalatest.{FlatSpec, Matchers}

class IAMEncoderSpec extends FlatSpec with Matchers {
  it should "encode a map of query params" in {
    IAMEncoder.encodeParams("a=b c&1=2,3") should be("a=b%20c&1=2%2C3")
  }

  it should "encode a query params string" in {
    IAMEncoder.encodeParams(Map(
      "a" -> Seq("b c"),
      "1" -> Seq("2","3")
    )) should be("a=b%20c&1=2%2C3")
  }
}
