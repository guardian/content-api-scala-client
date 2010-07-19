package com.gu.openplatform.contentapi.parser

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FeatureSpec
import java.net.URL
import org.joda.time.DateTime
import xml.Elem
import com.gu.openplatform.contentapi.model._

class XmlParserTest extends FeatureSpec with ShouldMatchers {

   protected def scenariosFor(unit: Unit*) {}

  private val searchEndpointXml =
    <response status="ok" total="13976" start-index="0" user-tier="free" page-size="2" current-page="1" pages="6988">
      <results>
        <content id="politics/2008/sep/25/ruthkelly.transport" type="article" section-id="/politics" section-name="Politics" web-publication-date="2008-09-25T00:00:00+01:00" web-title="Accusations fly as Kelly's farewell leaves bad taste" web-url="http://www.guardian.co.uk/politics/2008/sep/25/ruthkelly.transport" api-url="http://api.guardianapis.com/politics/2008/sep/25/ruthkelly.transport">
          <fields>
            <field name="headline">Accusations fly as Kelly's farewell leaves bad taste</field>
            <field name="standfirst">Blairite wing claims dirty tricks by No 10 forced minister to confirm departure</field>
          </fields>
          <tags>
            <tag type="keyword" web-title="Photography" id="artanddesign/photography" api-url="http://api.guardianapis.com/artanddesign/photography" web-url="http://www.guardian.co.uk/artanddesign/photography" section-id="artanddesign" section-name="Art and design"/>
            <tag type="tone" web-title="Reviews" id="tone/reviews" api-url="http://api.guardianapis.com/tone/reviews" web-url="http://www.guardian.co.uk/tone/reviews"/>
          </tags>
          <factboxes>
            <factbox type="book" heading="the bible" picture="http://static.guim.co.uk/thebible.jpg">
              <fields>
                <field name="rating">5 stars</field>
                <field name="author">God</field>
              </fields>
            </factbox>
          </factboxes>
          <media-assets>
            <asset type="picture" rel="body" index="1" file="http://static.guim.co.uk/thebible.jpg">
              <fields>
                <field name="alt-text">alt text</field>
                <field name="caption">caption</field>
              </fields>
            </asset>
          </media-assets>
        </content>
        <content id="politics/2008/sep/04/conservatives33" type="article" section-id="/politics" section-name="Politics" web-publication-date="2008-09-09T00:00:00+01:00" web-title="The new Tories: Simon Jones, Dagenham and Rainham" web-url="http://www.guardian.co.uk/politics/2008/sep/04/conservatives33" api-url="http://api.guardianapis.com/politics/2008/sep/04/conservatives33">
          <fields>
            <field name="headline">Simon Jones, Dagenham and Rainham</field>
            <field name="standfirst">Target seat no 164</field>
          </fields>
          <tags>
            <tag type="contributor" webTitle="Ewen MacAskill" id="profile/ewenmacaskill" api-url="http://api.guardianapis.com/profile/ewenmacaskill" web-url="http://www.guardian.co.uk/profile/ewenmacaskill"/>
            <tag type="keyword" webTitle="US elections 2008" id="world/us-elections-2008" api-url="http://api.guardianapis.com/world/us-elections-2008" web-url="http://www.guardian.co.uk/world/us-elections-2008" section-id="world" section-name="World news"/>
          </tags>
        </content>
      </results>
      <refinement-groups>
        <refinement-group type="keyword">
          <refinements>
            <refinement count="42" refined-url="http://api.guardianapis.com/search?format=xml" display-name="Photography" id="artanddesign/photography" api-url="http://api.guardianapis.com/artanddesign/photography" />
            <refinement count="12" refined-url="http://api.guardianapis.com/search?format=xml" display-name="US elections 2008" id="world/us-elections-2008" api-url="http://api.guardianapis.com/world/us-elections-2008" />
          </refinements>
        </refinement-group>
        <refinement-group type="section">
          <refinements>
            <refinement count="42" refined-url="http://api.guardianapis.com/search?format=xml" display-name="World news" id="world" api-url="http://api.guardianapis.com/world" />
            <refinement count="12" refined-url="http://api.guardianapis.com/search?format=xml" display-name="Art and design" id="artanddesign" api-url="http://api.guardianapis.com/artanddesign" />
          </refinements>
        </refinement-group>
      </refinement-groups>
    </response>

  private val tagEndpointXml =
    <response status="ok" total="13976" start-index="0" user-tier="free" page-size="2" current-page="1" pages="6988">
      <results>
            <tag type="keyword" web-title="Photography" id="artanddesign/photography" api-url="http://api.guardianapis.com/artanddesign/photography" web-url="http://www.guardian.co.uk/artanddesign/photography" section-id="artanddesign" section-name="Art and design"/>
            <tag type="tone" web-title="Reviews" id="tone/reviews" api-url="http://api.guardianapis.com/tone/reviews" web-url="http://www.guardian.co.uk/tone/reviews"/>
      </results>
    </response>
    
  private val sectionEndpointXml =
    <response status="ok" total="2" user-tier="free">
      <results>
            <section web-title="World news" id="world" api-url="http://api.guardianapis.com/world" web-url="http://www.guardian.co.uk/world" />
            <section web-title="Art and design" id="artanddesign" api-url="http://api.guardianapis.com/artanddesign" web-url="http://www.guardian.co.uk/artanddesign"/>
      </results>
    </response>

  private val idEndpointContentXml =
  <response status="ok" user-tier="free">
    <content id="politics/2008/sep/25/ruthkelly.transport" type="article" section-id="/politics" section-name="Politics" web-publication-date="2008-09-25T00:00:00+01:00" web-title="Accusations fly as Kelly's farewell leaves bad taste" web-url="http://www.guardian.co.uk/politics/2008/sep/25/ruthkelly.transport" api-url="http://api.guardianapis.com/politics/2008/sep/25/ruthkelly.transport">
      <fields>
        <field name="headline">Accusations fly as Kelly's farewell leaves bad taste</field>
        <field name="standfirst">Blairite wing claims dirty tricks by No 10 forced minister to confirm departure</field>
      </fields>
      <tags>
          <tag type="keyword" web-title="Photography" id="artanddesign/photography" api-url="http://api.guardianapis.com/artanddesign/photography" web-url="http://www.guardian.co.uk/artanddesign/photography" section-id="artanddesign" section-name="Art and design"/>
          <tag type="tone" web-title="Reviews" id="tone/reviews" api-url="http://api.guardianapis.com/tone/reviews" web-url="http://www.guardian.co.uk/tone/reviews"/>
      </tags>
      <factboxes>
        <factbox type="book" heading="the bible" picture="http://static.guim.co.uk/thebible.jpg">
          <fields>
            <field name="rating">5 stars</field>
            <field name="author">God</field>
          </fields>
        </factbox>
      </factboxes>
      <media-assets>
        <asset type="picture" rel="body" index="1" file="http://static.guim.co.uk/thebible.jpg">
          <fields>
            <field name="alt-text">alt text</field>
            <field name="caption">caption</field>
          </fields>
        </asset>
      </media-assets>
    </content>
  </response>

  private val idEndpointTagXml =
    <response status="ok" total="13976" start-index="0" user-tier="free" page-size="2" current-page="1" pages="6988">
      <tag type="keyword" web-title="Photography" id="artanddesign/photography" api-url="http://api.guardianapis.com/artanddesign/photography" web-url="http://www.guardian.co.uk/artanddesign/photography" section-id="artanddesign" section-name="Art and design"/>
      <results>
        <content id="politics/2008/sep/25/ruthkelly.transport" type="article" section-id="/politics" section-name="Politics" web-publication-date="2008-09-25T00:00:00+01:00" web-title="Accusations fly as Kelly's farewell leaves bad taste" web-url="http://www.guardian.co.uk/politics/2008/sep/25/ruthkelly.transport" api-url="http://api.guardianapis.com/politics/2008/sep/25/ruthkelly.transport">
          <fields>
            <field name="headline">Accusations fly as Kelly's farewell leaves bad taste</field>
            <field name="standfirst">Blairite wing claims dirty tricks by No 10 forced minister to confirm departure</field>
          </fields>
          <tags>
            <tag type="keyword" web-title="Photography" id="artanddesign/photography" api-url="http://api.guardianapis.com/artanddesign/photography" web-url="http://www.guardian.co.uk/artanddesign/photography" section-id="artanddesign" section-name="Art and design"/>
            <tag type="tone" web-title="Reviews" id="tone/reviews" api-url="http://api.guardianapis.com/tone/reviews" web-url="http://www.guardian.co.uk/tone/reviews"/>
          </tags>
        </content>
        <content id="politics/2008/sep/04/conservatives33" type="article" section-id="/politics" section-name="Politics" web-publication-date="2008-09-09T00:00:00+01:00" web-title="The new Tories: Simon Jones, Dagenham and Rainham" web-url="http://www.guardian.co.uk/politics/2008/sep/04/conservatives33" api-url="http://api.guardianapis.com/politics/2008/sep/04/conservatives33">
          <fields>
            <field name="headline">Simon Jones, Dagenham and Rainham</field>
            <field name="standfirst">Target seat no 164</field>
          </fields>
          <tags>
            <tag type="contributor" webTitle="Ewen MacAskill" id="profile/ewenmacaskill" api-url="http://api.guardianapis.com/profile/ewenmacaskill" web-url="http://www.guardian.co.uk/profile/ewenmacaskill"/>
            <tag type="keyword" webTitle="US elections 2008" id="world/us-elections-2008" api-url="http://api.guardianapis.com/world/us-elections-2008" web-url="http://www.guardian.co.uk/world/us-elections-2008" section-id="world" section-name="World news"/>
          </tags>
        </content>
      </results>
    </response>

  private val idEndpointSectionXml =
    <response status="ok" total="13976" start-index="0" user-tier="free" page-size="2" current-page="1" pages="6988">
        <section web-title="World news" id="world" api-url="http://api.guardianapis.com/world" web-url="http://www.guardian.co.uk/world" />
    </response>

  private val camelCaseTestEndpointXml =
    <response status="ok" total="13976" start-index="0" user-tier="free" page-size="2" current-page="1" pages="6988">
      <results>
        <content id="politics/2008/sep/25/ruthkelly.transport" type="article" section-id="/politics" section-name="Politics" web-publication-date="2008-09-25T00:00:00+01:00" web-title="Accusations fly as Kelly's farewell leaves bad taste" web-url="http://www.guardian.co.uk/politics/2008/sep/25/ruthkelly.transport" api-url="http://api.guardianapis.com/politics/2008/sep/25/ruthkelly.transport">
          <fields>
            <field name="duration-minutes">Parser wants to camel case this field's name</field>
            <field name="duration-seconds">Parser wants to camel case this field's name</field>
            <field name="picture-count">Parser wants to camel case this field's name</field>
            <field name="show-notes">Parser wants to camel case this field's name</field>
            <field name="alt-text">Parser wants to camel case this field's name</field>
          </fields>
        </content>
      </results>
    </response>

  def testBasicResponseHeaderCanBeParsed(xmlDescription: String, endpointName: String, endpointXml :Elem) {
    feature("xml parser for " + xmlDescription) {
      scenario("should parse basic response header") {
        val response: Response = XmlParser.parseEndpoint(endpointName, endpointXml)
        response.status should be ("ok")
        response.userTier should be ("free")
      }
    }
  }

  def testPagedResponseHeaderCanBeParsed(xmlDescription: String, endpointName: String, endpointXml :Elem) {
    feature("xml parser for " + xmlDescription) {
      scenario("should parse pagination information from response header") {
        val response: PagedResponse = (XmlParser.parseEndpoint(endpointName, endpointXml)).asInstanceOf[PagedResponse]

        response.startIndex should be (0)
        response.pageSize should be (2)
        response.currentPage should be (1)
        response.pages should be (6988)
        response.total should be (13976)
      }
    }
  }

  def testPagedItemResponseHeaderCanBeParsed(xmlDescription: String, endpointName: String, endpointXml :Elem) {
    feature("xml parser for " + xmlDescription) {
      scenario("should parse pagination information from response header") {
        val response: ItemResponse = (XmlParser.parseEndpoint(endpointName, endpointXml)).asInstanceOf[ItemResponse]

        response.startIndex.get should be (0)
        response.pageSize.get should be (2)
        response.currentPage.get should be (1)
        response.pages.get should be (6988)
        response.total.get should be (13976)
      }
    }
  }

  def testThereAre2ItemsInTheResults(xmlDescription: String, endpointName: String, endpointXml :Elem, resultsSelector :Response => List[Any]) {
    feature("xml parser for " + xmlDescription) {
      scenario("should find 2 entries") {
        val response: Response = XmlParser.parseEndpoint(endpointName, endpointXml)

        resultsSelector(response).length should be (2)
      }
    }
  }

  def testTheStandardArticleIsParsedCorrectly(xmlDescription: String, endpointName: String, endpointXml :Elem, contentSelector :Response => Content) {
    feature("xml parser for " + xmlDescription) {
      scenario("should parse the standard article ") {
        val response: Response = XmlParser.parseEndpoint(endpointName, endpointXml)

        val content :Content = contentSelector(response)

        content.sectionId should be (Some("/politics"))
        content.sectionName should be (Some("Politics"))
        content.webPublicationDate should be (new DateTime(2008, 9, 25, 0, 0, 0, 0))
        content.webTitle should be ("Accusations fly as Kelly's farewell leaves bad taste")
        content.webUrl should be (new URL("http://www.guardian.co.uk/politics/2008/sep/25/ruthkelly.transport"))
        content.apiUrl should be (new URL("http://api.guardianapis.com/politics/2008/sep/25/ruthkelly.transport"))
      }
    }
  }

  def testTheStandardTagIsParsedCorrectly(xmlDescription: String, endpointName: String, endpointXml :Elem, tagSelector :Response => Tag) {
    feature("xml parser for " + xmlDescription) {
      scenario("should parse the standard tag ") {
        val response: Response = XmlParser.parseEndpoint(endpointName, endpointXml)

        val tag :Tag = tagSelector(response)

        tag.id should be ("artanddesign/photography")
        tag.tagType should be ("keyword")
        tag.sectionId.get should be ("artanddesign")
        tag.sectionName.get should be ("Art and design")
        tag.webTitle should be ("Photography")
        tag.webUrl should be (new URL("http://www.guardian.co.uk/artanddesign/photography"))
        tag.apiUrl should be (new URL("http://api.guardianapis.com/artanddesign/photography"))
      }
    }
  }

  def testTheStandardSectionIsParsedCorrectly(xmlDescription: String, endpointName: String, endpointXml :Elem, sectionSelector :Response => Section) {
    feature("xml parser for " + xmlDescription) {
      scenario("should parse the standard section ") {
        val response: Response = XmlParser.parseEndpoint(endpointName, endpointXml)

        val section :Section = sectionSelector(response)

        section.webTitle should be ("World news")
        section.id should be ("world")
        section.webUrl should be (new URL("http://www.guardian.co.uk/world"))
        section.apiUrl should be (new URL("http://api.guardianapis.com/world"))
      }
    }
  }

  def testTheStandardFieldsAreParsedCorrectly(xmlDescription: String, endpointName: String, endpointXml :Elem, contentSelector :Response => Content) {
    feature("xml parser for " + xmlDescription) {
      scenario("should parse the standard fields ") {
        val response: Response = XmlParser.parseEndpoint(endpointName, endpointXml)

        val content :Content = contentSelector(response)

        content.fields should not be (None)
        val fields: Map[String, String] = content.fields
        fields should have size(2)

        fields("headline") should be ("Accusations fly as Kelly's farewell leaves bad taste")
        fields("standfirst") should be ("Blairite wing claims dirty tricks by No 10 forced minister to confirm departure")
      }
    }
  }

  def testTheStandardRefinementGroupIsParsedCorrectly(xmlDescription: String, endpointName: String, endpointXml :Elem, refinementGroupSelector :Response => RefinementGroup) {
    feature("xml parser for " + xmlDescription) {
      scenario("should parse the standard refinement group ") {
        val response: Response = XmlParser.parseEndpoint(endpointName, endpointXml)

        val refinementGroup :RefinementGroup = refinementGroupSelector(response)

        refinementGroup.refinementType should be ("keyword")
      }
    }
  }

  def testTheStandardRefinementIsParsedCorrectly(xmlDescription: String, endpointName: String, endpointXml :Elem, refinementSelector :Response => Refinement) {
    feature("xml parser for " + xmlDescription) {
      scenario("should parse the standard refinement ") {
        val response: Response = XmlParser.parseEndpoint(endpointName, endpointXml)

        val refinement :Refinement = refinementSelector(response)

        refinement.count should be (42)
        refinement.refinedUrl should be (new URL("http://api.guardianapis.com/search?format=xml"))
      }
    }
  }

  def testTheStandardFactboxIsParsedCorrectly(xmlDescription: String, endpointName: String, endpointXml :Elem, factboxSelector :Response => Factbox) {

    feature("xml parser for " + xmlDescription) {
      scenario("should parse the standard factbox ") {
        val response: Response = XmlParser.parseEndpoint(endpointName, endpointXml)

        val factbox :Factbox = factboxSelector(response)

        factbox.factboxType should be ("book")
        factbox.heading.get should be ("the bible")
        factbox.picture.get should be ("http://static.guim.co.uk/thebible.jpg")

        val fields: Map[String, String] = factbox.fields
        fields should have size(2)
        fields("rating") should be ("5 stars")
        fields("author") should be ("God")
      }
    }
  }

  def testTheStandardMediaAssetIsParsedCorrectly(xmlDescription: String, endpointName: String, endpointXml :Elem, mediaSelector :Response => MediaAsset) {

    feature("xml parser for " + xmlDescription) {
      scenario("should parse the standard media asset ") {
        val response: Response = XmlParser.parseEndpoint(endpointName, endpointXml)

        val mediaAsset: MediaAsset = mediaSelector(response)

        mediaAsset.mediaType should be ("picture")
        mediaAsset.relationship should be ("body")
        mediaAsset.index should be (1)
        mediaAsset.file should be ("http://static.guim.co.uk/thebible.jpg")

        val fields: Map[String, String] = mediaAsset.fields
        fields should have size(2)
        fields("altText") should be ("alt text")
        fields("caption") should be ("caption")
      }
    }
  }

  def testCamelCasingForHyphenatedFieldNames(xmlDescription: String, endpointName: String, endpointXml :Elem, contentSelector :Response => Content) {
    feature("xml parser for " + xmlDescription) {
      scenario("should camelcase field names ") {
        val response: Response = XmlParser.parseEndpoint(endpointName, endpointXml)

        val content :Content = contentSelector(response)
        val fields: Map[String, String] = contentSelector(response).fields
        fields should have size(5)

        fields("durationMinutes") should not be (None)
        fields("durationSeconds") should not be (None)
        fields("pictureCount") should not be (None)
        fields("showNotes") should not be (None)
        fields("altText") should not be (None)
      }
    }
  }

  scenariosFor(
    testBasicResponseHeaderCanBeParsed("search endpoint", "search", searchEndpointXml),
    testPagedResponseHeaderCanBeParsed("search endpoint", "search", searchEndpointXml),
    testThereAre2ItemsInTheResults("search endpoint response -> results ", "search", searchEndpointXml, _.asInstanceOf[SearchResponse].results),
    testTheStandardArticleIsParsedCorrectly("search endpoint", "search", searchEndpointXml, _.asInstanceOf[SearchResponse].results.head),
    testThereAre2ItemsInTheResults("search endpoint response -> results -> tags", "search", searchEndpointXml, _.asInstanceOf[SearchResponse].results.head.tags),
    testTheStandardTagIsParsedCorrectly("search endpoint response -> results -> content -> tag", "search", searchEndpointXml, _.asInstanceOf[SearchResponse].results.head.tags.head),
    testTheStandardFieldsAreParsedCorrectly("search endpoint", "search", searchEndpointXml, _.asInstanceOf[SearchResponse].results.head),
    testCamelCasingForHyphenatedFieldNames("search endpoint", "search", camelCaseTestEndpointXml, _.asInstanceOf[SearchResponse].results.head),
    testThereAre2ItemsInTheResults("search endpoint response -> refinementGroups", "search", searchEndpointXml, _.asInstanceOf[SearchResponse].refinementGroups),
    testTheStandardRefinementGroupIsParsedCorrectly("search endpoint response -> refinementGroups -> refinementGroup", "search", searchEndpointXml, _.asInstanceOf[SearchResponse].refinementGroups.head),
    testThereAre2ItemsInTheResults("search endpoint response -> refinementGroups -> refinementGroup -> refinements", "search", searchEndpointXml, _.asInstanceOf[SearchResponse].refinementGroups.head.refinements),
    testTheStandardRefinementIsParsedCorrectly("search endpoint response -> refinementGroups -> refinementGroup -> refinements -> refinement", "search", searchEndpointXml, _.asInstanceOf[SearchResponse].refinementGroups.head.refinements.head),
    testTheStandardFactboxIsParsedCorrectly("search endpoint response -> results -> content -> factboxes", "search", searchEndpointXml, _.asInstanceOf[SearchResponse].results.head.factboxes.head),
    testTheStandardMediaAssetIsParsedCorrectly("search endpoint response -> results -> content -> media-assets", "search", searchEndpointXml, _.asInstanceOf[SearchResponse].results.head.mediaAssets.head)
  )

  scenariosFor(
    testBasicResponseHeaderCanBeParsed("tag endpoint", "tags", tagEndpointXml),
    testPagedResponseHeaderCanBeParsed("tag endpoint", "tags", tagEndpointXml),
    testThereAre2ItemsInTheResults("tag endpoint response -> results", "tags", tagEndpointXml, _.asInstanceOf[TagsResponse].results),
    testTheStandardTagIsParsedCorrectly("tag endpoint response -> results -> tag", "tags", tagEndpointXml, _.asInstanceOf[TagsResponse].results.head)
  )

  scenariosFor(
    testBasicResponseHeaderCanBeParsed("section endpoint", "sections", sectionEndpointXml),
    testThereAre2ItemsInTheResults("section endpoint response -> results", "sections", sectionEndpointXml, _.asInstanceOf[SectionsResponse].results),
    testTheStandardSectionIsParsedCorrectly("section endpoint response -> results -> section", "sections", sectionEndpointXml, _.asInstanceOf[SectionsResponse].results.head)
  )

  scenariosFor(
    testBasicResponseHeaderCanBeParsed("id endpoint returning content", "id", idEndpointContentXml),
    testTheStandardArticleIsParsedCorrectly("id endpoint", "id", idEndpointContentXml, _.asInstanceOf[ItemResponse].content.get),
    testThereAre2ItemsInTheResults("id endpoint response -> content -> tags", "id", idEndpointContentXml, _.asInstanceOf[ItemResponse].content.get.tags),
    testTheStandardTagIsParsedCorrectly("id endpoint response ->  content -> tag", "id", idEndpointContentXml, _.asInstanceOf[ItemResponse].content.get.tags.head),
    testTheStandardFieldsAreParsedCorrectly("id endpoint", "id", idEndpointContentXml, _.asInstanceOf[ItemResponse].content.get),
    testTheStandardFactboxIsParsedCorrectly("id endpoint", "id", idEndpointContentXml, _.asInstanceOf[ItemResponse].content.get.factboxes.head),
    testTheStandardMediaAssetIsParsedCorrectly("id endpoint -> content -> media-assets", "id", idEndpointContentXml, _.asInstanceOf[ItemResponse].content.get.mediaAssets.head)
  )

  scenariosFor(
    testBasicResponseHeaderCanBeParsed("id endpoint returning tag", "id", idEndpointTagXml),
    testTheStandardTagIsParsedCorrectly("id endpoint response -> tag", "id", idEndpointTagXml, _.asInstanceOf[ItemResponse].tag.get),
    testPagedItemResponseHeaderCanBeParsed("id endpoint returning tag", "id", idEndpointTagXml),
    testThereAre2ItemsInTheResults("id endpoint returning tag -> results ", "id", idEndpointTagXml, _.asInstanceOf[ItemResponse].results),
    testTheStandardArticleIsParsedCorrectly("id endpoint returning tag -> results", "id", idEndpointTagXml, _.asInstanceOf[ItemResponse].results.head),
    testThereAre2ItemsInTheResults("id endpoint returning tag -> results -> tags", "id", idEndpointTagXml, _.asInstanceOf[ItemResponse].results.head.tags),
    testTheStandardTagIsParsedCorrectly("id endpoint returning tag -> results -> content -> tag", "id", idEndpointTagXml, _.asInstanceOf[ItemResponse].results.head.tags.head),
    testTheStandardFieldsAreParsedCorrectly("id endpoint returning tag -> results", "id", idEndpointTagXml, _.asInstanceOf[ItemResponse].results.head)
  )

  scenariosFor(
    testBasicResponseHeaderCanBeParsed("id endpoint returning section", "id", idEndpointSectionXml),
    testPagedItemResponseHeaderCanBeParsed("id endpoint returning section", "id", idEndpointSectionXml),
    testTheStandardSectionIsParsedCorrectly("id endpoint response -> section", "id", idEndpointSectionXml, _.asInstanceOf[ItemResponse].section.get)
  )

}