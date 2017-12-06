package com.gu.contentapi.client.model.utils

import com.gu.contentapi.client.model.v1.{Content, ContentFields, Tag}
import com.gu.contentapi.client.utils.CapiModelEnrichment._
import com.gu.contentapi.client.utils._
import org.mockito.Mockito._
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.mockito.MockitoSugar


class CapiModelEnrichmentTest extends FlatSpec with MockitoSugar with Matchers {

  it should  "have a designType of 'Media' when tag type/video is present" in {

    val content = mock[Content]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("type/video")
    when(content.fields) thenReturn(None)
    when(content.tags) thenReturn(List(tag))

    content.designType shouldEqual(Media)
  }

  it should  "have a designType of 'Media' when tag type/audio is present" in {

    val content = mock[Content]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("type/audio")
    when(content.fields) thenReturn(None)
    when(content.tags) thenReturn(List(tag))

    content.designType shouldEqual(Media)
  }

  it should  "have a designType of 'Media' when tag type/gallery is present" in {

    val content = mock[Content]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("type/gallery")
    when(content.fields) thenReturn(None)
    when(content.tags) thenReturn(List(tag))

    content.designType shouldEqual(Media)
  }

  it should  "have a designType of 'Review' when tag tone/reviews is present" in {

    val content = mock[Content]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("tone/reviews")
    when(content.fields) thenReturn(None)
    when(content.tags) thenReturn(List(tag))

    content.designType shouldEqual(Review)
  }

  it should  "have a designType of 'Review' when tag tone/livereview is present" in {

    val content = mock[Content]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("tone/livereview")
    when(content.fields) thenReturn(None)
    when(content.tags) thenReturn(List(tag))

    content.designType shouldEqual(Review)
  }

  it should  "have a designType of 'Review' when tag tone/albumreview is present" in {

    val content = mock[Content]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("tone/albumreview")
    when(content.fields) thenReturn(None)
    when(content.tags) thenReturn(List(tag))

    content.designType shouldEqual(Review)
  }

  it should  "have a designType of 'Comment' when tag tone/comment is present" in {

    val content = mock[Content]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("tone/comment")
    when(content.fields) thenReturn(None)
    when(content.tags) thenReturn(List(tag))

    content.designType shouldEqual(Comment)
  }

  it should  "have a designType of 'Live' when tag tone/minutebyminute is present" in {

    val content = mock[Content]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("tone/minutebyminute")
    when(content.fields) thenReturn(None)
    when(content.tags) thenReturn(List(tag))

    content.designType shouldEqual(Live)
  }

  it should  "have a designType of 'Feature' when tag tone/features is present" in {

    val content = mock[Content]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("tone/features")
    when(content.fields) thenReturn(None)
    when(content.tags) thenReturn(List(tag))

    content.designType shouldEqual(Feature)
  }


  it should  "have a designType of 'Analysis' when tag tone/analysis is present" in {

    val content = mock[Content]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("tone/analysis")
    when(content.fields) thenReturn(None)
    when(content.tags) thenReturn(List(tag))

    content.designType shouldEqual(Analysis)
  }

  it should "have a designType of 'Immersive' when the displayHint field is set to 'immersive'" in {
    val content = mock[Content]
    val fields = mock[ContentFields]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("tone/analysis")
    when(content.tags) thenReturn(List(tag))
    when(content.fields) thenReturn (Some(fields))
    when(fields.displayHint) thenReturn(Some("immersive"))

    content.designType shouldEqual(Immersive)
  }

  it should "have a designType of 'Quiz' when tag tone/quizzes is present" in {
    val content = mock[Content]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("tone/quizzes")
    when(content.tags) thenReturn(List(tag))
    when(content.fields) thenReturn(None)

    content.designType shouldEqual(Quiz)
  }

  it should "have a designType of 'GuardianView' when tag tone/editorials is present" in {
    val content = mock[Content]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("tone/editorials")
    when(content.tags) thenReturn(List(tag))
    when(content.fields) thenReturn(None)

    content.designType shouldEqual(GuardianView)
  }

  it should "have a designType of 'Interview' when tag tone/interview is present" in {
    val content = mock[Content]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("tone/interview")
    when(content.tags) thenReturn(List(tag))
    when(content.fields) thenReturn(None)

    content.designType shouldEqual(Interview)
  }

  it should "have a designType of 'MatchReport' when tag tone/matchreports is present" in {
    val content = mock[Content]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("tone/matchreports")
    when(content.tags) thenReturn(List(tag))
    when(content.fields) thenReturn(None)

    content.designType shouldEqual(MatchReport)
  }

  it should "have a designType of 'Recipe' when tag tone/recipe is present" in {
    val content = mock[Content]
    val tag = mock[Tag]

    when(tag.id) thenReturn ("tone/recipe")
    when(content.tags) thenReturn(List(tag))
    when(content.fields) thenReturn(None)

    content.designType shouldEqual(Recipe)
  }

  //test one example of filters being applied in priority order
  it should "return a designType of 'Media' over a designType of 'Comment' where tags for both are present'" in {
    val content = mock[Content]
    val commentTag = mock[Tag]
    val videoTag = mock[Tag]

    when(commentTag.id) thenReturn("tone/comment")
    when(videoTag.id) thenReturn("type/video")
    when(content.fields) thenReturn (None)
    when(content.tags) thenReturn( List(commentTag, videoTag))

    content.designType shouldEqual(Media)

  }



}