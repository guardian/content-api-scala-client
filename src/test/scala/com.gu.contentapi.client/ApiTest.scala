package com.gu.contentapi.client

import com.gu.contentapi.client.connection.DispatchAsyncHttp
import org.scalatest.{Matchers, FunSuite}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext


class ApiTest extends FunSuite with Matchers {
  test("should correctly add api key if present") {
    Api.search.parameters.get("api-key") should be (None)

    new Api with DispatchAsyncHttp {
      override val apiKey = Some("abcd")

      override implicit def executionContext: ExecutionContext = ExecutionContext.global
    }.search.parameters.get("api-key") should be (Some("abcd"))
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
