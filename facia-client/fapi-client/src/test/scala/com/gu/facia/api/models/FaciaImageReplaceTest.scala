package com.gu.facia.api.models

import com.gu.facia.api.utils.ResolvedMetaData
import com.gu.facia.client.models.TrailMetaData
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsBoolean, JsObject, JsString}

class FaciaImageReplaceTest extends AnyFlatSpec with Matchers {
  val trailMetaDataWithoutImageReplace =
    TrailMetaData(
      Map(
        "imageReplace" -> JsBoolean(value = false),
        "imageSrc" -> JsString("theImageSrc"),
        "imageSrcWidth" -> JsString("theImageSrcWidth"),
        "imageSrcHeight" -> JsString("theImageSrcHeight")
      )
    )

  val trailMetaDataWithImageReplace =
    TrailMetaData(
      Map(
        "imageReplace" -> JsBoolean(value = true),
        "imageSrc" -> JsString("theImageSrc"),
        "imageSrcWidth" -> JsString("theImageSrcWidth"),
        "imageSrcHeight" -> JsString("theImageSrcHeight")
      )
    )

  val imageSourceAsset = JsObject(
    List(
      "src" -> JsString("theImageSrcAsset"),
      "origin" -> JsString("theImageSrcOrigin"),
      "width" -> JsString("500"),
      "height" -> JsString("300")
    )
  )

  val trailMetaDataWithImageSource =
    TrailMetaData(
      Map(
        "imageReplace" -> JsBoolean(value = true),
        "imageSrc" -> JsString("theImageSrc"),
        "imageSrcWidth" -> JsString("theImageSrcWidth"),
        "imageSrcHeight" -> JsString("theImageSrcHeight"),
        "imageSource" -> imageSourceAsset
      )
    )

  "Image" should "give an Image of type default when it is not set" in {
    val resolvedMetaData =
      ResolvedMetaData.fromTrailMetaData(trailMetaDataWithoutImageReplace)
    FaciaImage.getFaciaImage(
      None,
      trailMetaDataWithoutImageReplace,
      resolvedMetaData
    ) should be(None)
  }

  it should "give back an Image when true" in {
    val resolvedMetaData =
      ResolvedMetaData.fromTrailMetaData(trailMetaDataWithImageReplace)
    FaciaImage.getFaciaImage(
      None,
      trailMetaDataWithImageReplace,
      resolvedMetaData
    ) should be(
      Some(
        Replace("theImageSrc", "theImageSrcWidth", "theImageSrcHeight", None)
      )
    )
  }

  it should "give back an ImageSource when imageSource present in metadata" in {
    val resolvedMetaData =
      ResolvedMetaData.fromTrailMetaData(trailMetaDataWithImageSource)
    FaciaImage.getFaciaImage(
      None,
      trailMetaDataWithImageSource,
      resolvedMetaData
    ) should be(Some(Replace("theImageSrcAsset", "500", "300", None)))
  }
}
