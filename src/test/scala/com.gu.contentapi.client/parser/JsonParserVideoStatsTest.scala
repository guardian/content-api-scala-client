package com.gu.contentapi.client.parser

import com.gu.contentapi.client.ClientTest
import org.scalatest.{Matchers, FlatSpec}

class JsonParserVideoStatsTest  extends FlatSpec with Matchers with ClientTest {

  val videoStatsResponse = JsonParser.parseVideoStats(loadJson("video-stats.json"))

  "video stats parser" should "parse basic response fields" in {
    videoStatsResponse.status should be ("ok")
  }

  it should "parse the mostViewedVideos" in {
    videoStatsResponse.mostViewedVideos.size should be (3)
    val video = videoStatsResponse.mostViewedVideos.head
    video.id should be ("gu-video-5706597fe4b0c18a88913e06")
    video.count should be (98)
  }

}
