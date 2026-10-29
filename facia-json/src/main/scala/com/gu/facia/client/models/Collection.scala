package com.gu.facia.client.models

import play.api.libs.json._
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import com.gu.facia.client.json.JodaFormat._

case class SlideshowAsset(
    src: String,
    width: String,
    height: String,
    caption: Option[String] = None
)
object SlideshowAsset {
  implicit val slideshowAssetFormat: OFormat[SlideshowAsset] =
    Json.format[SlideshowAsset]
}

case class ImageSourceAsset(
    src: String,
    origin: String,
    width: String,
    height: String
)
object ImageSourceAsset {
  implicit val imageSourceAssetFormat: OFormat[ImageSourceAsset] =
    Json.format[ImageSourceAsset]
}

sealed trait MetaDataCommonFields {
  val json: Map[String, JsValue]

  lazy val headline: Option[String] =
    json.get("headline").flatMap(_.asOpt[String])
  lazy val href: Option[String] = json.get("href").flatMap(_.asOpt[String])
  lazy val snapType: Option[String] =
    json.get("snapType").flatMap(_.asOpt[String])
  lazy val snapCss: Option[String] =
    json.get("snapCss").flatMap(_.asOpt[String])
  lazy val snapUri: Option[String] =
    json.get("snapUri").flatMap(_.asOpt[String])
  lazy val trailText: Option[String] =
    json.get("trailText").flatMap(_.asOpt[String])
  lazy val group: Option[String] = json.get("group").flatMap(_.asOpt[String])
  lazy val imageSrc: Option[String] =
    json.get("imageSrc").flatMap(_.asOpt[String])
  lazy val imageSrcWidth: Option[String] =
    json.get("imageSrcWidth").flatMap(_.asOpt[String])
  lazy val imageSrcHeight: Option[String] =
    json.get("imageSrcHeight").flatMap(_.asOpt[String])
  lazy val isBreaking: Option[Boolean] =
    json.get("isBreaking").flatMap(_.asOpt[Boolean])
  lazy val boostLevel: Option[String] =
    json.get("boostLevel").flatMap(_.asOpt[String])
  lazy val isImmersive: Option[Boolean] =
    json.get("isImmersive").flatMap(_.asOpt[Boolean])
  lazy val imageHide: Option[Boolean] =
    json.get("imageHide").flatMap(_.asOpt[Boolean])
  lazy val imageReplace: Option[Boolean] =
    json.get("imageReplace").flatMap(_.asOpt[Boolean])
  lazy val showMainVideo: Option[Boolean] =
    json.get("showMainVideo").flatMap(_.asOpt[Boolean])
  lazy val showKickerTag: Option[Boolean] =
    json.get("showKickerTag").flatMap(_.asOpt[Boolean])
  lazy val showKickerSection: Option[Boolean] =
    json.get("showKickerSection").flatMap(_.asOpt[Boolean])
  lazy val byline: Option[String] = json.get("byline").flatMap(_.asOpt[String])
  lazy val showByline: Option[Boolean] =
    json.get("showByline").flatMap(_.asOpt[Boolean])
  lazy val customKicker: Option[String] =
    json.get("customKicker").flatMap(_.asOpt[String])
  lazy val showKickerCustom: Option[Boolean] =
    json.get("showKickerCustom").flatMap(_.asOpt[Boolean])
  lazy val imageCutoutReplace: Option[Boolean] =
    json.get("imageCutoutReplace").flatMap(_.asOpt[Boolean])
  lazy val imageCutoutSrc: Option[String] =
    json.get("imageCutoutSrc").flatMap(_.asOpt[String])
  lazy val imageCutoutSrcWidth: Option[String] =
    json.get("imageCutoutSrcWidth").flatMap(_.asOpt[String])
  lazy val imageCutoutSrcHeight: Option[String] =
    json.get("imageCutoutSrcHeight").flatMap(_.asOpt[String])
  lazy val showBoostedHeadline: Option[Boolean] =
    json.get("showBoostedHeadline").flatMap(_.asOpt[Boolean])
  lazy val showQuotedHeadline: Option[Boolean] =
    json.get("showQuotedHeadline").flatMap(_.asOpt[Boolean])
  lazy val excludeFromRss: Option[Boolean] =
    json.get("excludeFromRss").flatMap(_.asOpt[Boolean])
  lazy val imageSlideshowReplace: Option[Boolean] =
    json.get("imageSlideshowReplace").flatMap(_.asOpt[Boolean])
  lazy val videoReplace: Option[Boolean] =
    json.get("videoReplace").flatMap(_.asOpt[Boolean])
  lazy val multimediaSlideshowReplace: Option[Boolean] =
    json.get("multimediaSlideshowReplace").flatMap(_.asOpt[Boolean])
  lazy val slideshow: Option[List[SlideshowAsset]] =
    json
      .get("slideshow")
      .flatMap(_.asOpt[List[SlideshowAsset]])
      .filter(_.nonEmpty)
  lazy val showLivePlayable: Option[Boolean] =
    json.get("showLivePlayable").flatMap(_.asOpt[Boolean])
  lazy val imageSource: Option[ImageSourceAsset] =
    json.get("imageSource").flatMap(_.asOpt[ImageSourceAsset])
  lazy val hideShowMore: Option[Boolean] =
    json.get("hideShowMore").flatMap(_.asOpt[Boolean])
  lazy val atomId: Option[String] = json.get("atomId").flatMap(_.asOpt[String])
  lazy val multimediaSlideshowAtomId: Option[String] =
    json.get("multimediaSlideshowAtomId").flatMap(_.asOpt[String])
  lazy val blockId: Option[String] =
    json.get("blockId").flatMap(_.asOpt[String])
}

object SupportingItemMetaData {
  implicit val flatReads: Reads[SupportingItemMetaData] =
    new Reads[SupportingItemMetaData] {
      override def reads(j: JsValue): JsResult[SupportingItemMetaData] = {
        JsSuccess(
          SupportingItemMetaData(
            j.asOpt[Map[String, JsValue]].getOrElse(Map.empty)
          )
        )
      }
    }

  implicit val flatWrites: Writes[SupportingItemMetaData] =
    new Writes[SupportingItemMetaData] {
      override def writes(o: SupportingItemMetaData): JsValue =
        Json.toJson(o.json)
    }

  val empty = SupportingItemMetaData(Map.empty)
}

case class SupportingItemMetaData(json: Map[String, JsValue]) extends MetaDataCommonFields

object TrailMetaData {
  implicit val flatReads: Reads[TrailMetaData] = new Reads[TrailMetaData] {
    override def reads(j: JsValue): JsResult[TrailMetaData] = {
      JsSuccess(
        TrailMetaData(j.asOpt[Map[String, JsValue]].getOrElse(Map.empty))
      )
    }
  }

  implicit val flatWrites: Writes[TrailMetaData] = new Writes[TrailMetaData] {
    override def writes(o: TrailMetaData): JsValue = Json.toJson(o.json)
  }

  val empty = TrailMetaData(Map.empty)

  def withDefaults(defaults: (String, JsValue)*): TrailMetaData =
    TrailMetaData(defaults.foldLeft(Map.empty[String, JsValue]) { case (m, kv) =>
      m + kv
    })
}

case class TrailMetaData(json: Map[String, JsValue]) extends MetaDataCommonFields {
  lazy val supporting: Option[List[SupportingItem]] =
    json.get("supporting").flatMap(_.asOpt[List[SupportingItem]])
}

sealed trait VariantId
object VariantId {
  case object A extends VariantId
  case object B extends VariantId

  implicit val format: Format[VariantId] = new Format[VariantId] {
    override def reads(json: JsValue): JsResult[VariantId] =
      json.validate[String].flatMap {
        case "A"   => JsSuccess(A)
        case "B"   => JsSuccess(B)
        case other => JsError(s"Unknown VariantId: $other")
      }
    override def writes(o: VariantId): JsValue = o match {
      case A => JsString("A")
      case B => JsString("B")
    }
  }
}

/** @param meta
  *   only contains the fields that are actually being tested, i.e. the fields that differ per variant. Any
  *   field not being tested will not be present here, and consumers should fall back to the trail's own
  *   [[TrailMetaData]] for those.
  */
case class VariantMeta(id: VariantId, meta: TrailMetaData)
object VariantMeta {
  implicit val jsonFormat: OFormat[VariantMeta] = Json.format[VariantMeta]
}

/** Represents an A/B test running on a trail within a collection.
  *
  * @param testUuid
  *   a static reference that persists if the card moves between containers, and can be shared across multiple
  *   Test instances on multiple cards.
  * @param variantMeta
  *   the list of variants for this test, each with its own metadata
  * @param startDate
  *   the start date of the test, in milliseconds since epoch
  * @param expiryDate
  *   the expiry date of the test, in milliseconds since epoch. If the test has expired, it will not be shown
  *   to users.
  * @param createdByName
  *   the name of the person who created the test
  * @param createdByEmail
  *   the email of the person who created the test
  * @param frontsThisTestCanRunOn
  *   the list of fronts that this test can run on.
  * @param hasManuallyEndedOnThisTrail
  *   whether this test has been manually ended on this trail. If true, the test will not be shown to users on
  *   this trail, nor will it report to Ophan, even if it is still active on other trails.
  * @param manuallyEndedOnThisTrailByName
  *   the name of the person who manually ended this test on this trail, if applicable
  * @param manuallyEndedOnThisTrailByEmail
  *   the email of the person who manually ended this test on this trail, if applicable
  * @param manuallyEndedOnThisTrailDate
  *   the date at which the test was manually ended on this trail, if applicable, in milliseconds since epoch
  */
case class Test(
    testUuid: String,
    variantMeta: List[VariantMeta],
    startDate: Option[Long],
    expiryDate: Option[Long],
    createdByName: String,
    createdByEmail: String,
    frontsThisTestCanRunOn: List[String],
    hasManuallyEndedOnThisTrail: Boolean,
    manuallyEndedOnThisTrailByName: Option[String],
    manuallyEndedOnThisTrailByEmail: Option[String],
    manuallyEndedOnThisTrailDate: Option[Long]
)
object Test {
  implicit val jsonFormat: OFormat[Test] = Json.format[Test]
}

object SupportingItem {
  implicit val jsonFormat: OFormat[SupportingItem] = Json.format[SupportingItem]
}

/** @param tests
  *   the list of A/B tests configured on this supporting item. Note that expired tests and tests which have
  *   been manually ended aren't removed from this list. Consumers must check a test's expiry/manually-ended
  *   status themselves.
  */
case class SupportingItem(
    id: String,
    frontPublicationDate: Option[Long],
    publishedBy: Option[String],
    meta: Option[SupportingItemMetaData],
    tests: Option[List[Test]]
) {
  val isSnap: Boolean = id.startsWith("snap/")
  lazy val safeMeta = meta.getOrElse(TrailMetaData.empty)
}

object Trail {
  implicit val jsonFormat: OFormat[Trail] = Json.format[Trail]
  val eventGraphicPrefix: String = "event-graphic/"
}

/** @param tests
  *   the list of A/B tests configured on this trail. Note that expired tests and tests which have been
  *   manually ended aren't removed from this list. Consumers must check a test's expiry/manually-ended status
  *   themselves.
  */
case class Trail(
    id: String,
    frontPublicationDate: Long,
    publishedBy: Option[String],
    meta: Option[TrailMetaData],
    tests: Option[List[Test]]
) {
  val isSnap: Boolean = id.startsWith("snap/")
  val isEventGraphic: Boolean = id.startsWith(Trail.eventGraphicPrefix)
  lazy val safeMeta: TrailMetaData = meta.getOrElse(TrailMetaData.empty)
}

object CollectionJson {
  implicit val jsonFormat: OFormat[CollectionJson] = Json.format[CollectionJson]
}

case class CollectionJson(
    live: List[Trail],
    draft: Option[List[Trail]],
    treats: Option[List[Trail]],
    lastUpdated: DateTime,
    updatedBy: String,
    updatedEmail: String,
    displayName: Option[String],
    href: Option[String],
    previously: Option[List[Trail]],
    targetedTerritory: Option[TargetedTerritory]
)
