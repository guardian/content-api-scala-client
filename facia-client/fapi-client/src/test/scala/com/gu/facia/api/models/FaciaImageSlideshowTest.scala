package com.gu.facia.api.models

import com.gu.facia.api.utils.ResolvedMetaData
import com.gu.facia.client.models.TrailMetaData
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsArray, JsBoolean, JsObject, JsString}

class FaciaImageSlideshowTest extends AnyFlatSpec with Matchers {
  val assetOne = JsObject(
    List(
      "src" -> JsString("theImageSrcOne"),
      "width" -> JsString("theImageSrcWidthOne"),
      "height" -> JsString("theImageSrcHeightOne")
    )
  )

  val assetTwo = JsObject(
    List(
      "src" -> JsString("theImageSrcTwo"),
      "width" -> JsString("theImageSrcWidthTwo"),
      "height" -> JsString("theImageSrcHeightTwo")
    )
  )

  val assetThree = JsObject(
    List(
      "src" -> JsString("theImageSrcThree"),
      "width" -> JsString("theImageSrcWidthThree"),
      "height" -> JsString("theImageSrcHeightThree")
    )
  )

  val assetFourWithCaption = JsObject(
    List(
      "src" -> JsString("theImageSrcFour"),
      "width" -> JsString("theImageSrcWidthFour"),
      "height" -> JsString("theImageSrcHeightFour"),
      "caption" -> JsString("exampleCaption")
    )
  )

  val trailMetaDataWithoutImageSlideshowReplace =
    TrailMetaData(
      Map(
        "imageSlideshowReplace" -> JsBoolean(value = false),
        "slideshow" -> JsArray(List(assetOne, assetTwo, assetThree))
      )
    )

  val trailMetaDataWithImageSlideshowReplace =
    TrailMetaData(
      Map(
        "imageSlideshowReplace" -> JsBoolean(value = true),
        "slideshow" -> JsArray(List(assetOne, assetTwo, assetThree))
      )
    )

  val trailMetaDataWithImageSlideshowReplaceAndCaptionSetOnOneImage =
    TrailMetaData(
      Map(
        "imageSlideshowReplace" -> JsBoolean(value = true),
        "slideshow" -> JsArray(
          List(assetOne, assetTwo, assetThree, assetFourWithCaption)
        )
      )
    )

  val trailMetaDataWithImageSlideshowReplaceAndNoAssets =
    TrailMetaData(
      Map(
        "imageSlideshowReplace" -> JsBoolean(value = true),
        "slideshow" -> JsArray(Nil)
      )
    )

  "Image" should "give a None if when imageSlideshowReplace not set" in {
    val resolvedMetaData = ResolvedMetaData.fromTrailMetaData(
      trailMetaDataWithoutImageSlideshowReplace
    )
    FaciaImage.getFaciaImage(
      None,
      trailMetaDataWithoutImageSlideshowReplace,
      resolvedMetaData
    ) should be(None)
  }

  it should "give back an ImageGallery when true" in {
    val resolvedMetaData =
      ResolvedMetaData.fromTrailMetaData(trailMetaDataWithImageSlideshowReplace)
    FaciaImage.getFaciaImage(
      None,
      trailMetaDataWithImageSlideshowReplace,
      resolvedMetaData
    ) should be(
      Some(
        ImageSlideshow(
          List(
            Replace(
              "theImageSrcOne",
              "theImageSrcWidthOne",
              "theImageSrcHeightOne",
              None
            ),
            Replace(
              "theImageSrcTwo",
              "theImageSrcWidthTwo",
              "theImageSrcHeightTwo",
              None
            ),
            Replace(
              "theImageSrcThree",
              "theImageSrcWidthThree",
              "theImageSrcHeightThree",
              None
            )
          )
        )
      )
    )
  }

  it should "give back an ImageGallery when true, with caption set on one image" in {
    val resolvedMetaData = ResolvedMetaData.fromTrailMetaData(
      trailMetaDataWithImageSlideshowReplaceAndCaptionSetOnOneImage
    )
    FaciaImage.getFaciaImage(
      None,
      trailMetaDataWithImageSlideshowReplaceAndCaptionSetOnOneImage,
      resolvedMetaData
    ) should be(
      Some(
        ImageSlideshow(
          List(
            Replace(
              "theImageSrcOne",
              "theImageSrcWidthOne",
              "theImageSrcHeightOne",
              None
            ),
            Replace(
              "theImageSrcTwo",
              "theImageSrcWidthTwo",
              "theImageSrcHeightTwo",
              None
            ),
            Replace(
              "theImageSrcThree",
              "theImageSrcWidthThree",
              "theImageSrcHeightThree",
              None
            ),
            Replace(
              "theImageSrcFour",
              "theImageSrcWidthFour",
              "theImageSrcHeightFour",
              Some("exampleCaption")
            )
          )
        )
      )
    )
  }

  it should "give back a None when imageSlideshowReplace true with no assets" in {
    val resolvedMetaData = ResolvedMetaData.fromTrailMetaData(
      trailMetaDataWithImageSlideshowReplaceAndNoAssets
    )
    FaciaImage.getFaciaImage(
      None,
      trailMetaDataWithImageSlideshowReplaceAndNoAssets,
      resolvedMetaData
    ) should be(None)
  }
}
