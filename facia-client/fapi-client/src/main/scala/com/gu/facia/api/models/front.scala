package com.gu.facia.api.models

import com.gu.facia.client.models.{ConfigJson, FrontJson}

sealed trait FrontPriority
object EditorialPriority extends FrontPriority
object CommercialPriority extends FrontPriority
object TrainingPriority extends FrontPriority
object EmailPriority extends FrontPriority

case class FrontImage(imageUrl: String, imageHeight: Int, imageWidth: Int)

case class Front(
    id: String,
    collections: List[String],
    navSection: Option[String],
    webTitle: Option[String],
    title: Option[String],
    description: Option[String],
    onPageDescription: Option[String],
    frontImage: Option[FrontImage],
    isImageDisplayed: Boolean,
    priority: FrontPriority,
    isHidden: Boolean,
    canonicalCollection: String,
    group: Option[String]
)

object Front {
  private def getFrontPriority(frontJson: FrontJson): FrontPriority =
    frontJson.priority match {
      case Some("commercial") => CommercialPriority
      case Some("training")   => TrainingPriority
      case Some("email")      => EmailPriority
      case Some("edition")    => TrainingPriority
      case _                  => EditorialPriority
    }

  private def getImageUrl(frontJson: FrontJson): Option[FrontImage] =
    for {
      imageUrl <- frontJson.imageUrl
      imageHeight <- frontJson.imageHeight
      imageWidth <- frontJson.imageWidth
    } yield FrontImage(imageUrl, imageHeight, imageWidth)

  def fromFrontJson(id: String, frontJson: FrontJson): Front = {
    Front(
      id,
      frontJson.collections,
      frontJson.navSection,
      frontJson.webTitle,
      frontJson.title,
      frontJson.description,
      frontJson.onPageDescription,
      getImageUrl(frontJson),
      frontJson.isImageDisplayed.getOrElse(false),
      getFrontPriority(frontJson),
      frontJson.isHidden.getOrElse(false),
      canonicalCollection(id, frontJson),
      frontJson.group
    )
  }

  /** If we're on a network front, try hard-coded headlines ids, otherwise use editorially chosen canonical
    * container if present, falling back to the first available collection. We should never have a front with
    * no containers so final fallback is a placeholder.
    */
  private def canonicalCollection(id: String, frontJson: FrontJson): String = {
    frontJson.canonical
      .filter(frontJson.collections.contains)
      .orElse(frontJson.collections.headOption)
      .getOrElse("no collections")
  }

  def frontsFromConfig(configJson: ConfigJson): Set[Front] = {
    configJson.fronts.map { case (id, json) =>
      Front.fromFrontJson(id, json)
    }.toSet
  }
}
