package com.gu.facia.client.models

import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json._

object Backfill {
  implicit val jsonFormat: OFormat[Backfill] = Json.format[Backfill]
}
case class Backfill(
    `type`: String,
    query: String
)

sealed trait Metadata

case object Secondary extends Metadata

case object Canonical extends Metadata

case object Special extends Metadata

case object Breaking extends Metadata

case object Branded extends Metadata

case object DynamoLike extends Metadata

case object LongRunningPalette extends Metadata

case object SombrePalette extends Metadata

case object InvestigationPalette extends Metadata

case object BreakingPalette extends Metadata

case object EventPalette extends Metadata

case object EventAltPalette extends Metadata

case object Podcast extends Metadata

case object UnknownMetadata extends Metadata

case object LongRunningAltPalette extends Metadata

case object SombreAltPalette extends Metadata
case object SpecialReportAltPalette extends Metadata

object Metadata extends StrictLogging {

  val tags: Map[String, Metadata] = Map(
    "Secondary" -> Secondary,
    "Canonical" -> Canonical,
    "Special" -> Special,
    "Breaking" -> Breaking,
    "Branded" -> Branded,
    "DynamoLike" -> DynamoLike,
    "LongRunningPalette" -> LongRunningPalette,
    "SombrePalette" -> SombrePalette,
    "InvestigationPalette" -> InvestigationPalette,
    "BreakingPalette" -> BreakingPalette,
    "EventPalette" -> EventPalette,
    "EventAltPalette" -> EventAltPalette,
    "Podcast" -> Podcast,
    "LongRunningAltPalette" -> LongRunningAltPalette,
    "SombreAltPalette" -> SombreAltPalette,
    "SpecialReportAltPalette" -> SpecialReportAltPalette
  )

  implicit object MetadataFormat extends Format[Metadata] {
    def reads(json: JsValue) = {
      (json \ "type").transform[JsString](Reads.JsStringReads) match {
        case JsSuccess(JsString(string), _) =>
          tags.get(string) match {
            case Some(result) => JsSuccess(result)
            case None         =>
              logger.warn(
                s"Could not convert CollectionTag: $string is of unknown type"
              )
              JsSuccess(UnknownMetadata)
          }
        case _ =>
          JsError("Could not convert CollectionTag: type is not a string")
      }
    }

    def writes(cardStyle: Metadata) = cardStyle match {
      case Secondary          => JsObject(Seq("type" -> JsString("Secondary")))
      case Canonical          => JsObject(Seq("type" -> JsString("Canonical")))
      case Special            => JsObject(Seq("type" -> JsString("Special")))
      case Breaking           => JsObject(Seq("type" -> JsString("Breaking")))
      case Branded            => JsObject(Seq("type" -> JsString("Branded")))
      case DynamoLike         => JsObject(Seq("type" -> JsString("DynamoLike")))
      case LongRunningPalette =>
        JsObject(Seq("type" -> JsString("LongRunningPalette")))
      case SombrePalette        => JsObject(Seq("type" -> JsString("SombrePalette")))
      case InvestigationPalette =>
        JsObject(Seq("type" -> JsString("InvestigationPalette")))
      case BreakingPalette =>
        JsObject(Seq("type" -> JsString("BreakingPalette")))
      case EventPalette    => JsObject(Seq("type" -> JsString("EventPalette")))
      case EventAltPalette =>
        JsObject(Seq("type" -> JsString("EventAltPalette")))
      case Podcast               => JsObject(Seq("type" -> JsString("Podcast")))
      case LongRunningAltPalette =>
        JsObject(Seq("type" -> JsString("LongRunningAltPalette")))
      case SombreAltPalette =>
        JsObject(Seq("type" -> JsString("SombreAltPalette")))
      case SpecialReportAltPalette =>
        JsObject(Seq("type" -> JsString("SpecialReportAltPalette")))
      case UnknownMetadata =>
        JsObject(Seq("type" -> JsString("UnknownMetadata")))
    }
  }
}

sealed trait CollectionPlatform
case object AnyPlatform extends CollectionPlatform
case object WebCollection extends CollectionPlatform
case object AppCollection extends CollectionPlatform

object CollectionPlatform {
  implicit object CollectionPlatformFormat extends Format[CollectionPlatform] {
    def reads(json: JsValue) = json match {
      case JsString("Web") => JsSuccess(WebCollection)
      case JsString("App") => JsSuccess(AppCollection)
      case JsString("Any") => JsSuccess(AnyPlatform)
      case other           =>
        JsError(s"Could not deserialize to a CollectionPlatform type: $other")
    }

    def writes(platform: CollectionPlatform): JsString = platform match {
      case AnyPlatform   => JsString("Any")
      case WebCollection => JsString("Web")
      case AppCollection => JsString("App")
    }
  }
}

sealed trait TargetedTerritory {
  val id: String
}
case object NZTerritory extends TargetedTerritory {
  val id = "NZ"
}
case object USEastCoastTerritory extends TargetedTerritory {
  val id = "US-East-Coast"
}
case object USWestCoastTerritory extends TargetedTerritory {
  val id = "US-West-Coast"
}
case object EU27Territory extends TargetedTerritory {
  val id = "EU-27"
}
case object AUVictoriaTerritory extends TargetedTerritory {
  val id = "AU-VIC"
}
case object AUQueenslandTerritory extends TargetedTerritory {
  val id = "AU-QLD"
}
case object AUNewSouthWalesTerritory extends TargetedTerritory {
  val id = "AU-NSW"
}

case object UnknownTerritory extends TargetedTerritory {
  val id = "XX"
}

object TargetedTerritory {
  val allTerritories: List[TargetedTerritory] = List(
    NZTerritory,
    USEastCoastTerritory,
    USWestCoastTerritory,
    EU27Territory,
    AUVictoriaTerritory,
    AUQueenslandTerritory,
    AUNewSouthWalesTerritory
  )

  implicit object TargetedTerritoryFormat extends Format[TargetedTerritory] {
    def reads(json: JsValue): JsResult[TargetedTerritory] = json match {
      case JsString(NZTerritory.id)           => JsSuccess(NZTerritory)
      case JsString(USEastCoastTerritory.id)  => JsSuccess(USEastCoastTerritory)
      case JsString(USWestCoastTerritory.id)  => JsSuccess(USWestCoastTerritory)
      case JsString(EU27Territory.id)         => JsSuccess(EU27Territory)
      case JsString(AUVictoriaTerritory.id)   => JsSuccess(AUVictoriaTerritory)
      case JsString(AUQueenslandTerritory.id) =>
        JsSuccess(AUQueenslandTerritory)
      case JsString(AUNewSouthWalesTerritory.id) =>
        JsSuccess(AUNewSouthWalesTerritory)
      case JsString(UnknownTerritory.id) => JsSuccess(UnknownTerritory)
      case JsString(value)               => JsSuccess(UnknownTerritory)
      case _                             => JsError("Territory must be a string")
    }
    def writes(territory: TargetedTerritory): JsString = territory match {
      case NZTerritory              => JsString(NZTerritory.id)
      case USEastCoastTerritory     => JsString(USEastCoastTerritory.id)
      case USWestCoastTerritory     => JsString(USWestCoastTerritory.id)
      case EU27Territory            => JsString(EU27Territory.id)
      case AUVictoriaTerritory      => JsString(AUVictoriaTerritory.id)
      case AUQueenslandTerritory    => JsString(AUQueenslandTerritory.id)
      case AUNewSouthWalesTerritory => JsString(AUNewSouthWalesTerritory.id)
      case _                        => JsString(UnknownTerritory.id)
    }
  }
}

object FrontsToolSettings {
  implicit val jsonFormat: OFormat[FrontsToolSettings] =
    Json.format[FrontsToolSettings]
}

case class FrontsToolSettings(
    displayEditWarning: Option[Boolean]
)

object DisplayHintsJson {
  implicit val jsonFormat: OFormat[DisplayHintsJson] =
    Json.format[DisplayHintsJson]
}

case class DisplayHintsJson(
    maxItemsToDisplay: Option[Int],
    suppressImages: Option[Boolean] = None
)

object GroupConfigJson {
  implicit val jsonFormat: OFormat[GroupConfigJson] =
    Json.format[GroupConfigJson]
}

case class GroupConfigJson(name: String, maxItems: Option[Int])

object CollectionConfigJson {
  implicit val jsonFormat: OFormat[CollectionConfigJson] =
    Json.format[CollectionConfigJson]

  val emptyConfig: CollectionConfigJson = withDefaults(
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None
  )

  def withDefaults(
      displayName: Option[String] = None,
      backfill: Option[Backfill] = None,
      metadata: Option[List[Metadata]] = None,
      `type`: Option[String] = None,
      href: Option[String] = None,
      description: Option[String] = None,
      groups: Option[List[String]] = None,
      groupsConfig: Option[List[GroupConfigJson]] = None,
      uneditable: Option[Boolean] = None,
      showTags: Option[Boolean] = None,
      showSections: Option[Boolean] = None,
      hideKickers: Option[Boolean] = None,
      showDateHeader: Option[Boolean] = None,
      showLatestUpdate: Option[Boolean] = None,
      excludeFromRss: Option[Boolean] = None,
      showTimestamps: Option[Boolean] = None,
      hideShowMore: Option[Boolean] = None,
      displayHints: Option[DisplayHintsJson] = None,
      userVisibility: Option[String] = None,
      targetedTerritory: Option[TargetedTerritory] = None,
      platform: Option[CollectionPlatform] = None,
      frontsToolSettings: Option[FrontsToolSettings] = None
  ): CollectionConfigJson = CollectionConfigJson(
    displayName,
    backfill,
    metadata,
    `type`,
    href,
    description,
    groups,
    groupsConfig,
    uneditable,
    showTags,
    showSections,
    hideKickers,
    showDateHeader,
    showLatestUpdate,
    excludeFromRss,
    showTimestamps,
    hideShowMore,
    displayHints,
    userVisibility,
    targetedTerritory,
    platform,
    frontsToolSettings
  )
}

case class CollectionConfigJson(
    displayName: Option[String],
    backfill: Option[Backfill],
    metadata: Option[List[Metadata]],
    `type`: Option[String],
    href: Option[String],
    description: Option[String],
    groups: Option[List[String]],
    groupsConfig: Option[List[GroupConfigJson]],
    uneditable: Option[Boolean],
    showTags: Option[Boolean],
    showSections: Option[Boolean],
    hideKickers: Option[Boolean],
    showDateHeader: Option[Boolean],
    showLatestUpdate: Option[Boolean],
    excludeFromRss: Option[Boolean],
    showTimestamps: Option[Boolean],
    hideShowMore: Option[Boolean],
    displayHints: Option[DisplayHintsJson],
    userVisibility: Option[String],
    targetedTerritory: Option[TargetedTerritory],
    platform: Option[CollectionPlatform],
    frontsToolSettings: Option[FrontsToolSettings]
) {
  val collectionType = `type`
}

object FrontJson {
  implicit val jsonFormat: OFormat[FrontJson] = Json.format[FrontJson]
}

case class FrontJson(
    collections: List[String],
    navSection: Option[String],
    webTitle: Option[String],
    title: Option[String],
    description: Option[String],
    onPageDescription: Option[String],
    imageUrl: Option[String],
    imageWidth: Option[Int],
    imageHeight: Option[Int],
    isImageDisplayed: Option[Boolean],
    priority: Option[String],
    isHidden: Option[Boolean],
    canonical: Option[String],
    group: Option[String]
)

object ConfigJson {
  implicit val jsonFormat: OFormat[ConfigJson] = Json.format[ConfigJson]
  def empty = ConfigJson(Map.empty, Map.empty)
}

case class ConfigJson(
    fronts: Map[String, FrontJson],
    collections: Map[String, CollectionConfigJson]
)
