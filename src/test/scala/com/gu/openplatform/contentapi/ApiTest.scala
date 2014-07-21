package com.gu.openplatform.contentapi

import org.scalatest.{Matchers, FunSuite}
import org.joda.time.DateTime
import com.gu.openplatform.contentapi.connection.JavaNetSyncHttp


class ApiTest extends FunSuite with Matchers {
  test("should correctly add api key if present") {
    Api.search.parameters.get("api-key") should be (None)

    object ApiWithKey extends SyncApi with JavaNetSyncHttp {
      override val apiKey = Some("abcd")
    }

    ApiWithKey.search.parameters.get("api-key") should be (Some("abcd"))
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
