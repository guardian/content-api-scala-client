package com.gu.openplatform.contentapi

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite

class ApiTest extends FunSuite with ShouldMatchers {
  test("should correctly add api key if present") {
    try {
      Api.apiKey = None
      Api.searchQuery.parameters.get("api-key") should be (None)

      Api.apiKey = Some("abcd")
      Api.searchQuery.parameters.get("api-key") should be (Some("abcd"))

    } finally {
      Api.apiKey = None
    }
  }
}