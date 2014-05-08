package com.gu.openplatform.contentapi

import org.scalatest.{Matchers, FunSuite}
import org.joda.time.DateTime


class ApiTest extends FunSuite with Matchers {
  test("should correctly add api key if present") {
    try {
      Api.apiKey = None
      Api.search.parameters.get("api-key") should be (None)

      Api.apiKey = Some("abcd")
      Api.search.parameters.get("api-key") should be (Some("abcd"))

    } finally {
      Api.apiKey = None
    }
  }

  test("should add custom parameters") {
    val now = new DateTime
    val params = Api.search
      .stringParam("myStringParam", "foo")
      .intParam("myIntParam", 3)
      .dateParam("myDateParam", now)
      .boolParam("myBoolParam", true)
      .parameters

    params.get("myStringParam") should be (Some("foo"))
    params.get("myIntParam") should be (Some("3"))
    params.get("myDateParam") should be (Some(now.toString()))
    params.get("myBoolParam") should be (Some("true"))
  }
}
