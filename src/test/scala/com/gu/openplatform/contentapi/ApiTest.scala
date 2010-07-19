package com.gu.openplatform.contentapi

import connection.Api
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite

class ApiTest extends FunSuite with ShouldMatchers {
  test("should correctly add api key if present") {
    try {
      Api.apiKey = None
      Api.searchQuery.optionalParameters should be ("")

      Api.apiKey = Some("abcd")
      Api.searchQuery.optionalParameters should be ("&api-key=abcd")

    } finally {
      Api.apiKey = None
    }
  }
}