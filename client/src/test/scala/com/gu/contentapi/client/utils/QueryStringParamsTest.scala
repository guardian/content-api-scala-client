package com.gu.contentapi.client.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class QueryStringParamsTest extends AnyFlatSpec with Matchers {

  "QueryStringParams" should "correctly encode GET query string parameters" in {

    val questionableParams = Seq(
      ("foo", "bar"),
      ("withPlus", "1+2=3"),
      ("withPipe", "(tone/analytics|tone/comment)")
    )

    QueryStringParams.apply(questionableParams) should be("?foo=bar&withPlus=1%2B2%3D3&withPipe=%28tone%2Fanalytics%7Ctone%2Fcomment%29")
  }

}
