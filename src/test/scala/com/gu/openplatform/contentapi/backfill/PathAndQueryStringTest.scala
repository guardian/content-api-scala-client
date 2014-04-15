package com.gu.openplatform.contentapi.backfill

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

class PathAndQueryStringTest extends FlatSpec with ShouldMatchers {
  "QueryStringExtractor" should "extract parameter path and parameter pairs from a path with a query string" in {
    PathAndQueryString.extract("hello?k1=v1&k2=v2") should be ((
      "hello",
      List(
        "k1" -> "v1",
        "k2" -> "v2"
      )
    ))
  }

  it should "return the path and empty list of parameters for a path without a query string" in {
    PathAndQueryString.extract("hello_world") should be (("hello_world", Nil))
  }
}
