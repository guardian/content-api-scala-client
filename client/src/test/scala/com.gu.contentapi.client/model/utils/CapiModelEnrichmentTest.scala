package com.gu.contentapi.client.model.utils

import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.utils.CapiModelEnrichment._
import com.gu.contentapi.client.utils._
import com.gu.contentapi.client.utils.format._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CapiModelEnrichmentDesignTypeTest extends AnyFlatSpec with MockitoSugar with Matchers {

  def fixture = new {
    val content: Content = mock[Content]
    val tag: Tag = mock[Tag]
    val fields: ContentFields = mock[ContentFields]

    when(fields.displayHint) thenReturn None
    when(content.tags) thenReturn List(tag)
    when(content.fields) thenReturn None
  }

  it should "have a designType of 'Media' when tag type/video is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "type/video"

    f.content.designType shouldEqual Media
  }

  it should "have a designType of 'Media' when tag type/audio is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "type/audio"

    f.content.designType shouldEqual Media
  }

  it should "have a designType of 'Media' when tag type/gallery is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "type/gallery"

    f.content.designType shouldEqual Media
  }

  it should "have a designType of 'Review' when tag tone/reviews is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/reviews"

    f.content.designType shouldEqual Review
  }

  it should "have a designType of 'Review' when tag tone/livereview is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/livereview"

    f.content.designType shouldEqual Review
  }

  it should "have a designType of 'Review' when tag tone/albumreview is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/albumreview"

    f.content.designType shouldEqual Review
  }

  it should "have a designType of 'Comment' when tag tone/comment is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/comment"

    f.content.designType shouldEqual Comment
  }

  it should "have a designType of 'Live' when tag tone/minutebyminute is present and is liveblogging" in {
    val f = fixture

    when(f.tag.id) thenReturn "tone/minutebyminute"
    when(f.fields.liveBloggingNow) thenReturn Some(true)
    when(f.content.fields) thenReturn Some(f.fields)

    f.content.designType shouldEqual Live
  }

  it should "have a designType of 'Article' when tag tone/minutebyminute is present but not live anymore" in {
    val f = fixture

    when(f.tag.id) thenReturn "tone/minutebyminute"
    when(f.fields.liveBloggingNow) thenReturn None
    when(f.content.fields) thenReturn Some(f.fields)

    f.content.designType shouldEqual Article
  }

  it should "have a designType of 'Feature' when tag tone/features is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/features"

    f.content.designType shouldEqual Feature
  }


  it should "have a designType of 'Analysis' when tag tone/analysis is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/analysis"

    f.content.designType shouldEqual Analysis
  }

  it should "have a designType of 'Immersive' when the displayHint field is set to 'immersive'" in {
    val f = fixture

    when(f.tag.id) thenReturn "tone/analysis"
    when(f.content.fields) thenReturn Some(f.fields)
    when(f.fields.displayHint) thenReturn Some("immersive")

    f.content.designType shouldEqual Immersive
  }

  it should "have a designType of 'Quiz' when tag tone/quizzes is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/quizzes"

    f.content.designType shouldEqual Quiz
  }

  it should "have a designType of 'GuardianView' when tag tone/editorials is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/editorials"

    f.content.designType shouldEqual GuardianView
  }

  it should "have a designType of 'Interview' when tag tone/interview is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/interview"

    f.content.designType shouldEqual Interview
  }

  it should "have a designType of 'MatchReport' when tag tone/matchreports is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/matchreports"

    f.content.designType shouldEqual MatchReport
  }

  it should "have a designType of 'Recipe' when tag tone/recipes is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/recipes"

    f.content.designType shouldEqual Recipe
  }

  //test one example of filters being applied in priority order
  it should "return a designType of 'Media' over a designType of 'Comment' where tags for both are present'" in {
    val content = mock[Content]
    val commentTag = mock[Tag]
    val videoTag = mock[Tag]

    when(commentTag.id) thenReturn "tone/comment"
    when(videoTag.id) thenReturn "type/video"
    when(content.fields) thenReturn None
    when(content.tags) thenReturn List(commentTag, videoTag)

    content.designType shouldEqual Media

  }

  it should "have a designType of 'Newsletter' when tag tone/newsletter-tone is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/newsletter-tone"

    f.content.designType shouldEqual Newsletter
  }
}

class CapiModelEnrichmentFormatTest extends AnyFlatSpec with MockitoSugar with Matchers {

  def fixture = new {
    val content: Content = mock[Content]
    val tag: Tag = mock[Tag]
    val fields: ContentFields = mock[ContentFields]

    val blocks: Blocks = mock[Blocks]
    val main: Block = mock[Block]
    val blockElement: BlockElement = mock[BlockElement]
    val imageTypeData: ImageElementFields = mock[ImageElementFields]

    val element: Element = mock[Element]
    val asset: Asset = mock[Asset]
    val assetFields: AssetFields = mock[AssetFields]

    when(fields.displayHint) thenReturn None
    when(content.tags) thenReturn List(tag)
    when(content.fields) thenReturn None
    when(content.pillarName) thenReturn None
    when(content.`type`) thenReturn ContentType.Article

    when(content.blocks) thenReturn None
    when(blocks.main) thenReturn None
    when(main.elements) thenReturn Seq(blockElement)
    when(blockElement.imageTypeData) thenReturn None

    when(content.elements) thenReturn None
    when(element.relation) thenReturn ""
    when(element.`type`) thenReturn ElementType.Text // Are these acceptable empty values?
    when(element.assets) thenReturn Seq(asset)
    when(asset.`type`) thenReturn AssetType.Image // Are these acceptable empty values?
    when(asset.typeData) thenReturn None
    when(assetFields.role) thenReturn None

  }

  behavior of "Format.design"

  it should "have a design of 'PrintShopDesign' when tag artanddesign/series/guardian-print-shop is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "artanddesign/series/guardian-print-shop"

    f.content.design shouldEqual PrintShopDesign
  }

  it should "have a design of PictureDesign when tag artanddesign/series/guardian-print-shop is present on Picture content" in {
    val f = fixture
    when(f.tag.id) thenReturn "artanddesign/series/guardian-print-shop"
    when(f.content.`type`) thenReturn ContentType.Picture

    f.content.design shouldEqual PictureDesign
  }

  it should "have a design of 'AudioDesign' when tag type/audio is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "type/audio"

    f.content.design shouldEqual AudioDesign
  }

  it should "have a design of 'VideoDesign' when tag type/video is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "type/video"

    f.content.design shouldEqual VideoDesign
  }

  it should "have a design of 'GalleryDesign' when tag type/gallery is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "type/gallery"

    f.content.design shouldEqual GalleryDesign
  }

  it should "have a design of 'ReviewDesign' when tag tone/reviews is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/reviews"

    f.content.design shouldEqual ReviewDesign
  }

  it should "have a design of 'ReviewDesign' when tag tone/livereview is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/livereview"

    f.content.design shouldEqual ReviewDesign
  }

  it should "have a design of 'ReviewDesign' when tag tone/albumreview is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/albumreview"

    f.content.design shouldEqual ReviewDesign
  }

  it should "have a design of 'AnalysisDesign' when tag tone/analysis is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/analysis"

    f.content.design shouldEqual AnalysisDesign
  }

  it should "have a design of 'ExplainerDesign' when tag tone/explainers is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/explainers"

    f.content.design shouldEqual ExplainerDesign
  }

  it should "have a design of 'CommentDesign' when tag tone/comment is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/comment"

    f.content.design shouldEqual CommentDesign
  }

  it should "have a design of 'CommentDesign' when tag tone/letters is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/letters"

    f.content.design shouldEqual LetterDesign
  }

  it should "have a design of 'ObituaryDesign' when tag tone/obituaries is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/obituaries"

    f.content.design shouldEqual ObituaryDesign
  }

  it should "have a design of 'FeatureDesign' when tag tone/features is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/features"

    f.content.design shouldEqual FeatureDesign
  }

  it should "have a design of 'RecipeDesign' when tag tone/recipes is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/recipes"

    f.content.design shouldEqual RecipeDesign
  }

  it should "have a design of 'MatchReportDesign' when tag tone/matchreports is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/matchreports"

    f.content.design shouldEqual MatchReportDesign
  }

  it should "have a design of 'InterviewDesign' when tag tone/interview is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/interview"

    f.content.design shouldEqual InterviewDesign
  }

  it should "have a design of 'EditorialDesign' when tag tone/editorials is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/editorials"

    f.content.design shouldEqual EditorialDesign
  }

  it should "have a design of 'QuizDesign' when tag tone/quizzes is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/quizzes"

    f.content.design shouldEqual QuizDesign
  }

  it should "have a design of 'FullPageInteractiveDesign' when ContentType is " +
    "Interactive and display hint is 'fullPageInteractive" in {
    val f = fixture
    when(f.content.`type`) thenReturn ContentType.Interactive
    when(f.fields.displayHint) thenReturn Some("fullPageInteractive")
    when(f.content.fields) thenReturn Some(f.fields)

    f.content.display shouldEqual StandardDisplay
    f.content.design shouldEqual FullPageInteractiveDesign
  }

  it should "have a design of 'FullPageInteractiveDesign' when is legacy " +
    "immersive interactive and display hint is 'immersive" in {
    val f = fixture
    when(f.content.`type`) thenReturn ContentType.Interactive
    when(f.fields.displayHint) thenReturn Some("immersive")
    when(f.content.fields) thenReturn Some(f.fields)
    when(f.fields.creationDate) thenReturn Some(CapiDateTime(1632116952, "2021-09-20T06:49:12Z"))

    f.content.display shouldEqual StandardDisplay
    f.content.design shouldEqual FullPageInteractiveDesign
  }

  it should "not have a design of 'FullPageInteractiveDesign' " +
    "when display hint is 'fullPageInteractive and ContentType is 'Article" in {
    val f = fixture
    when(f.content.`type`) thenReturn ContentType.Article
    when(f.fields.displayHint) thenReturn Some("fullPageInteractive")

    f.content.design shouldEqual ArticleDesign
    f.content.design should not equal FullPageInteractiveDesign
  }

  it should "have a design of 'InteractiveDesign' when ContentType is Interactive" in {
    val f = fixture
    when(f.content.`type`) thenReturn ContentType.Interactive

    f.content.design shouldEqual InteractiveDesign
  }

  it should "have a design of 'PhotoEssayDesign' when displayHint contains photoEssay" in {
    val f = fixture
    when(f.fields.displayHint) thenReturn Some("photoEssay")
    when(f.content.fields) thenReturn Some(f.fields)

    f.content.design shouldEqual PhotoEssayDesign
  }

  it should "have a design of 'LiveBlogDesign' when tag tone/minutebyminute is present and is liveblogging" in {
    val f = fixture

    when(f.tag.id) thenReturn "tone/minutebyminute"
    when(f.fields.liveBloggingNow) thenReturn Some(true)
    when(f.content.fields) thenReturn Some(f.fields)

    f.content.design shouldEqual LiveBlogDesign
  }

  it should "have a design of 'DeadBlogDesign' when tag tone/minutebyminute is present but not live anymore" in {
    val f = fixture

    when(f.tag.id) thenReturn "tone/minutebyminute"
    when(f.fields.liveBloggingNow) thenReturn None
    when(f.content.fields) thenReturn Some(f.fields)

    f.content.design shouldEqual DeadBlogDesign
  }

  it should "have a design of 'ArticleDesign' when no predicates match" in {
    val f = fixture

    f.content.design shouldEqual ArticleDesign
  }

  //test examples of filters being applied in priority order
  it should "return a design of 'VideoDesign' over a design of 'CommentDesign' where tags for both are present'" in {
    val content = mock[Content]
    val commentTag = mock[Tag]
    val videoTag = mock[Tag]

    when(commentTag.id) thenReturn "tone/comment"
    when(videoTag.id) thenReturn "type/video"
    when(content.fields) thenReturn None
    when(content.tags) thenReturn List(commentTag, videoTag)

    content.design shouldEqual VideoDesign
  }

  it should "return a design of 'InterviewDesign' over a design of 'FeatureDesign' where tags for both are present'" in {
    val content = mock[Content]
    val interviewTag = mock[Tag]
    val featureTag = mock[Tag]

    when(interviewTag.id) thenReturn "tone/interview"
    when(featureTag.id) thenReturn "tone/features"
    when(content.fields) thenReturn None
    when(content.tags) thenReturn List(interviewTag, featureTag)

    content.design shouldEqual InterviewDesign
  }

  it should "return a design of 'PhotoEssayDesign' over a design of 'FeatureDesign' where information for both is present'" in {
    val f = fixture

    when(f.tag.id) thenReturn "tone/features"
    when(f.fields.displayHint) thenReturn Some("photoEssay")
    when(f.content.fields) thenReturn Some(f.fields)

    f.content.design shouldEqual PhotoEssayDesign
  }

  it should "return a design of 'ObituaryDesign' over a design of 'FeatureDesign' where information for both is present'" in {
    val f = fixture

    when(f.tag.id) thenReturn "tone/obituaries"
    when(f.fields.displayHint) thenReturn Some("photoEssay")
    when(f.content.fields) thenReturn Some(f.fields)

    f.content.design shouldEqual ObituaryDesign
  }

  it should "have a design of 'NewsletterSignupDesign' when tag info/newsletter-sign-up is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "info/newsletter-sign-up"

    f.content.design shouldEqual NewsletterSignupDesign
  }

  it should "have a design of 'TimelineDesign' when tag tone/timelines is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/timelines"

    f.content.design shouldEqual TimelineDesign
  }

  it should "have a design of 'ProfileDesign' when tag tone/profiles is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/profiles"

    f.content.design shouldEqual ProfileDesign
  }

  behavior of "Format.theme"

  it should "return a theme of 'OpinionPillar' when tag tone/comment is present and has a pillar of 'NewsPillar'" in {
    val f = fixture
    when(f.content.pillarName) thenReturn Some("News")
    when(f.tag.id) thenReturn "tone/comment"

    f.content.theme shouldEqual OpinionPillar
  }

  it should "return a theme of 'OpinionPillar' when tag tone/letters is present and has a pillar of 'NewsPillar'" in {
    val f = fixture
    when(f.content.pillarName) thenReturn Some("News")
    when(f.tag.id) thenReturn "tone/letters"

    f.content.theme shouldEqual OpinionPillar
  }

  it should "return a theme of 'SportPillar' when has a pillarName of 'Sport'" in {
    val f = fixture
    when(f.content.pillarName) thenReturn Some("Sport")

    f.content.theme shouldEqual SportPillar
  }

  it should "return a theme of 'CulturePillar' when has a pillarName of 'Arts'" in {
    val f = fixture
    when(f.content.pillarName) thenReturn Some("Arts")

    f.content.theme shouldEqual CulturePillar
  }

  it should "return a theme of 'CulturePillar' when has a pillarName of 'Books'" in {
    val f = fixture
    when(f.content.pillarName) thenReturn Some("Books")

    f.content.theme shouldEqual CulturePillar
  }

  it should "return a theme of 'LifestylePillar' when has a pillarName of 'Lifestyle'" in {
    val f = fixture
    when(f.content.pillarName) thenReturn Some("Lifestyle")

    f.content.theme shouldEqual LifestylePillar
  }

  it should "return a theme of 'SpecialReportTheme' when tag business/series/undercover-in-the-chicken-industry is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "business/series/undercover-in-the-chicken-industry"
    f.content.theme shouldEqual SpecialReportTheme
  }

  it should "return a theme of 'SpecialReportTheme' when tag business/series/britains-debt-timebomb is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "business/series/britains-debt-timebomb"

    f.content.theme shouldEqual SpecialReportTheme
  }

  it should "return a theme of 'SpecialReportTheme' when tag environment/series/the-polluters is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "environment/series/the-polluters"

    f.content.theme shouldEqual SpecialReportTheme
  }

  it should "return a theme of 'SpecialReportTheme' when tag news/series/hsbc-files is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "news/series/hsbc-files"

    f.content.theme shouldEqual SpecialReportTheme
  }

  it should "return a theme of 'SpecialReportTheme' when tag news/series/panama-papers is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "news/series/panama-papers"

    f.content.theme shouldEqual SpecialReportTheme
  }

  it should "return a theme of 'SpecialReportTheme' when tag us-news/homan-square is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "us-news/homan-square"

    f.content.theme shouldEqual SpecialReportTheme
  }

  it should "return a theme of 'SpecialReportTheme' when tag uk-news/series/the-new-world-of-work is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "uk-news/series/the-new-world-of-work"

    f.content.theme shouldEqual SpecialReportTheme
  }

  it should "return a theme of 'SpecialReportTheme' when tag world/series/the-new-arrivals is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "world/series/the-new-arrivals"

    f.content.theme shouldEqual SpecialReportTheme
  }

  it should "return a theme of 'SpecialReportTheme' when tag news/series/nauru-files is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "news/series/nauru-files"

    f.content.theme shouldEqual SpecialReportTheme
  }

  it should "return a theme of 'SpecialReportTheme' when tag us-news/series/counted-us-police-killings is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "us-news/series/counted-us-police-killings"

    f.content.theme shouldEqual SpecialReportTheme
  }

  it should "return a theme of 'SpecialReportTheme' when tag australia-news/series/healthcare-in-detention is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "australia-news/series/healthcare-in-detention"

    f.content.theme shouldEqual SpecialReportTheme
  }

  it should "return a theme of 'SpecialReportTheme' when tag society/series/this-is-the-nhs is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "society/series/this-is-the-nhs"

    f.content.theme shouldEqual SpecialReportTheme
  }

  it should "return a theme of 'SpecialReportTheme' when it is also an Opinion piece" in {

    val content = mock[Content]
    val commentTag = mock[Tag]
    val specialReportTag = mock[Tag]


    when(specialReportTag.id) thenReturn "society/series/this-is-the-nhs"
    when(commentTag.id) thenReturn "tone/letters"
    when(content.fields) thenReturn None
    when(content.tags) thenReturn List(commentTag, specialReportTag)

    content.theme shouldEqual SpecialReportTheme
  }

  it should "return a theme of 'Labs' when tag tone/advertisement-features is present" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/advertisement-features"

    f.content.theme shouldEqual Labs
  }

  it should "return a theme of 'Labs' when tag tone/advertisement-features is present and any pillarName is set" in {
    val f = fixture
    when(f.tag.id) thenReturn "tone/advertisement-features"
    when(f.content.pillarName) thenReturn Some("Lifestyle")


    f.content.theme shouldEqual Labs
  }

  it should "return a theme of 'NewsPillar' when no predicates match" in {
    val f = fixture
    f.content.theme shouldEqual NewsPillar
  }

  behavior of "Format.display"

  it should "return a display of 'ImmersiveDisplay' when a displayHint of immersive is set" in {
    val f = fixture
    when(f.content.fields) thenReturn Some(f.fields)
    when(f.fields.displayHint) thenReturn Some("immersive")
    f.content.display shouldEqual ImmersiveDisplay
  }

  it should "return a display of 'ImmersiveDisplay' when a displayHint of photoEssay is set" in {
    val f = fixture
    when(f.content.fields) thenReturn Some(f.fields)
    when(f.fields.displayHint) thenReturn Some("photoEssay")
    f.content.display shouldEqual ImmersiveDisplay
  }

  it should "return a display of 'ShowcaseDisplay' when a showcaseImage is set" in {
    val f = fixture

    when(f.content.blocks) thenReturn Some(f.blocks)
    when(f.blocks.main) thenReturn Some(f.main)
    when(f.blockElement.imageTypeData) thenReturn Some(f.imageTypeData)
    when(f.imageTypeData.role) thenReturn Some("showcase")

    f.content.display shouldEqual ShowcaseDisplay

  }

  it should "return a display of 'ShowcaseDisplay' when a showcaseEmbed is set" in {
    val f = fixture

    when(f.content.elements) thenReturn Some(scala.collection.Seq(f.element))
    when(f.element.relation) thenReturn "main"
    when(f.element.`type`) thenReturn ElementType.Embed
    when(f.asset.`type`) thenReturn AssetType.Embed
    when(f.asset.typeData) thenReturn Some(f.assetFields)
    when(f.assetFields.role) thenReturn Some("showcase")

    f.content.display shouldEqual ShowcaseDisplay

  }

  it should "return a display of 'NumberedListDisplay' when a displayHint of numberedList is set" in {
    val f = fixture
    when(f.content.fields) thenReturn Some(f.fields)
    when(f.fields.displayHint) thenReturn Some("numberedList")
    f.content.display shouldEqual NumberedListDisplay
  }

  it should "return a display of 'NumberedListDisplay' when a displayHint of numberedList is set and a showcase element is present" in {
    val f = fixture
    when(f.content.fields) thenReturn Some(f.fields)
    when(f.fields.displayHint) thenReturn Some("numberedList")

    when(f.content.elements) thenReturn Some(scala.collection.Seq(f.element))
    when(f.element.relation) thenReturn "main"
    when(f.element.`type`) thenReturn ElementType.Embed
    when(f.asset.`type`) thenReturn AssetType.Embed
    when(f.asset.typeData) thenReturn Some(f.assetFields)
    when(f.assetFields.role) thenReturn Some("showcase")

    f.content.display shouldEqual NumberedListDisplay
  }

  it should "return a display of 'StandardDisplay' when no predicates are set" in {
    val f = fixture

    f.content.display shouldEqual StandardDisplay
  }
  
  it should "confirm interactive content made in 2021 is legacy interactive content" in {
    val f = fixture
    when(f.content.fields) thenReturn Some(f.fields)
    when(f.fields.creationDate) thenReturn Some(CapiDateTime(1632116952, "2021-09-20T06:49:12Z"))

    publishedBeforeInteractiveImmersiveSwitchover(f.content) shouldEqual true
  }

  it should "confirm interactive content made in 2026 is not legacy interactive content" in {
    val f = fixture
    when(f.content.fields) thenReturn Some(f.fields)
    when(f.fields.creationDate) thenReturn Some(CapiDateTime(1695188952, "2026-09-20T06:49:12Z"))

    publishedBeforeInteractiveImmersiveSwitchover(f.content) shouldEqual false
  }

}
