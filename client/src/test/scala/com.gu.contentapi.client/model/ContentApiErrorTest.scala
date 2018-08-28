package com.gu.contentapi.client.model

import org.scalatest.{FlatSpec, Matchers}

class ContentApiErrorTest extends FlatSpec with Matchers  {
  "ContentApiError" should "Handle error responses properly" in {
    ContentApiError(HttpResponse(Array(), 500, "error")) should be (ContentApiError(500, "error"))
  }
}
