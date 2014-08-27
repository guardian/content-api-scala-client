package com.gu.openplatform.contentapi.parser

import com.gu.openplatform.contentapi.parser.JsonFileLoader._

class FoldersJsonParserTest extends UnitSpec {

  // generated by:
  // http://content.guardianapis.com/folders.json
  //  ?page-size=2
  lazy val foldersResponse = JsonParser.parseFolders(loadFile("folders.json"))


  "folders endpoint parser" should "parse basic reponse header" in {
    foldersResponse.status should be("ok")
    foldersResponse.userTier should be("free")
  }

  it should "parse pagination" in {
    foldersResponse.startIndex should be(1)
    foldersResponse.pageSize should be(2)
    foldersResponse.currentPage should be(1)
    foldersResponse.pages should be(571)
    foldersResponse.total should be(1142)
  }

  it should "have parse folders correctly" in {
    foldersResponse.results.size should be(2)
    val folder = foldersResponse.results.head
    folder.id should be("folder/traveleditorsindex/travelawards")
    folder.sectionId should be(Some("travel"))
    folder.sectionName should be(Some("Travel"))
    folder.webTitle should be("Travel awards")
    folder.apiUrl should be("http://content.guardianapis.com/folder/traveleditorsindex/travelawards")
  }

}