package com.gu.contentapi.client.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ContentApiErrorTest extends AnyFlatSpec with Matchers  {
  "ContentApiError" should "Handle error responses properly" in {
    ContentApiError(HttpResponse(Array(), 500, "error")) should be (ContentApiError(500, "error"))
  }
}
