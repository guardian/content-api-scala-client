package com.gu.contentapi.client.parser

import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}
import com.gu.contentapi.client.ClientTest

class ItemJsonParserTest extends FlatSpec with Matchers with ClientTest {

  val contentItemResponse = JsonParser.parseItem(loadJson("item-content.json"))
  val tagItemResponse = JsonParser.parseItem(loadJson("item-tag.json"))
  val sectionItemResponse = JsonParser.parseItem(loadJson("item-section.json"))

  "content item parser" should "parse basic response fields" in {
    contentItemResponse.status should be ("ok")
    contentItemResponse.userTier should be ("internal")
    contentItemResponse.total should be (Some(1))
    contentItemResponse.startIndex should be (None)
    contentItemResponse.pageSize should be (None)
    contentItemResponse.currentPage should be (None)
    contentItemResponse.pages should be (None)
  }

  it should "only include a content item" in {
    contentItemResponse.content should not be (None)
    contentItemResponse.tag should be (None)
    contentItemResponse.section should be (None)
    contentItemResponse.edition should be (None)
    contentItemResponse.results.length should be (0)
    contentItemResponse.leadContent.length should be (0)
  }

  it should "parse content fields" in {
    val expectedFields = Map(
      "shortUrl" -> "http://gu.com/p/3d55c",
      "standfirst" -> "Ethical consumers should be aware poor Bolivians can no longer afford their staple grain, due to western demand raising prices",
      "hasStoryPackage" -> "true",
      "lastModified" -> "2014-05-21T13:40:59Z",
      "body" -> "<p>Not long ago, quinoa was just an obscure Peruvian grain you could only buy in wholefood shops. We struggled to pronounce it (it's keen-wa, not qui-no-a), yet it was feted by food lovers as a novel addition to the familiar ranks of couscous and rice. Dieticians clucked over quinoa approvingly because it ticked the low-fat box and fitted in with government healthy eating advice to \"base your meals on starchy foods\".</p><p>Adventurous eaters liked its slightly bitter taste and the little white curls that formed around the grains. Vegans embraced quinoa as a credibly nutritious substitute for meat. Unusual among grains, quinoa has a high protein content (between 14%-18%), and it contains all those pesky, yet essential, amino acids needed for good health that can prove so elusive to vegetarians who prefer not to pop food supplements.</p><p>Sales took off. Quinoa was, in marketing speak, the \"miracle grain of the Andes\", a healthy, right-on, ethical addition to the meat avoider's larder (no dead animals, just a crop that doesn't feel pain). Consequently, the price shot up – it has tripled since 2006 – with more rarified black, red and \"royal\" types commanding particularly handsome premiums.</p><p>But there is an unpalatable truth to face for those of us with a bag of quinoa in the larder. The appetite of countries such as ours for this grain <a href=\"http://www.guardian.co.uk/world/2013/jan/14/quinoa-andes-bolivia-peru-crop\" title=\"\">has pushed up prices to such an extent</a> that poorer people in Peru and Bolivia, for whom it was once a nourishing staple food, can no longer afford to eat it. Imported junk food is cheaper. In Lima, quinoa now costs more than chicken. Outside the cities, and fuelled by overseas demand, the pressure is on to turn land that once produced a portfolio of diverse crops into quinoa monoculture.</p><p>In fact, the quinoa trade is yet another troubling example of a damaging north-south exchange, with well-intentioned health and ethics-led consumers here unwittingly driving poverty there. It's beginning to look like a cautionary tale of how a focus on exporting premium foods can damage the producer country's food security. Feeding our apparently insatiable 365-day-a-year hunger for this luxury vegetable, Peru has also cornered the world market in asparagus. Result? In the arid Ica region where Peruvian asparagus production is concentrated, <a href=\"http://www.guardian.co.uk/environment/2010/sep/15/peru-asparagus-british-wells\" title=\"\">this thirsty export vegetable has depleted the water resources</a> on which local people depend. NGOs report that asparagus labourers toil in sub-standard conditions and cannot afford to feed their children while fat cat exporters and foreign supermarkets cream off the profits. That's the pedigree of all those bunches of pricy spears on supermarket shelves.</p><p>Soya, a foodstuff beloved of the vegan lobby as an alternative to dairy products, is another problematic import, one that drives environmental destruction [see footnote]. Embarrassingly, for those who portray it as a progressive alternative to planet-destroying meat, soya production is now one of the two main causes of deforestation in South America, along with cattle ranching, where vast expanses of forest and grassland have been felled to make way for huge plantations.</p><p>Three years ago, the pioneering <a href=\"http://www.fifediet.co.uk/\" title=\"\">Fife Diet</a>, Europe's biggest local food-eating project, sowed an experimental crop of quinoa. It failed, and the experiment has not been repeated. But the attempt at least recognised the need to strengthen our own food security by lessening our reliance on imported foods, and looking first and foremost to what can be grown, or reared, on our doorstep.</p><p>In this respect, omnivores have it easy. Britain excels in producing meat and dairy foods for them to enjoy. However, a rummage through the shopping baskets of vegetarians and vegans swiftly clocks up the food miles, a consequence of their higher dependency on products imported from faraway places. From tofu and tamari to carob and chickpeas, the axis of the vegetarian shopping list is heavily skewed to global.</p><p>There are promising initiatives: <a href=\"http://hodmedods.co.uk/about/our-products/\" title=\"\">one enterprising Norfolk company</a>, for instance, has just started marketing UK-grown fava beans (the sort used to make falafel) as a protein-rich alternative to meat. But in the case of quinoa, there's a ghastly irony when the Andean peasant's staple grain becomes too expensive at home because it has acquired hero product status among affluent foreigners preoccupied with personal health, animal welfare and reducing their carbon \"foodprint\". Viewed through a lens of food security, our current enthusiasm for quinoa looks increasingly misplaced.</p><p>• This footnote was appended on 17 January 2013. To clarify: while soya is found in a variety of health products, the majority of production - 97% according to the UN report of 2006 - is used for animal feed.</p>",
      "headline" -> "Can vegans stomach the unpalatable truth about quinoa?",
      "trailText" -> "<p><strong>Joanna Blythman: </strong>Ethical consumers should be aware poor Bolivians can no longer afford their staple grain, due to western demand raising prices</p>",
      "shouldHideAdverts" -> "false",
      "publication" -> "theguardian.com",
      "showInRelatedContent" -> "true",
      "wordcount" -> "750",
      "commentable" -> "true",
      "allowUgc" -> "false",
      "isPremoderated" -> "false",
      "byline" -> "Joanna Blythman",
      "productionOffice" -> "UK",
      "liveBloggingNow" -> "false",
      "commentCloseDate" -> "2013-01-19T10:14:00Z"
    )
    contentItemResponse.content.get.fields should be (Some(expectedFields))
  }

  it should "parse content tags" in {
    contentItemResponse.content.get.tags.size should be (18)
    contentItemResponse.content.get.tags.head.id should be ("commentisfree/commentisfree")
    contentItemResponse.content.get.tags.head.webTitle should be ("Comment is free")
    contentItemResponse.content.get.tags.head.`type` should be ("blog")
    contentItemResponse.content.get.tags.head.sectionId should be (Some("commentisfree"))
    contentItemResponse.content.get.tags.head.sectionName should be (Some("Comment is free"))
    contentItemResponse.content.get.tags.head.webUrl should be ("http://www.theguardian.com/commentisfree/commentisfree")
    contentItemResponse.content.get.tags.head.apiUrl should be ("http://content.guardianapis.com/commentisfree/commentisfree")

    // check sections can be optional
    contentItemResponse.content.get.tags(2).id should be ("tone/comment")
    contentItemResponse.content.get.tags(2).sectionId should be (None)

    // check bios and byline images can be optional
    contentItemResponse.content.get.tags(3).id should be ("profile/joannablythman")
    contentItemResponse.content.get.tags(3).bio should be (Some("<p>Joanna Blythman is an food writer, investigative journalist and broadcaster</p>"))
    contentItemResponse.content.get.tags(3).bylineImageUrl should be (Some("http://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2008/10/02/joannablythman_140x140.jpg"))
  }

  it should "parse content elements" in {
    contentItemResponse.content.get.elements.get.size should be (2)
    contentItemResponse.content.get.elements.get.head.id should be ("gu-image-402807041")
    contentItemResponse.content.get.elements.get.head.relation should be ("main")
    contentItemResponse.content.get.elements.get.head.`type` should be ("image")
    contentItemResponse.content.get.elements.get.head.assets.size should be (11)

    // check an asset too
    contentItemResponse.content.get.elements.get.head.assets.head.`type` should be ("image")
    contentItemResponse.content.get.elements.get.head.assets.head.mimeType should be (Some("image/jpeg"))
    contentItemResponse.content.get.elements.get.head.assets.head.file should be (Some("http://static.guim.co.uk/sys-images/Guardian/About/General/2013/1/16/1358330705646/Bolivian-woman-harvesting-002.jpg"))
    val expectedAssetTypeData = Map(
      "source" -> " George Steinmetz/Corbis",
      "photographer" -> "George Steinmetz",
      "altText" -> "Bolivian woman harvesting Quinoa",
      "height" -> "54",
      "credit" -> "George Steinmetz/ George Steinmetz/Corbis",
      "caption" -> "A Bolivian woman harvesting quinoa negro. 'Well-intentioned health and ethics-led consumers here [are] unwittingly driving poverty there.' Photograph: George Steinmetz/ George Steinmetz/Corbis",
      "width" -> "54"
    )
    contentItemResponse.content.get.elements.get.head.assets.head.typeData should be (expectedAssetTypeData)
  }

  it should "parse content references" in {
    contentItemResponse.content.get.references.size should be (0)
  }

  "tag item parser" should "parse basic response fields" in {
    tagItemResponse.status should be ("ok")
    tagItemResponse.userTier should be ("internal")
    tagItemResponse.total.get should be (10814)
    tagItemResponse.startIndex.get should be (1)
    tagItemResponse.pageSize.get should be (10)
    tagItemResponse.currentPage.get should be (1)
    tagItemResponse.pages.get should be (1082)
  }

  it should "include a tag item, results, and lead content" in {
    tagItemResponse.content should be (None)
    tagItemResponse.tag should not be (None)
    tagItemResponse.section should be (None)
    tagItemResponse.edition should be (None)
    tagItemResponse.results.length should not be (0)
    tagItemResponse.leadContent.length should not be (0)
  }

  it should "parse tag" in {
    tagItemResponse.tag.get.id should be ("world/france")
    tagItemResponse.tag.get.webTitle should be ("France")
    tagItemResponse.tag.get.`type` should be ("keyword")
    tagItemResponse.tag.get.sectionId should be (Some("world"))
    tagItemResponse.tag.get.sectionName should be (Some("World news"))
    tagItemResponse.tag.get.webUrl should be ("http://www.theguardian.com/world/france")
    tagItemResponse.tag.get.apiUrl should be ("http://content.guardianapis.com/world/france")
  }

  it should "parse tag results" in {
    tagItemResponse.results.size should be (10)
    tagItemResponse.results.head.webTitle should be ("An awkward interview with Le Corbusier: from the archive, 11 September 1965")
    tagItemResponse.results.head.webPublicationDate should be (new DateTime(2014, 9, 11, 5, 30, 0, 0))
    tagItemResponse.results.head.sectionName should be (Some("Art and design"))
    tagItemResponse.results.head.sectionId should be (Some("artanddesign"))
    tagItemResponse.results.head.id should be ("artanddesign/2014/sep/11/le-corbusier-india-architecture-1965")
    tagItemResponse.results.head.webUrl should be ("http://www.theguardian.com/artanddesign/2014/sep/11/le-corbusier-india-architecture-1965")
    tagItemResponse.results.head.apiUrl should be ("http://content.guardianapis.com/artanddesign/2014/sep/11/le-corbusier-india-architecture-1965")
  }

  it should "parse tag lead content" in {
    tagItemResponse.leadContent.size should be (10)
    tagItemResponse.leadContent.head.webTitle should be ("Former French trade minister failed to pay rent or taxes")
  }

  "section item parser" should "parse basic response fields" in {
    sectionItemResponse.status should be ("ok")
    sectionItemResponse.userTier should be ("internal")
    sectionItemResponse.total.get should be (94450)
    sectionItemResponse.startIndex.get should be (1)
    sectionItemResponse.pageSize.get should be (10)
    sectionItemResponse.currentPage.get should be (1)
    sectionItemResponse.pages.get should be (9445)
  }

  it should "include a section item, edition, and results" in {
    sectionItemResponse.content should be (None)
    sectionItemResponse.tag should be (None)
    sectionItemResponse.section should not be (None)
    sectionItemResponse.edition should not be (None)
    sectionItemResponse.results.length should not be (0)
    sectionItemResponse.leadContent.length should be (0)
  }

  it should "parse section" in {
    sectionItemResponse.section.get.id should be ("commentisfree")
    sectionItemResponse.section.get.webTitle should be ("Comment is free")
    sectionItemResponse.section.get.webUrl should be ("http://www.theguardian.com/commentisfree")
    sectionItemResponse.section.get.apiUrl should be ("http://content.guardianapis.com/commentisfree")
    sectionItemResponse.section.get.editions.length should be (4)
    sectionItemResponse.section.get.editions.last.id should be ("au/commentisfree")
    sectionItemResponse.section.get.editions.last.webTitle should be ("Comment is free")
    sectionItemResponse.section.get.editions.last.code should be ("au")
    sectionItemResponse.section.get.editions.last.webUrl should be ("http://www.theguardian.com/au/commentisfree")
    sectionItemResponse.section.get.editions.last.apiUrl should be ("http://content.guardianapis.com/au/commentisfree")
  }

  it should "parse section edition" in {
    sectionItemResponse.edition.get.id should be ("commentisfree")
    sectionItemResponse.edition.get.webTitle should be ("Comment is free")
    sectionItemResponse.edition.get.code should be ("default")
    sectionItemResponse.edition.get.webUrl should be ("http://www.theguardian.com/commentisfree")
    sectionItemResponse.edition.get.apiUrl should be ("http://content.guardianapis.com/commentisfree")
  }

}
