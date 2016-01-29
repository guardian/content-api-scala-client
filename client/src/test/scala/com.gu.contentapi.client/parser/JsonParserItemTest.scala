package com.gu.contentapi.client.parser

import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.model.{ItemResponse}
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers, OptionValues}
import com.gu.contentapi.client.ClientTest
import com.gu.contentapi.client.utils.CapiModelEnrichment._
import com.gu.storypackage.model.v1.{ArticleType, Group}
import com.gu.contentatom.thrift.atom.quiz.QuizAtom
import com.gu.contentatom.thrift.AtomData

class JsonParserItemTest extends FlatSpec with Matchers with OptionValues with ClientTest {

  val contentItemResponse = JsonParser.parseItem(loadJson("item-content.json"))
  val contentItemWithBlocksResponse = JsonParser.parseItem(loadJson("item-content-with-blocks.json"))
  val contentItemWithCrosswordResponse = JsonParser.parseItem(loadJson("item-content-with-crossword.json"))
  val contentItemWithRichLinkElementResponse = JsonParser.parseItem(loadJson("item-content-with-rich-link-element.json"))
  val contentItemWithMembershipElementResponse = JsonParser.parseItem(loadJson("item-content-with-membership-element.json"))
  val contentItemWithPackageResponse = JsonParser.parseItem(loadJson("item-content-with-package.json"))
  val contentItemWithAtomQuiz = JsonParser.parseItem(loadJson("item-content-with-atom-quiz.json"))
  val contentItemWithAtomViewpoints = JsonParser.parseItem(loadJson("item-content-with-atom-viewpoints.json"))
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
    contentItemResponse.mostViewed.length should be (0)
    contentItemResponse.editorsPicks.length should be (0)
  }

  it should "include the following fields on a content item" in {
    val content = contentItemResponse.content.get
    content.isExpired should be(Some(false))
    content.id should be("commentisfree/2013/jan/16/vegans-stomach-unpalatable-truth-quinoa")
    content.`type` should be(ContentType.Article)
    content.sectionId should be(Some("commentisfree"))
    content.sectionName should be(Some("Comment is free"))

    val expectedWebPublicationDate = CapiDateTime(new DateTime("2013-01-16T10:14:31Z").getMillis)
    content.webPublicationDate should be(Some(expectedWebPublicationDate))

    content.webTitle should be("Can vegans stomach the unpalatable truth about quinoa? | Joanna Blythman")
    content.webUrl should be("http://www.theguardian.com/commentisfree/2013/jan/16/vegans-stomach-unpalatable-truth-quinoa")
    content.apiUrl should be("http://content.guardianapis.com/commentisfree/2013/jan/16/vegans-stomach-unpalatable-truth-quinoa")
  }

  it should "parse content fields" in {
    val contentFields = contentItemResponse.content.get.fields.get

    contentFields.main should be (Some("<figure class=\"element element-image\" data-media-id=\"ccf7c9b185722aee258b7d172760b81e325e9603\"> <img src=\"http://media.guim.co.uk/ccf7c9b185722aee258b7d172760b81e325e9603/333_713_2796_1678/1000.jpg\" alt=\"Enda Kenny and David Cameron together in 2013.\" width=\"1000\" height=\"600\" class=\"gu-image\" /> <figcaption> <span class=\"element-image__caption\">Enda Kenny and David Cameron together in 2013.</span> <span class=\"element-image__credit\">Photograph: WPA Pool/Getty Images</span> </figcaption> </figure>"))
    contentFields.newspaperPageNumber should be (Some(3))
    contentFields.starRating should be (Some(4))
    contentFields.contributorBio should be (Some("EMEA marketing director at xAd"))
    contentFields.membershipAccess should be (Some(MembershipTier.MembersOnly))

    val expectedCreationDate = CapiDateTime(new DateTime("2015-09-04T03:31:41Z").getMillis)
    contentFields.creationDate should be (Some(expectedCreationDate))

    contentFields.displayHint should be (Some("immersive"))

    val expectedFirstPublicationDate = CapiDateTime(new DateTime("2015-09-04T08:33:26Z").getMillis)
    contentFields.firstPublicationDate should be (Some(expectedFirstPublicationDate))

    contentFields.internalComposerCode should be (Some("55e953ebe4b019bd8fe33524"))
    contentFields.internalOctopusCode should be (Some("10745645"))
    contentFields.internalPageCode should be (Some(2364888))
    contentFields.internalStoryPackageCode should be (Some(86755))
    contentFields.isInappropriateForSponsorship should be (Some(false))

    val expectedNewspaperEditionDate = CapiDateTime(new DateTime("2014-11-08T11:15:00Z").getMillis)
    contentFields.newspaperEditionDate should be (Some(expectedNewspaperEditionDate))

    val expectedScheduledPublicationDate = CapiDateTime(new DateTime("2113-11-08T11:15:00Z").getMillis)
    contentFields.scheduledPublicationDate should be (Some(expectedScheduledPublicationDate))

    contentFields.secureThumbnail should be (Some("https://media.guim.co.uk/35bc6e02d111ee24233d0518a5a8bdfc33633370/0_28_3521_2113/140.jpg"))
    contentFields.thumbnail should be (Some("http://media.guim.co.uk/35bc6e02d111ee24233d0518a5a8bdfc33633370/0_28_3521_2113/140.jpg"))
    contentFields.shortUrl should be (Some("http://gu.com/p/3d55c"))
    contentFields.standfirst should be (Some("Ethical consumers should be aware poor Bolivians can no longer afford their staple grain, due to western demand raising prices"))
    contentFields.hasStoryPackage should be (Some(true))

    val expectedLastModified = CapiDateTime(new DateTime("2014-05-21T13:40:59Z").getMillis)
    contentFields.lastModified should be (Some(expectedLastModified))

    contentFields.body should be (Some("<p>Not long ago, quinoa was just an obscure Peruvian grain you could only buy in wholefood shops. We struggled to pronounce it (it's keen-wa, not qui-no-a), yet it was feted by food lovers as a novel addition to the familiar ranks of couscous and rice. Dieticians clucked over quinoa approvingly because it ticked the low-fat box and fitted in with government healthy eating advice to \"base your meals on starchy foods\".</p><p>Adventurous eaters liked its slightly bitter taste and the little white curls that formed around the grains. Vegans embraced quinoa as a credibly nutritious substitute for meat. Unusual among grains, quinoa has a high protein content (between 14%-18%), and it contains all those pesky, yet essential, amino acids needed for good health that can prove so elusive to vegetarians who prefer not to pop food supplements.</p><p>Sales took off. Quinoa was, in marketing speak, the \"miracle grain of the Andes\", a healthy, right-on, ethical addition to the meat avoider's larder (no dead animals, just a crop that doesn't feel pain). Consequently, the price shot up – it has tripled since 2006 – with more rarified black, red and \"royal\" types commanding particularly handsome premiums.</p><p>But there is an unpalatable truth to face for those of us with a bag of quinoa in the larder. The appetite of countries such as ours for this grain <a href=\"http://www.guardian.co.uk/world/2013/jan/14/quinoa-andes-bolivia-peru-crop\" title=\"\">has pushed up prices to such an extent</a> that poorer people in Peru and Bolivia, for whom it was once a nourishing staple food, can no longer afford to eat it. Imported junk food is cheaper. In Lima, quinoa now costs more than chicken. Outside the cities, and fuelled by overseas demand, the pressure is on to turn land that once produced a portfolio of diverse crops into quinoa monoculture.</p><p>In fact, the quinoa trade is yet another troubling example of a damaging north-south exchange, with well-intentioned health and ethics-led consumers here unwittingly driving poverty there. It's beginning to look like a cautionary tale of how a focus on exporting premium foods can damage the producer country's food security. Feeding our apparently insatiable 365-day-a-year hunger for this luxury vegetable, Peru has also cornered the world market in asparagus. Result? In the arid Ica region where Peruvian asparagus production is concentrated, <a href=\"http://www.guardian.co.uk/environment/2010/sep/15/peru-asparagus-british-wells\" title=\"\">this thirsty export vegetable has depleted the water resources</a> on which local people depend. NGOs report that asparagus labourers toil in sub-standard conditions and cannot afford to feed their children while fat cat exporters and foreign supermarkets cream off the profits. That's the pedigree of all those bunches of pricy spears on supermarket shelves.</p><p>Soya, a foodstuff beloved of the vegan lobby as an alternative to dairy products, is another problematic import, one that drives environmental destruction [see footnote]. Embarrassingly, for those who portray it as a progressive alternative to planet-destroying meat, soya production is now one of the two main causes of deforestation in South America, along with cattle ranching, where vast expanses of forest and grassland have been felled to make way for huge plantations.</p><p>Three years ago, the pioneering <a href=\"http://www.fifediet.co.uk/\" title=\"\">Fife Diet</a>, Europe's biggest local food-eating project, sowed an experimental crop of quinoa. It failed, and the experiment has not been repeated. But the attempt at least recognised the need to strengthen our own food security by lessening our reliance on imported foods, and looking first and foremost to what can be grown, or reared, on our doorstep.</p><p>In this respect, omnivores have it easy. Britain excels in producing meat and dairy foods for them to enjoy. However, a rummage through the shopping baskets of vegetarians and vegans swiftly clocks up the food miles, a consequence of their higher dependency on products imported from faraway places. From tofu and tamari to carob and chickpeas, the axis of the vegetarian shopping list is heavily skewed to global.</p><p>There are promising initiatives: <a href=\"http://hodmedods.co.uk/about/our-products/\" title=\"\">one enterprising Norfolk company</a>, for instance, has just started marketing UK-grown fava beans (the sort used to make falafel) as a protein-rich alternative to meat. But in the case of quinoa, there's a ghastly irony when the Andean peasant's staple grain becomes too expensive at home because it has acquired hero product status among affluent foreigners preoccupied with personal health, animal welfare and reducing their carbon \"foodprint\". Viewed through a lens of food security, our current enthusiasm for quinoa looks increasingly misplaced.</p><p>• This footnote was appended on 17 January 2013. To clarify: while soya is found in a variety of health products, the majority of production - 97% according to the UN report of 2006 - is used for animal feed.</p>"))
    contentFields.headline should be (Some("Can vegans stomach the unpalatable truth about quinoa?"))
    contentFields.trailText should be (Some("<p><strong>Joanna Blythman: </strong>Ethical consumers should be aware poor Bolivians can no longer afford their staple grain, due to western demand raising prices</p>"))
    contentFields.shouldHideAdverts should be (Some(false))
    contentFields.publication should be (Some("theguardian.com"))
    contentFields.showInRelatedContent should be (Some(true))
    contentFields.wordcount should be (Some(750))
    contentFields.commentable should be (Some(true))
    contentFields.isPremoderated should be(Some(false))
    contentFields.byline should be (Some("Joanna Blythman"))
    contentFields.productionOffice should be (Some(Office.Uk))
    contentFields.liveBloggingNow should be (Some(false))
    contentFields.legallySensitive should be (Some(false))
    contentFields.sensitive should be (Some(false))
    contentFields.lang should be (Some("en"))

    val expectedCommentCloseDate = CapiDateTime(new DateTime("2013-01-19T10:14:00Z").getMillis)
    contentFields.commentCloseDate should be (Some(expectedCommentCloseDate))
    contentFields.allowUgc should be (Some(false))
  }

  it should "parse content tags" in {
    contentItemResponse.content.get.tags.size should be (18)

    val tag = contentItemResponse.content.get.tags.head
    tag.id should be ("commentisfree/commentisfree")
    tag.webTitle should be ("Comment is free")
    tag.`type` should be (TagType.Blog)
    tag.sectionId should be (Some("commentisfree"))
    tag.sectionName should be (Some("Comment is free"))
    tag.webUrl should be ("http://www.theguardian.com/commentisfree/commentisfree")
    tag.apiUrl should be ("http://content.guardianapis.com/commentisfree/commentisfree")

    // check sections can be optional
    val secondTag = contentItemResponse.content.get.tags(2)
    secondTag.id should be ("tone/comment")
    secondTag.sectionId should be (None)

    // check bios and byline images can be optional
    val thirdTag = contentItemResponse.content.get.tags(3)
    thirdTag.id should be ("profile/joannablythman")
    thirdTag.bio should be (Some("<p>Joanna Blythman is an food writer, investigative journalist and broadcaster</p>"))
    thirdTag.bylineImageUrl should be (Some("http://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2008/10/02/joannablythman_140x140.jpg"))
  }

  it should "parse content elements" in {
    contentItemResponse.content.get.elements.get.size should be (3)

    val element = contentItemResponse.content.get.elements.get.head
    element.id should be ("gu-image-402807041")
    element.relation should be ("main")
    element.`type` should be (ElementType.Image)
    element.assets.size should be (11)

    // check an asset too
    val elementAsset = contentItemResponse.content.get.elements.get.head.assets.head
    elementAsset.`type` should be (AssetType.Image)
    elementAsset.mimeType should be (Some("image/jpeg"))
    elementAsset.file should be (Some("http://static.guim.co.uk/sys-images/Guardian/About/General/2013/1/16/1358330705646/Bolivian-woman-harvesting-002.jpg"))

    val elementAssetFields = elementAsset.typeData.get
    elementAssetFields.source should be (Some("George Steinmetz/Corbis"))
    elementAssetFields.photographer should be (Some("George Steinmetz"))
    elementAssetFields.altText should be (Some("Bolivian woman harvesting Quinoa"))
    elementAssetFields.height should be (Some(54))
    elementAssetFields.credit should be (Some("George Steinmetz/ George Steinmetz/Corbis"))
    elementAssetFields.caption should be (Some("A Bolivian woman harvesting quinoa negro. 'Well-intentioned health and ethics-led consumers here [are] unwittingly driving poverty there.' Photograph: George Steinmetz/ George Steinmetz/Corbis"))
    elementAssetFields.width should be (Some(54))
    elementAssetFields.thumbnailUrl should be (Some("thumbnailUrl"))
    elementAssetFields.role should be (Some("role"))
    elementAssetFields.mediaId should be (Some("mediaId"))
    elementAssetFields.iframeUrl should be (Some("iframeUrl"))
    elementAssetFields.scriptName should be (Some("scriptName"))
    elementAssetFields.scriptUrl should be (Some("scriptUrl"))
    elementAssetFields.html should be (Some("html"))
    elementAssetFields.embedType should be (Some("embedType"))
    elementAssetFields.blockAds should be (Some(false))

    // check audio elements too
    val audioElement = contentItemResponse.content.get.elements.get.apply(2).assets.head
    audioElement.`type` should be (AssetType.Audio)
    audioElement.mimeType should be (Some("audio/mpeg"))
    audioElement.file should be (Some("http://static.guim.co.uk/audio/kip/football/series/footballweekly/1447937149295/5649/FW-nov19-2015.mp3"))

    val audioElementAssetFields = audioElement.typeData.get
    audioElementAssetFields.explicit should be (Some(false))
    audioElementAssetFields.clean should be (Some(true))
    audioElementAssetFields.source should be (Some("guardian.co.uk"))
    audioElementAssetFields.durationMinutes should be (Some(60))
    audioElementAssetFields.durationSeconds should be (Some(17))
  }

  it should "parse content references" in {
    contentItemResponse.content.get.references.size should be (0)
  }

  it should "parse content rights" in {
    val rights = contentItemResponse.content.get.rights.get
    rights.syndicatable should be (true)
    rights.subscriptionDatabases should be (true)
    rights.developerCommunity should be (true)
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
    tagItemResponse.mostViewed.length should be (0)
    tagItemResponse.editorsPicks.length should be (0)
  }

  it should "parse tag" in {
    val tag =  tagItemResponse.tag.get
    tag.id should be ("world/france")
    tag.webTitle should be ("France")
    tag.`type` should be (TagType.Keyword)
    tag.sectionId should be (Some("world"))
    tag.sectionName should be (Some("World news"))
    tag.podcast.get.linkUrl should be ("http://www.theguardian.com/")
    tag.podcast.get.author should be ("theguardian.com")
    tag.podcast.get.copyright should be ("theguardian.com © 2014")
    tag.podcast.get.explicit should be (true)
    tag.podcast.get.image.value should be ("http://www.theguardian.com/images/lol.jpg")
    tag.podcast.get.subscriptionUrl.value should be ("http://phobos.apple.com/WebObjects/MZStore.woa/wa/viewPodcast?id=188674007")
    tag.webUrl should be ("http://www.theguardian.com/world/france")
    tag.apiUrl should be ("http://content.guardianapis.com/world/france")
  }

  it should "parse tag results" in {
    tagItemResponse.results.size should be (10)

    val tagResult = tagItemResponse.results.head
    tagResult.webTitle should be ("An awkward interview with Le Corbusier: from the archive, 11 September 1965")

    val expectedWebPublicationDate = CapiDateTime(new DateTime("2014-09-11T04:30:00Z").getMillis)
    tagResult.webPublicationDate should be (Some(expectedWebPublicationDate))

    tagResult.sectionName should be (Some("Art and design"))
    tagResult.sectionId should be (Some("artanddesign"))
    tagResult.id should be ("artanddesign/2014/sep/11/le-corbusier-india-architecture-1965")
    tagResult.webUrl should be ("http://www.theguardian.com/artanddesign/2014/sep/11/le-corbusier-india-architecture-1965")
    tagResult.apiUrl should be ("http://content.guardianapis.com/artanddesign/2014/sep/11/le-corbusier-india-architecture-1965")
  }

  it should "parse tag lead content" in {
    val leadContent = tagItemResponse.leadContent
    leadContent.size should be (10)
    leadContent.head.webTitle should be ("Former French trade minister failed to pay rent or taxes")
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
    sectionItemResponse.mostViewed.length should not be (0)
    sectionItemResponse.editorsPicks.length should not be (0)
  }

  it should "parse section" in {
    val section = sectionItemResponse.section.get
    section.id should be ("commentisfree")
    section.webTitle should be ("Comment is free")
    section.webUrl should be ("http://www.theguardian.com/commentisfree")
    section.apiUrl should be ("http://content.guardianapis.com/commentisfree")


    section.editions.length should be (4)

    val sectionEdition = section.editions.last
    sectionEdition.id should be ("au/commentisfree")
    sectionEdition.webTitle should be ("Comment is free")
    sectionEdition.code should be ("au")
    sectionEdition.webUrl should be ("http://www.theguardian.com/au/commentisfree")
    sectionEdition.apiUrl should be ("http://content.guardianapis.com/au/commentisfree")
  }

  it should "parse section edition" in {
    val sectionEdition = sectionItemResponse.edition.get
    sectionEdition.id should be ("commentisfree")
    sectionEdition.webTitle should be ("Comment is free")
    sectionEdition.code should be ("default")
    sectionEdition.webUrl should be ("http://www.theguardian.com/commentisfree")
    sectionEdition.apiUrl should be ("http://content.guardianapis.com/commentisfree")
  }


  it should "parse the publication date of content" in {
    val expectedWebPublicationDate = CapiDateTime(new DateTime("2015-04-17T10:21:49Z").getMillis)
    contentItemWithBlocksResponse.content.get.webPublicationDate should be(Some(expectedWebPublicationDate))
  }

  it should "parse the publication dates of blocks" in {
    val mainBlock = contentItemWithBlocksResponse.content.get.blocks.get.main.get
    val expectedFirstPublicationDate = CapiDateTime(new DateTime("2015-04-09T14:27:28.486+01:00").getMillis)
    mainBlock.firstPublishedDate should be(Some(expectedFirstPublicationDate))

    val expectedCreatedDate = CapiDateTime(new DateTime("2015-04-09T14:27:28.486+01:00").getMillis)
    mainBlock.createdDate should be(Some(expectedCreatedDate))

    val expectedLastModifiedDate = CapiDateTime(new DateTime("2015-04-09T14:27:35.492+01:00").getMillis)
    mainBlock.lastModifiedDate should be(Some(expectedLastModifiedDate))
  }

  it should "parse the users of blocks" in {
    val mainBlock = contentItemWithBlocksResponse.content.get.blocks.get.main.get
    mainBlock.createdBy.get.email should be("mariot.chauvin@theguardian.com")
    mainBlock.createdBy.get.firstName should be (None)
    mainBlock.createdBy.get.lastName should be (None)
    mainBlock.lastModifiedBy.get.email should be("david.blishen@theguardian.com")
    mainBlock.lastModifiedBy.get.firstName should be(Some("David"))
    mainBlock.lastModifiedBy.get.lastName should be(Some("Blishen"))
  }

  it should "parse the blocks elements" in {
    val bodyBlock = contentItemWithBlocksResponse.content.get.blocks.get.body.get
    val blockElements = bodyBlock.filter(!_.elements.isEmpty).head.elements

    blockElements.size should be (6)
  }

  it should "parse keyEvent attribute " in {
    val bodyBlocks = contentItemWithBlocksResponse.content.get.blocks.get.body.get
    val keyEvent = bodyBlocks.filter(_.id == "55267da9e4b091b2a1c75fe0").head
    val nonKeyEvent = bodyBlocks.filter(_.id == "55267dc0e4b091b2a1c75fe1").head

    keyEvent.attributes.keyEvent shouldBe Some(true)
    nonKeyEvent.attributes.keyEvent shouldBe None
  }

  it should "parse pinned attribute" in {
    val bodyBlocks = contentItemWithBlocksResponse.content.get.blocks.get.body.get
    val pinned = bodyBlocks.filter(_.id == "55267da9e4b091b2a1c75fe0").head
    val notPinned = bodyBlocks.filter(_.id == "55267dc0e4b091b2a1c75fe1").head

    pinned.attributes.pinned shouldBe Some(true)
    notPinned.attributes.pinned shouldBe None
  }

  it should "parse summary attribute " in {
    val bodyBlocks = contentItemWithBlocksResponse.content.get.blocks.get.body.get
    val noSummary = bodyBlocks.filter(_.id == "55267da9e4b091b2a1c75fe0").head
    val summary = bodyBlocks.filter(_.id == "55267dc0e4b091b2a1c75fe1").head

    summary.attributes.summary shouldBe Some(true)
    noSummary.attributes.summary shouldBe None
  }

  it should "parse a text element for a block" in {
    val textElement = getBlockElementsOfType(contentItemWithBlocksResponse, `type` = ElementType.Text)

    textElement should not be empty
  }

  it should "parse a video element for a block" in {
    val videoElement = getBlockElementsOfType(contentItemWithBlocksResponse, `type` = ElementType.Video)

    videoElement should not be empty
  }

  it should "parse a tweet element for a block" in {
    val tweetElement = getBlockElementsOfType(contentItemWithBlocksResponse, `type` = ElementType.Tweet)

    tweetElement should not be empty
  }

  it should "have the correct typeData for a text element for a block" in {
    val textElement = getBlockElementsOfType(contentItemWithBlocksResponse, `type` = ElementType.Text)
    val textElementFields = textElement.head.textTypeData.get

    textElementFields.html.get should be ("<h2>Embed block</h2>")

  }

  it should "have the correct typeData for a video element for a block" in {
    val videoElement = getBlockElementsOfType(contentItemWithBlocksResponse, `type` = ElementType.Video)
    val videoElementFields = videoElement.head.videoTypeData.get

    videoElementFields.url.get should be ("http://www.youtube.com/watch?v=p0jSGkf4DQc")
    videoElementFields.title.get should be ("The Roots (5 of 5) 2011 Lowlands Festival, Netherlands")
    videoElementFields.description.get should be ("Uploaded by FunkItBlog on 2012-01-03.")
    videoElementFields.html.get should be ("<p>Some html for the video</p>")
  }

  it should "have the correct typeData for a tweet element for a block" in {
    val tweetElement = getBlockElementsOfType(contentItemWithBlocksResponse, `type` = ElementType.Tweet)
    val tweetElementFields = tweetElement.head.tweetTypeData.get

    tweetElementFields.id.get should be ("596605887244083201")
    tweetElementFields.html.get should be ("<p>Some html </p>")
    tweetElementFields.originalUrl.get should be ("https://twitter.com/elenacresci/status/596605887244083201")
    tweetElementFields.source.get should be ("Twitter")
    tweetElementFields.url.get should be ("https://twitter.com/elenacresci/statuses/596605887244083201")
  }

  it should "have the correct typeData for a image element for a block" in {
    val imageElement = getBlockElementsOfType(contentItemWithBlocksResponse, `type` = ElementType.Image)
    val imageElementFields = imageElement.head.imageTypeData.get

    imageElementFields.caption.get should be ("FIFA chief Sepp Blatter leaves at the end of the Asian Football Confederation (AFC) regional Congress on April 30, 2015 in the Bahraini capital Manama. Sepp Blatter closed on a fifth term as FIFA president as a key ally, Asia’s soccer boss, won new powers and silenced dissent at a regional congress in Bahrain. AFP PHOTO / MOHAMMED AL-SHAIKHMOHAMMED AL-SHAIKH/AFP/Getty Images")
    imageElementFields.copyright.get should be ("copyright text")
    imageElementFields.displayCredit.get should be (true)
    imageElementFields.credit.get should be ("Image credit")
    imageElementFields.source.get should be ("AFP/Getty Images")
    imageElementFields.photographer.get should be ("MIKE")
    imageElementFields.alt.get should be ("Sepp Blatter")
    imageElementFields.mediaId.get should be ("e38889cd5697318e26258f29e0036cd9633f2dbf")
    imageElementFields.mediaApiUri.get should be ("https://api.media.test.dev-gutools.co.uk/images/e38889cd5697318e26258f29e0036cd9633f2dbf")
    imageElementFields.picdarUrn.get should be ("GD*52757191")
    imageElementFields.suppliersReference.get should be ("Nic6447359")
    imageElementFields.imageType.get should be ("Photograph")
  }

  it should "have the correct typeData for a audio element for a block" in {
    val audioElement = getBlockElementsOfType(contentItemWithBlocksResponse, `type` = ElementType.Audio)
    val audioElementFields = audioElement.head.audioTypeData.get

    audioElementFields.html.get should be ("<p>html for the audio</p>")
    audioElementFields.caption.get should be ("Audio caption")
    audioElementFields.description.get should be ("Listen to user48736353001 | Explore the largest community of artists, bands, podcasters and creators of music & audio.")
    audioElementFields.credit.get should be ("Audio credit")
    audioElementFields.source.get should be ("Soundcloud")
    audioElementFields.title.get should be ("user48736353001")
    audioElementFields.durationMinutes.get should be (9)
    audioElementFields.durationSeconds.get should be (41)
    audioElementFields.clean.get should be (true)
    audioElementFields.explicit.get should be (false)
  }

  it should "have the correct typeData for a pull quote element for a block" in {
    val pullquoteElement = getBlockElementsOfType(contentItemWithBlocksResponse, `type` = ElementType.Pullquote)
    val pullquoteElementFields = pullquoteElement.head.pullquoteTypeData.get

    pullquoteElementFields.html.get should be ("<h2>text of the pullquote</h2>")
    pullquoteElementFields.attribution.get should be ("Joe Bloggs")
  }

  it should "have the correct typeData for a rich-link element for a block" in {
    val richLinkElement = getBlockElementsOfType(contentItemWithRichLinkElementResponse, `type` = ElementType.RichLink)
    val richLinkElementFields = richLinkElement.head.richLinkTypeData.get

    richLinkElementFields.role.get should be("thumbnail")
    richLinkElementFields.linkText.get should be("Mecca: hajj crush kills hundreds near holy city – live coverage")
    richLinkElementFields.originalUrl.get should be("http://www.theguardian.com/world/live/2015/sep/24/hajj-crush-kills-scores-near-mecca-live-coverage")
    richLinkElementFields.url.get should be("http://www.theguardian.com/world/live/2015/sep/24/hajj-crush-kills-scores-near-mecca-live-coverage")
    richLinkElementFields.linkPrefix.get should be("Related: ")
  }

  it should "have the correct typeData for an interactive element for a block" in {
    val interactiveElement = getBlockElementsOfType(contentItemWithRichLinkElementResponse, `type` = ElementType.Interactive)
    val interactiveElementFields = interactiveElement.head.interactiveTypeData.get

    interactiveElementFields.scriptUrl.get should be("http://interactive.guim.co.uk/embed/iframe-wrapper/0.1/boot.js")
    interactiveElementFields.alt.get should be("Hajj scene of stampede")
    interactiveElementFields.scriptName.get should be("iframe-wrapper")
    interactiveElementFields.html.get should be("""<a href="http://interactive.guim.co.uk/uploader/embed/2015/09/mecca_crush/giv-31114wBYHjoKMce1W/">Hajj scene of stampede</a>""")
    interactiveElementFields.originalUrl.get should be("http://interactive.guim.co.uk/uploader/embed/2015/09/mecca_crush/giv-31114wBYHjoKMce1W/")
    interactiveElementFields.source.get should be("Guardian")
    interactiveElementFields.iframeUrl.get should be("http://interactive.guim.co.uk/uploader/embed/2015/09/mecca_crush/giv-31114wBYHjoKMce1W/")
  }

  it should "have the correct typeData for a membership element for a block" in {
    val membershipElement = getBlockElementsOfType(contentItemWithMembershipElementResponse, `type` = ElementType.Membership)
    val membershipElementFields = membershipElement.head.membershipTypeData.get

    membershipElementFields.venue.get should be("The Scott room")
    membershipElementFields.identifier.get should be("guardian-live")
    membershipElementFields.image.get should be("https://media.guim.co.uk/e50e166a08e4279c352d83fa2f3210186999bd13/0_586_2064_1239/500.jpg")
    membershipElementFields.price.get should be("£10")
    membershipElementFields.start.get.toJodaDateTime should be(new DateTime("2015-09-30T18:00:00Z"))
    membershipElementFields.linkText.get should be("Guardian Live | Guardian Newsroom: Should the UK bomb Syria?")
    membershipElementFields.location.get should be("The Guardian, Kings Place, 90 York Way, London, N1 9GU")
    membershipElementFields.end.get.toJodaDateTime should be(new DateTime("2015-09-30T19:30:00Z"))
    membershipElementFields.originalUrl.get should be("https://membership.theguardian.com/event/guardian-live-guardian-newsroom-should-the-uk-bomb-syria-18761779989")
    membershipElementFields.title.get should be("Guardian Live | Guardian Newsroom: Should the UK bomb Syria?")
    membershipElementFields.linkPrefix.get should be("Membership Event: ")
  }

  it should "deserialize a crossword correctly" in {
    val crossword = contentItemWithCrosswordResponse.content.value.crossword.value
    crossword.`type` should be(CrosswordType.Cryptic)
    crossword.number should be(24623)
    crossword.dimensions.cols should be(15)
    crossword.entries.head.id should be("8-across")
    crossword.entries.head.separatorLocations should be(Some(Map("," -> Seq(4))))
  }

  it should "deserialize a package correctly" in {
    val pkg = contentItemWithPackageResponse.`package`.value
    pkg.packageId should be("I'm packing, I'm packing, I'm pack-pack-packing")
    pkg.articles should have size 2

    pkg.articles(0).metadata.id should be("internal-code/page/2436646")
    pkg.articles(0).metadata.articleType should be(ArticleType.Article)
    pkg.articles(0).metadata.group should be(Group.Included)
    pkg.articles(0).metadata.showMainVideo should be(Some(true))
    pkg.articles(0).metadata.showKickerTag should be(Some(true))
    pkg.articles(0).metadata.byline should be(Some("Haroon Siddique and Chris"))
    pkg.articles(0).metadata.showBoostedHeadline should be(Some(true))
    pkg.articles(0).content.webTitle should be("package article 1")

    pkg.articles(1).metadata.id should be("internal-code/page/2437327")
    pkg.articles(1).metadata.articleType should be(ArticleType.Article)
    pkg.articles(0).metadata.group should be(Group.Included)
    pkg.articles(1).metadata.trailText should be(Some("Sunday attendance also drops to 760,000 as decline continues in face of growing secularism, diversity and Chris"))
    pkg.articles(1).content.webTitle should be("package article 2")
  }

  it should "deserialize an embedded quiz correctly" in {
    val content = contentItemWithAtomQuiz.content.get
    val atoms = content.atoms
    val quiz = atoms.get.quiz.get
    val data = quiz.data.asInstanceOf[AtomData.Quiz].quiz
    val quizContent = data.content

    quiz.id should be("be04fec5-7d6f-46c5-936e-f1260acea63b")
    data.quizType should be("knowledge")
    quizContent.questions should have size 1

    val question = quizContent.questions(0)
    question.questionText should be("Is this a good test quiz?")
    question.answers should have size 2

    question.answers(0).answerText should be("Yes")
    question.answers(0).assets should have size 0
    question.answers(0).weight should be(1)
    question.answers(0).revealText should be(None)

    question.answers(1).answerText should be("No")
    question.answers(1).assets should have size 0
    question.answers(1).weight should be(0)
    question.answers(1).revealText should be(Some("Not bad"))
  }

  it should "deserialize an embedded viewpoints atom correctly" in {
    val content = contentItemWithAtomViewpoints.content.get
    val atoms = content.atoms

    // Check the first viewpoint in array
    val viewpoints1 = atoms.get.viewpoints.get(0)
    val data = viewpoints1.data.asInstanceOf[AtomData.Viewpoints].viewpoints
    val viewpointsContent = data.viewpoints

    viewpoints1.id should be("4")
    data.name should be("Embed test 2")
    viewpointsContent should have size 2

    val firstViewpoint = viewpointsContent(0)
    firstViewpoint.quote should be("If this works I'll be happy, over the moon in fact")
    firstViewpoint.date should be(Some(1452814440000L))
    firstViewpoint.commenter.name should be("Jeb Bush")
    firstViewpoint.commenter.imageUrl should be(Some("http://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2016/1/12/1452598832981/JebBushR.png"))
    firstViewpoint.commenter.description should be(Some("Former Florida governor"))
    firstViewpoint.commenter.party should be(Some("Republican"))

    val secondViewpoint = viewpointsContent(1)
    secondViewpoint.quote should be("I'm all over this atoms stuff, not so hot on teamcity problems,")
    secondViewpoint.date should be(Some(1452641640000L))
    secondViewpoint.commenter.name should be("Hilary Clinton")
    secondViewpoint.commenter.imageUrl should be(Some("http://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2016/1/12/1452598832111/HillaryClintonR.png"))
    secondViewpoint.commenter.description should be(Some("Former secretary of state"))
    secondViewpoint.commenter.party should be(Some("Democrat"))

    // Check the second viewpoint in array
    val viewpoints2 = atoms.get.viewpoints.get(1)
    val data2 = viewpoints2.data.asInstanceOf[AtomData.Viewpoints].viewpoints
    val viewpointsContent2 = data2.viewpoints

    viewpoints2.id should be("1")
    data2.name should be("Embed test viewpoints")
    viewpointsContent2 should have size 1

    val firstViewpoint2 = viewpointsContent2(0)
    firstViewpoint2.quote should be("I'm all over this atoms stuff")
    firstViewpoint2.date should be(Some(1454110440000L))
    firstViewpoint2.commenter.name should be("Hilary Clinton")
    firstViewpoint2.commenter.imageUrl should be(Some("http://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2016/1/12/1452598832111/HillaryClintonR.png"))
    firstViewpoint2.commenter.description should be(Some("Former secretary of state"))
    firstViewpoint2.commenter.party should be(Some("Democrat"))

  }

  private def getBlockElementsOfType(response: ItemResponse, `type`: ElementType): Seq[BlockElement] = {
    val bodyBlock = response.content.get.blocks.get.body.get
    val blockElements = bodyBlock.filter(_.elements.nonEmpty).head.elements
    blockElements.filter(`type` == _.`type`)
  }

}
