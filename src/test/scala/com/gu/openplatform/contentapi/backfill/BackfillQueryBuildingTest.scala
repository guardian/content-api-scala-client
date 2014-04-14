package com.gu.openplatform.contentapi.backfill

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import com.gu.openplatform.contentapi.Api

class BackfillQueryBuildingTest extends FlatSpec with ShouldMatchers {
  "BackfillQueryBuilding" should "build search queries correctly" in {
    Api.buildQueryFromString("search?q=cats&show-references=all") should be (Right(
      Api.search.stringParam("q", "cats").stringParam("show-references", "all")
    ))
  }

  it should "build item queries correctly" in {
    Api.buildQueryFromString("commentisfree?show-tags=all&show-fields=all&show-references=all") should be (Left(
      Api.item.itemId("commentisfree").showTags("all").showFields("all").showReferences("all")
    ))
  }
}
