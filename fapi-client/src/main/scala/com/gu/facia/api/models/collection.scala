package com.gu.facia.api.models

import com.gu.contentapi.client.ContentApiClient
import com.gu.contentapi.client.model.v1.{
  Block,
  BlockElement,
  Blocks,
  Content,
  ContentAtomElementFields
}
import com.gu.contentatom.thrift.{Atom, AtomData}
import com.gu.contentatom.thrift.atom.media.MediaAtom
import com.gu.contentatom.thrift.atom.multimediaslideshow.{
  MultimediaSlideshowAtom,
  SlideContent
}
import com.gu.facia.api.contentapi.{LatestSnapsRequest, LinkSnapsRequest}
import com.gu.facia.client.models.{
  CollectionJson,
  SupportingItem,
  TargetedTerritory,
  Trail
}
import org.joda.time.{DateTime, DateTimeZone}
import com.gu.facia.api.utils.BoostLevel
import com.gu.facia.api.Response
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}

case class Collection(
    id: String,
    displayName: String,
    href: Option[String],
    live: List[Trail],
    draft: Option[List[Trail]],
    treats: List[Trail],
    lastUpdated: Option[DateTime],
    updatedBy: Option[String],
    updatedEmail: Option[String],
    collectionConfig: CollectionConfig,
    targetedTerritory: Option[TargetedTerritory]
)

object Collection extends StrictLogging {
  def fromCollectionJsonConfigAndContent(
      collectionId: String,
      collectionJson: Option[CollectionJson],
      collectionConfig: CollectionConfig
  ): Collection = {
    Collection(
      collectionId,
      collectionConfig.displayName
        .orElse(collectionJson.flatMap(_.displayName))
        .getOrElse("untitled"),
      collectionConfig.href.orElse(collectionJson.flatMap(_.href)),
      collectionJson.map(_.live).getOrElse(Nil),
      collectionJson.flatMap(_.draft),
      collectionJson.flatMap(_.treats).getOrElse(Nil),
      collectionJson.map(_.lastUpdated),
      collectionJson.map(_.updatedBy),
      collectionJson.map(_.updatedEmail),
      collectionConfig,
      collectionConfig.targetedTerritory
    )
  }

  private[models] def isSplashCard(
      trail: Trail,
      index: Int,
      collectionType: String,
      collectionHasSnap: Boolean
  ): Boolean = {
    (collectionType, trail.safeMeta.group, index, collectionHasSnap) match {
      case ("flexible/general", Some("3"), _, _) => true
      case ("flexible/special", None, 0, false)  => true
      case ("flexible/special", None, 1, true)   => true
      case _                                     => false
    }
  }

  private[models] def isSnapCard(
      group: Option[String],
      collectionType: String
  ): Boolean = {
    (group, collectionType) match {
      case (Some("1"), "flexible/special") => true
      case _                               => false
    }
  }

  private[models] def maxSupportingItems(
      isSplashCard: Boolean,
      collectionType: String,
      boostLevel: String
  ): Int = {
    if (
      (collectionType == "flexible/general" || collectionType == "flexible/special") &&
      !isSplashCard && boostLevel == BoostLevel.Default.label
    ) {
      2
    } else {
      4
    }
  }

  def contentFrom(
      collection: Collection,
      content: Set[Content],
      snapContent: Map[String, Option[Content]] = Map.empty,
      linkSnapBrandingsByEdition: Map[String, BrandingByEdition],
      from: Collection => List[Trail]
  ): List[FaciaContent] = {
    // if content is not in the set it was most likely filtered out by the CAPI query, so exclude it
    // note that this does not currently deal with e.g. snaps
    def resolveTrail(
        trail: Trail,
        index: Int,
        collectionHasSnap: Boolean
    ): Option[FaciaContent] = {
      val boostLevel = trail.safeMeta.boostLevel
      val isSplash = isSplashCard(
        trail,
        index,
        collection.collectionConfig.collectionType,
        collectionHasSnap
      )
      val maxItems = maxSupportingItems(
        isSplash,
        collection.collectionConfig.collectionType,
        boostLevel.getOrElse("default")
      )

      content
        .find { c =>
          trail.id.endsWith(
            "/" + c.fields
              .flatMap(_.internalPageCode)
              .getOrElse(throw new RuntimeException("No internal page code"))
          )
        }
        .map { content =>
          trail.safeMeta.supporting
            .map(_.flatMap(resolveSupportingContent))
            .map(supportingItems =>
              CuratedContent.fromTrailAndContentWithSupporting(
                content,
                trail.safeMeta,
                Option(trail.frontPublicationDate),
                supportingItems.take(maxItems),
                collection.collectionConfig,
                trail.tests
              )
            )
            .getOrElse(
              CuratedContent.fromTrailAndContent(
                content,
                trail.safeMeta,
                Option(trail.frontPublicationDate),
                collection.collectionConfig,
                trail.tests
              )
            )
        }
        .orElse {
          snapContent
            .find { case (id, _) => trail.id == id }
            .map(c => LatestSnap.fromTrailAndContent(trail, c._2))
        }
        .orElse {
          linkSnapBrandingsByEdition
            .find { case (id, _) =>
              trail.id == id
            }
            .flatMap { case (_, brandingByEdition) =>
              Snap.maybeFromTrailAndBrandings(trail, brandingByEdition)
            }
            .orElse {
              Snap.maybeFromTrail(trail)
            }
        }
        .orElse {
          Option.when(trail.isEventGraphic) {
            EventGraphic.fromTrail(trail)
          }
        }
    }

    def resolveSupportingContent(
        supportingItem: SupportingItem
    ): Option[FaciaContent] = {
      content
        .find { c =>
          supportingItem.id.endsWith(
            "/" + c.fields
              .flatMap(_.internalPageCode)
              .getOrElse(throw new RuntimeException("No internal page code"))
          )
        }
        .map { content =>
          SupportingCuratedContent.fromTrailAndContent(
            content,
            supportingItem.safeMeta,
            supportingItem.frontPublicationDate,
            collection.collectionConfig,
            supportingItem.tests
          )
        }
        .orElse {
          snapContent
            .find { case (id, _) => supportingItem.id == id }
            .map(c =>
              LatestSnap.fromSupportingItemAndContent(supportingItem, c._2)
            )
        }
        .orElse {
          Snap.maybeFromSupportingItem(supportingItem)
        }
    }

    val collectionHasSnap = from(collection).headOption match {
      case Some(trail) =>
        isSnapCard(
          trail.safeMeta.group,
          collection.collectionConfig.collectionType
        )
      case None => false
    }

    for {
      (trail, index) <- from(collection).zipWithIndex
      content <- resolveTrail(trail, index, collectionHasSnap)
    } yield content

  }

  private def enrichContentWithMediaOrMultimediaAtom(
      faciaContent: List[FaciaContent]
  )(implicit
      capiClient: ContentApiClient,
      ec: ExecutionContext
  ): Response[List[FaciaContent]] = {

    def getMediaAtom(fcContent: FaciaContent)(implicit
        ec: ExecutionContext,
        capiClient: ContentApiClient
    ): Response[Option[Atom]] = {
      val futureMaybeAtomData = fcContent match {
        case faciaContent @ AtomId(atomId)
            if faciaContent.properties.videoReplace =>
          capiClient
            .getResponse(ContentApiClient.item(atomId))
            .map { response =>
              response.media.flatMap(atom =>
                Option.when(isValidMediaAtom(atom))(atom)
              )
            }
            .recover { case e =>
              logger.warn(
                s"Exception while fetching media atom for ID $atomId: ${e.getMessage}",
                e
              )
              None
            }

        case faciaContent: CuratedContent
            if faciaContent.properties.showMainVideo =>
          val mainAtom = for {
            atomId <- getMainMediaAtomId(faciaContent)
            atoms <- faciaContent.content.atoms
            mediaAtoms <- atoms.media
            validMediaAtom <- mediaAtoms.find(atom =>
              atom.id == atomId && isValidMediaAtom(atom)
            )
          } yield validMediaAtom
          Future.successful(mainAtom)
        case _ => Future.successful(None)
      }

      Response.Async.Right(futureMaybeAtomData)
    }

    def getMultimediaSlideshow(fcContent: FaciaContent)(implicit
        ec: ExecutionContext,
        capiClient: ContentApiClient
    ): Response[Option[FaciaMultimediaSlideshow]] = {
      val futureMaybeSlideshow = fcContent match {
        case curatedContent: CuratedContent
            if curatedContent.properties.multimediaSlideshowReplace =>
          curatedContent.multimediaSlideshowAtomId match {
            case Some(atomId) =>
              capiClient
                .getResponse(ContentApiClient.item(atomId))
                .flatMap { response =>
                  response.multimediaslideshow match {
                    case Some(atom) => resolveMultimediaSlideshow(atom)
                    case None       => Future.successful(None)
                  }
                }
                .recover { case e =>
                  logger.warn(
                    s"Exception while fetching multimedia slideshow atom for ID $atomId: ${e.getMessage}",
                    e
                  )
                  None
                }
            case None => Future.successful(None)
          }
        case _ => Future.successful(None)
      }

      Response.Async.Right(futureMaybeSlideshow)
    }

    def resolveMultimediaSlideshow(atom: Atom)(implicit
        ec: ExecutionContext,
        capiClient: ContentApiClient
    ): Future[Option[FaciaMultimediaSlideshow]] = atom.data match {
      case AtomData.MultimediaSlideshow(slideshow) =>
        getMultimediaSlideshowMediaAtoms(atom.id, slideshow).map { mediaAtoms =>
          val slides = slideshow.slides.toList.flatMap { slide =>
            val content: Option[FaciaSlideContent] = slide.content match {
              case SlideContent.Image(image) => Some(FaciaSlideImage(image))
              case SlideContent.MediaAtom(reference) =>
                mediaAtoms.get(reference.mediaAtomId).map(FaciaSlideVideo(_))
              case SlideContent.UnknownUnionField(_) =>
                logger.warn(
                  s"Dropping slide with unknown content from multimedia slideshow ${atom.id}"
                )
                None
            }
            content.map(FaciaSlide(_, slide.caption, slide.label, slide.credit))
          }
          Option.when(slides.nonEmpty)(
            FaciaMultimediaSlideshow(
              atom.id,
              slideshow.title,
              slideshow.aspectRatio,
              slides
            )
          )
        }
      case _ =>
        logger.warn(s"Multimedia slideshow atom ${atom.id} is not valid")
        Future.successful(None)
    }

    def getMultimediaSlideshowMediaAtoms(
        slideshowId: String,
        slideshow: MultimediaSlideshowAtom
    )(implicit
        ec: ExecutionContext,
        capiClient: ContentApiClient
    ): Future[Map[String, Atom]] = {
      val mediaAtomIds = slideshow.slides
        .map(_.content)
        .collect { case SlideContent.MediaAtom(reference) =>
          reference.mediaAtomId
        }
        .distinct
        .toList

      Future
        .traverse(mediaAtomIds) { mediaAtomId =>
          capiClient
            .getResponse(ContentApiClient.item(s"atom/media/$mediaAtomId"))
            .map(_.media.filter(isValidMediaAtom).map(mediaAtomId -> _))
            .recover { case e =>
              logger.warn(
                s"Exception while fetching media atom $mediaAtomId for multimedia slideshow $slideshowId: ${e.getMessage}",
                e
              )
              None
            }
        }
        .map(_.flatten.toMap)
    }

    def getMainMediaAtomId(faciaContent: CuratedContent): Option[String] = {
      for {
        block <- faciaContent.content.blocks
        main <- block.main
        element <- main.elements.find(_.contentAtomTypeData.isDefined)
        atomData <- element.contentAtomTypeData
      } yield atomData.atomId
    }

    def isValidMediaAtom(atom: Atom): Boolean = {
      atom.data match {
        case mediaData: AtomData.Media =>
          if (!isExpired(mediaData.media)) {
            true
          } else {
            logger.warn(s"Media atom ${atom.id} is expired")
            false
          }
        case _ =>
          logger.warn(s"Media atom ${atom.id} is not valid")
          false
      }
    }

    def isExpired(mediaAtom: MediaAtom): Boolean = {
      val maybeExpired = for {
        metadata <- mediaAtom.metadata
        expiryDate <- metadata.expiryDate
      } yield new DateTime(expiryDate).withZone(DateTimeZone.UTC).isBeforeNow
      maybeExpired.getOrElse(false)
    }

    val responses: Seq[Response[FaciaContent]] = faciaContent.map {
      case curatedContent: CuratedContent =>
        for {
          maybeMediaAtom <- getMediaAtom(curatedContent)
          maybeMultimediaSlideshow <- getMultimediaSlideshow(curatedContent)
        } yield {
          val withMediaAtom = maybeMediaAtom match {
            case mediaAtom @ Some(_) =>
              curatedContent.copy(mediaAtom = mediaAtom)
            case None => curatedContent
          }
          maybeMultimediaSlideshow match {
            case multimediaSlideshow @ Some(_) =>
              withMediaAtom.copy(multimediaSlideshow = multimediaSlideshow)
            case None => withMediaAtom
          }
        }
      case content => Response.Right(content)
    }

    Response.traverse(responses.toList)

  }

  /* Live Methods */
  def liveContent(
      collection: Collection,
      content: Set[Content],
      snapContent: Map[String, Option[Content]] = Map.empty,
      linkSnapBrandingsByEdition: Map[String, BrandingByEdition] = Map.empty
  )(implicit
      capiClient: ContentApiClient,
      ec: ExecutionContext
  ): Response[List[FaciaContent]] = {
    val liveContent = contentFrom(
      collection,
      content,
      snapContent,
      linkSnapBrandingsByEdition,
      collection => collection.live
    )
    enrichContentWithMediaOrMultimediaAtom(liveContent)
  }

  def liveContentIdsWithoutSnaps(collection: Collection): List[String] =
    collection.live
      .filterNot(_.isSnap)
      .filterNot(_.isEventGraphic)
      .map(_.id)

  private def allLiveSupportingItems(
      collection: Collection
  ): List[SupportingItem] =
    collection.live
      .filterNot(_.isEventGraphic)
      .flatMap(_.meta)
      .flatMap(_.supporting)
      .flatten

  def liveSupportingIdsWithoutSnaps(collection: Collection): List[String] =
    allLiveSupportingItems(collection).filterNot(_.isSnap).map(_.id)

  def liveSupportingSnaps(collection: Collection): LatestSnapsRequest =
    LatestSnapsRequest(
      allLiveSupportingItems(collection)
        .filter(_.isSnap)
        .filter(_.safeMeta.snapType.contains("latest"))
        .flatMap(snap =>
          snap.meta.flatMap(_.snapUri).map(uri => snap.id -> uri)
        )
        .toMap
    )

  def liveLatestSnapsRequestFor(collection: Collection): LatestSnapsRequest =
    LatestSnapsRequest(
      collection.live
        .filter(_.isSnap)
        .filter(_.safeMeta.snapType.contains("latest"))
        .flatMap(snap => snap.safeMeta.snapUri.map(uri => snap.id -> uri))
        .toMap
    )

  private def linkSnapsRequestFor(trails: List[Trail]): LinkSnapsRequest =
    LinkSnapsRequest(
      trails
        .filter(_.isSnap)
        .filter(_.safeMeta.snapType.contains("link"))
        .flatMap(snap => snap.safeMeta.href.map(uri => snap.id -> uri))
        .toMap
    )

  def liveLinkSnapsRequestFor(collection: Collection): LinkSnapsRequest =
    linkSnapsRequestFor(collection.live)

  /* Draft Methods */
  def draftContent(
      collection: Collection,
      content: Set[Content],
      snapContent: Map[String, Option[Content]] = Map.empty,
      linkSnapBrandingsByEdition: Map[String, BrandingByEdition] = Map.empty
  )(implicit
      capiClient: ContentApiClient,
      ec: ExecutionContext
  ): Response[List[FaciaContent]] = {
    val draftContent = contentFrom(
      collection,
      content,
      snapContent,
      linkSnapBrandingsByEdition,
      collection => collection.draft.getOrElse(collection.live)
    )
    enrichContentWithMediaOrMultimediaAtom(draftContent)
  }

  def draftContentIdsWithoutSnaps(
      collection: Collection
  ): Option[List[String]] =
    collection.draft.map(
      _.filterNot(_.isSnap).filterNot(_.isEventGraphic).map(_.id)
    )

  private def allDraftSupportingItems(
      collection: Collection
  ): Option[List[SupportingItem]] =
    collection.draft.map(
      _.filterNot(_.isEventGraphic)
        .flatMap(_.meta)
        .flatMap(_.supporting)
        .flatten
    )

  def draftSupportingIdsWithoutSnaps(
      collection: Collection
  ): Option[List[String]] =
    allDraftSupportingItems(collection).map(_.filterNot(_.isSnap).map(_.id))

  def draftSupportingSnaps(collection: Collection): Option[LatestSnapsRequest] =
    allDraftSupportingItems(collection)
      .map(listOfSupportingItems =>
        LatestSnapsRequest(
          listOfSupportingItems
            .filter(_.isSnap)
            .filter(_.safeMeta.snapType.contains("latest"))
            .flatMap(snap =>
              snap.meta.flatMap(_.snapUri).map(uri => snap.id -> uri)
            )
            .toMap
        )
      )

  def draftLatestSnapsRequestFor(
      collection: Collection
  ): Option[LatestSnapsRequest] =
    collection.draft.map(listOfTrails =>
      LatestSnapsRequest(
        listOfTrails
          .filter(_.isSnap)
          .filter(_.safeMeta.snapType.contains("latest"))
          .flatMap(snap => snap.safeMeta.snapUri.map(uri => snap.id -> uri))
          .toMap
      )
    )

  def draftLinkSnapsRequestFor(
      collection: Collection
  ): Option[LinkSnapsRequest] =
    collection.draft map linkSnapsRequestFor

  /* Treats */
  def treatContent(
      collection: Collection,
      content: Set[Content],
      snapContent: Map[String, Option[Content]] = Map.empty
  )(implicit
      capiClient: ContentApiClient,
      ec: ExecutionContext
  ): Response[List[FaciaContent]] = {
    val treatContent = contentFrom(
      collection,
      content,
      snapContent,
      linkSnapBrandingsByEdition = Map.empty,
      collection => collection.treats
    )
    enrichContentWithMediaOrMultimediaAtom(treatContent)
  }

  def treatsRequestFor(
      collection: Collection
  ): (List[String], LatestSnapsRequest) = {
    val latestSnapsRequest =
      LatestSnapsRequest(
        collection.treats
          .filter(_.isSnap)
          .filter(_.safeMeta.snapType.contains("latest"))
          .flatMap(snap => snap.safeMeta.snapUri.map(uri => snap.id -> uri))
          .toMap
      )

    val treatIds = collection.treats.filterNot(_.isSnap).map(_.id)

    (treatIds, latestSnapsRequest)
  }

  def withoutSnaps(collection: Collection): Collection = {
    collection.copy(
      live = collection.live.filterNot(_.isSnap),
      draft = collection.draft.map(_.filterNot(_.isSnap))
    )
  }
}
