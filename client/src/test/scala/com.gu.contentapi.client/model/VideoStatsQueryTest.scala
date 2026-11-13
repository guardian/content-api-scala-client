package com.gu.contentapi.client.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class VideoStatsQueryTest extends AnyFlatSpec with Matchers {

  it should "request overall video stats" in {
    VideoStatsQuery().pathSegment shouldEqual
      "stats/videos"
  }

  it should "request video stats by edition only" in {
    VideoStatsQuery(Some("uk"), None).pathSegment shouldEqual
      "stats/videos/uk"
  }

  it should "request video stats by section only" in {
    VideoStatsQuery(None, Some("sport")).pathSegment shouldEqual
      "stats/videos/sport"
  }

  it should "request video stats by edition/section" in {
    VideoStatsQuery(Some("uk"), Some("sport")).pathSegment shouldEqual
      "stats/videos/uk/sport"
  }
}
