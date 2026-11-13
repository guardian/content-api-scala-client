package com.gu.facia.api

import com.gu.contentapi.client.ContentApiClient
import com.gu.contentapi.client.model.v1.Content
import com.gu.facia.api.contentapi.ContentApi.{AdjustItemQuery, AdjustSearchQuery}
import com.gu.facia.api.contentapi.{ContentApi, LatestSnapsRequest, LinkSnapsRequest}
import com.gu.facia.api.models._
import com.gu.facia.api.utils.{BackfillResolver, BoostLevel, ContentProperties}
import com.gu.facia.client.ApiClient

import scala.PartialFunction.condOpt
import scala.collection.compat._
import scala.concurrent.{ExecutionContext, Future}

object FAPI {
  def getFronts()(implicit
      capiClient: ContentApiClient,
      faciaClient: ApiClient,
      ec: ExecutionContext
  ): Response[Set[Front]] = {
    for {
      config <- Response.Async.Right(faciaClient.config)
    } yield Front.frontsFromConfig(config)
  }

  def frontForPath(path: String)(implicit
      capiClient: ContentApiClient,
      faciaClient: ApiClient,
      ec: ExecutionContext
  ): Response[Front] = {
    for {
      fronts <- getFronts()
      front <- Response.fromOption(
        fronts.find(_.id == path),
        NotFound(s"Not front found for $path")
      )
    } yield front
  }

  /** Fetches the collection information for the given id by resolving info out of the fronts config and the
    * collection's own config JSON.
    */
  def getCollection(collectionId: String)(implicit
      capiClient: ContentApiClient,
      faciaClient: ApiClient,
      ec: ExecutionContext
  ): Response[Collection] = {
    val fCollectionJson = faciaClient.collection(collectionId)
    val fConfigJson = faciaClient.config
    for {
      collectionJson <- Response.Async.Right(fCollectionJson)
      configJson <- Response.Async.Right(fConfigJson)
      collectionConfigJson <- Response.fromOption(
        configJson.collections.get(collectionId),
        NotFound(s"Collection config not found for $collectionId")
      )
      collectionConfig = CollectionConfig.fromCollectionJson(
        collectionConfigJson
      )
    } yield {
      Collection.fromCollectionJsonConfigAndContent(
        collectionId,
        collectionJson,
        collectionConfig
      )
    }
  }

  /** Fetch all the collections for a front in one go
    */
  def frontCollections(frontId: String)(implicit
      capiClient: ContentApiClient,
      faciaClient: ApiClient,
      ec: ExecutionContext
  ): Response[List[Collection]] = {
    for {
      configJson <- Response.Async.Right(faciaClient.config)
      frontJson <- Response.fromOption(
        configJson.fronts.get(frontId),
        NotFound(s"No front found for $frontId")
      )
      collectionIds = frontJson.collections
      collectionsJsons <- Response.Async.Right(
        Future.traverse(collectionIds)(faciaClient.collection)
      )
      collectionConfigJsons <- Response.traverse(
        collectionIds.map(id =>
          Response.fromOption(
            configJson.collections.get(id),
            NotFound(s"Collection config not found for $id")
          )
        )
      )
      collectionConfigs = collectionConfigJsons.map(
        CollectionConfig.fromCollectionJson
      )
    } yield {
      collectionIds
        .lazyZip(collectionsJsons)
        .lazyZip(collectionConfigs)
        .toList
        .map { case (collectionId, collectionJson, collectionConfig) =>
          Collection.fromCollectionJsonConfigAndContent(
            collectionId,
            collectionJson,
            collectionConfig
          )
        }
    }
  }

  private def getLiveContentForCollection(
      collection: Collection,
      adjustSearchQuery: AdjustSearchQuery = identity
  )(implicit
      capiClient: ContentApiClient,
      ec: ExecutionContext
  ): Response[Set[Content]] = {
    val itemIdsForRequest = Collection.liveContentIdsWithoutSnaps(collection)
    val supportingIdsForRequest =
      Collection.liveSupportingIdsWithoutSnaps(collection)
    val allItemIdsForRequest = itemIdsForRequest ::: supportingIdsForRequest
    for {
      hydrateQueries <- ContentApi.buildHydrateQueries(
        capiClient,
        allItemIdsForRequest,
        adjustSearchQuery
      )
      hydrateResponses <- ContentApi.getHydrateResponse(
        capiClient,
        hydrateQueries
      )
      content = ContentApi.itemsFromSearchResponses(hydrateResponses)
    } yield content
  }

  private def getDraftContentForCollection(
      collection: Collection,
      adjustSearchQuery: AdjustSearchQuery = identity
  )(implicit
      capiClient: ContentApiClient,
      ec: ExecutionContext
  ): Response[Set[Content]] = {
    val itemIdsForRequest =
      Collection
        .draftContentIdsWithoutSnaps(collection)
        .getOrElse(Collection.liveContentIdsWithoutSnaps(collection))
    val supportingIdsForRequest =
      Collection
        .draftSupportingIdsWithoutSnaps(collection)
        .getOrElse(Collection.liveSupportingIdsWithoutSnaps(collection))
    val allItemIdsForRequest = itemIdsForRequest ::: supportingIdsForRequest
    for {
      hydrateQueries <- ContentApi.buildHydrateQueries(
        capiClient,
        allItemIdsForRequest,
        adjustSearchQuery
      )
      hydrateResponses <- ContentApi.getHydrateResponse(
        capiClient,
        hydrateQueries
      )
      content = ContentApi.itemsFromSearchResponses(hydrateResponses)
    } yield content
  }

  private def getLiveLatestSnapContentForCollection(
      collection: Collection,
      adjustItemQuery: AdjustItemQuery
  )(implicit capiClient: ContentApiClient, ec: ExecutionContext) = {
    val latestSnapsRequest: LatestSnapsRequest =
      Collection.liveLatestSnapsRequestFor(collection)
    val latestSupportingSnaps: LatestSnapsRequest =
      Collection.liveSupportingSnaps(collection)
    val allSnaps = latestSnapsRequest.join(latestSupportingSnaps)
    for (
      snapContent <- ContentApi.latestContentFromLatestSnaps(
        capiClient,
        allSnaps,
        adjustItemQuery
      )
    )
      yield snapContent
  }

  private def getDraftLatestSnapContentForCollection(
      collection: Collection,
      adjustItemQuery: AdjustItemQuery
  )(implicit
      capiClient: ContentApiClient,
      ec: ExecutionContext
  ): Response[Map[String, Option[Content]]] = {
    val latestSnapsRequest: LatestSnapsRequest =
      Collection
        .draftLatestSnapsRequestFor(collection)
        .getOrElse(Collection.liveLatestSnapsRequestFor(collection))
    val latestSupportingSnaps: LatestSnapsRequest =
      Collection
        .draftSupportingSnaps(collection)
        .getOrElse(Collection.liveSupportingSnaps(collection))
    val allSnaps = latestSnapsRequest.join(latestSupportingSnaps)
    for (
      snapContent <- ContentApi.latestContentFromLatestSnaps(
        capiClient,
        allSnaps,
        adjustItemQuery
      )
    )
      yield snapContent
  }

  private def getLiveLinkSnapBrandingsForCollection(collection: Collection)(implicit
      capiClient: ContentApiClient,
      ec: ExecutionContext
  ): Response[Map[String, BrandingByEdition]] =
    getLinkSnapBrandings(Collection.liveLinkSnapsRequestFor(collection))

  private def getDraftLinkSnapBrandingsForCollection(collection: Collection)(implicit
      capiClient: ContentApiClient,
      ec: ExecutionContext
  ): Response[Map[String, BrandingByEdition]] =
    getLinkSnapBrandings(
      Collection
        .draftLinkSnapsRequestFor(collection)
        .getOrElse(Collection.liveLinkSnapsRequestFor(collection))
    )

  private def getLinkSnapBrandings(request: LinkSnapsRequest)(implicit
      capiClient: ContentApiClient,
      ec: ExecutionContext
  ): Response[Map[String, BrandingByEdition]] =
    for (snapContent <- ContentApi.linkSnapBrandingsByEdition(capiClient, request)) yield snapContent

  def getTreatsForCollection(
      collection: Collection,
      adjustSearchQuery: AdjustSearchQuery = identity,
      adjustItemQuery: AdjustItemQuery = identity
  )(implicit capiClient: ContentApiClient, ec: ExecutionContext) = {
    val (treatIds, treatsSnapsRequest) = Collection.treatsRequestFor(collection)
    for {
      hydrateQueries <- ContentApi.buildHydrateQueries(
        capiClient,
        treatIds,
        adjustSearchQuery
      )
      hydrateResponses <- ContentApi.getHydrateResponse(
        capiClient,
        hydrateQueries
      )
      snapContent <- ContentApi.latestContentFromLatestSnaps(
        capiClient,
        treatsSnapsRequest,
        adjustItemQuery
      )
      setOfContent = ContentApi.itemsFromSearchResponses(hydrateResponses)
      content <- Collection.treatContent(collection, setOfContent, snapContent)
    } yield content
  }

  def liveCollectionContentWithoutSnaps(
      collection: Collection,
      adjustSearchQuery: AdjustSearchQuery = identity
  )(implicit
      capiClient: ContentApiClient,
      ec: ExecutionContext
  ): Response[List[FaciaContent]] = {
    val collectionWithoutSnaps = Collection.withoutSnaps(collection)
    for {
      setOfContent <- getLiveContentForCollection(collection, adjustSearchQuery)
      liveContent <- Collection.liveContent(
        collectionWithoutSnaps,
        setOfContent
      )
    } yield liveContent
  }

  def liveCollectionContentWithSnaps(
      collection: Collection,
      adjustSearchQuery: AdjustSearchQuery = identity,
      adjustSnapItemQuery: AdjustItemQuery = identity
  )(implicit
      capiClient: ContentApiClient,
      ec: ExecutionContext
  ): Response[List[FaciaContent]] = {
    for {
      setOfContent <- getLiveContentForCollection(collection, adjustSearchQuery)
      snapContent <- getLiveLatestSnapContentForCollection(
        collection,
        adjustSnapItemQuery
      )
      linkSnapBrandingsByEdition <- getLiveLinkSnapBrandingsForCollection(
        collection
      )
      liveContent <- Collection.liveContent(
        collection,
        setOfContent,
        snapContent,
        linkSnapBrandingsByEdition
      )
    } yield liveContent
  }

  def draftCollectionContentWithoutSnaps(
      collection: Collection,
      adjustSearchQuery: AdjustSearchQuery = identity
  )(implicit
      capiClient: ContentApiClient,
      ec: ExecutionContext
  ): Response[List[FaciaContent]] = {
    val collectionWithoutSnaps = Collection.withoutSnaps(collection)
    for {
      setOfContent <- getDraftContentForCollection(
        collection,
        adjustSearchQuery
      )
      draftContent <- Collection.draftContent(
        collectionWithoutSnaps,
        setOfContent
      )
    } yield draftContent
  }

  def draftCollectionContentWithSnaps(
      collection: Collection,
      adjustSearchQuery: AdjustSearchQuery = identity,
      adjustSnapItemQuery: AdjustItemQuery = identity
  )(implicit
      capiClient: ContentApiClient,
      ec: ExecutionContext
  ): Response[List[FaciaContent]] = {
    for {
      setOfContent <- getDraftContentForCollection(
        collection,
        adjustSearchQuery
      )
      snapContent <- getDraftLatestSnapContentForCollection(
        collection,
        adjustSnapItemQuery
      )
      linkSnapBrandingsByEdition <- getDraftLinkSnapBrandingsForCollection(
        collection
      )
      draftContent <- Collection.draftContent(
        collection,
        setOfContent,
        snapContent,
        linkSnapBrandingsByEdition
      )
    } yield draftContent
  }

  /** Fetches content for the configured backfill query. The query can be manipulated for different
    * requirements by providing adjustment functions. The results then have their facia metadata resolved
    * using the collection information.
    */
  def backfillFromConfig(
      collectionConfig: CollectionConfig,
      adjustSearchQuery: AdjustSearchQuery = identity,
      adjustItemQuery: AdjustItemQuery = identity
  )(implicit
      capiClient: ContentApiClient,
      faciaClient: ApiClient,
      ec: ExecutionContext
  ): Response[List[FaciaContent]] = {

    val backfillRequest = BackfillResolver.resolveFromConfig(collectionConfig)
    BackfillResolver.backfill(
      backfillRequest,
      adjustSearchQuery,
      adjustItemQuery
    )
  }

  private def updateContentProperties(
      content: FaciaContent,
      properties: ContentProperties
  ): FaciaContent = content match {
    case curatedContent: CuratedContent =>
      curatedContent.copy(properties = properties)
    case supportingCuratedContent: SupportingCuratedContent =>
      supportingCuratedContent.copy(properties = properties)
    case snap: LatestSnap           => snap.copy(properties = properties)
    case snap: LinkSnap             => snap.copy(properties = properties)
    case eventGraphic: EventGraphic =>
      eventGraphic.copy(properties = properties)
  }

  private def updateGroup(content: FaciaContent, group: String): FaciaContent =
    content match {
      case curatedContent: CuratedContent                     => curatedContent.copy(group = group)
      case supportingCuratedContent: SupportingCuratedContent =>
        supportingCuratedContent.copy(group = group)
      case snap: LatestSnap           => snap.copy(group = group)
      case snap: LinkSnap             => snap.copy(group = group)
      case eventGraphic: EventGraphic =>
        eventGraphic.copy(group = group)
    }

  case class GroupBoostConfig(
      default: BoostLevel,
      allowedBoosts: List[BoostLevel],
      maxItems: Int,
      group: String
  )

  private val flexibleGeneralBoosts = List(
    // Splash
    GroupBoostConfig(
      BoostLevel.Default,
      List(
        BoostLevel.Default,
        BoostLevel.Boost,
        BoostLevel.MegaBoost,
        BoostLevel.GigaBoost
      ),
      maxItems = 1,
      group = "3"
    ),
    // Very big
    GroupBoostConfig(
      BoostLevel.MegaBoost,
      List(BoostLevel.MegaBoost),
      maxItems = 0,
      group = "2"
    ),
    // Big
    GroupBoostConfig(
      BoostLevel.Boost,
      List(BoostLevel.Boost, BoostLevel.MegaBoost),
      maxItems = 0,
      group = "1"
    ),
    // Standard
    GroupBoostConfig(
      BoostLevel.Default,
      List(BoostLevel.Default, BoostLevel.Boost, BoostLevel.MegaBoost),
      maxItems = 8,
      group = "0"
    )
  )

  private def boostsConfigFor(
      collectionType: String,
      groupsConfig: List[GroupConfig]
  ): Option[List[GroupBoostConfig]] = {
    val groupsConfigTopGroupFirst = groupsConfig.reverse
    condOpt(collectionType) { case "flexible/general" =>
      flexibleGeneralBoosts.zip(groupsConfigTopGroupFirst).map { case (boosts, groupConfig) =>
        boosts
          .copy(maxItems = groupConfig.maxItems.getOrElse(boosts.maxItems))
      }
    }
  }

  def applyDefaultBoostLevelsAndGroups(
      collectionConfig: CollectionConfig,
      contents: List[FaciaContent]
  ): List[FaciaContent] = {
    applyDefaultBoostLevelsAndGroups[FaciaContent](
      groupsConfig = collectionConfig.groupsConfig,
      collectionType = collectionConfig.collectionType,
      contents = contents,
      getBoostLevel = _.properties.boostLevel,
      setBoostLevel = (content, default) =>
        updateContentProperties(
          content,
          content.properties.copy(boostLevel = default)
        ),
      setGroup = updateGroup
    )
  }

  // NB - this will not behave as expected if there are "gaps" in the curated content (ie there are no curated cards in the
  // second group but there are curated cards in the third group). But the fronts tool is supposed to prevent gaps.
  def applyDefaultBoostLevelsAndGroups[T](
      groupsConfig: Option[GroupsConfig],
      collectionType: String,
      contents: List[T],
      getBoostLevel: T => BoostLevel,
      setBoostLevel: (T, BoostLevel) => T,
      setGroup: (T, String) => T
  ): List[T] = {
    val contentsWithDefaultBoosts = for {
      gc <- groupsConfig
      boostsConfig <- boostsConfigFor(collectionType, gc.config)
    } yield {
      val (result, _) = boostsConfig.foldLeft((List.empty[T], contents)) {
        case ((processed, unprocessed), groupConfig) =>
          val (currentGroupContents, remaining) =
            unprocessed.splitAt(groupConfig.maxItems)
          val currentGroupContentsWithBoostsAndGroupNumber =
            currentGroupContents.map(content => {
              val contentWithGroupNumber = setGroup(content, groupConfig.group)
              if (
                groupConfig.allowedBoosts
                  .contains(getBoostLevel(contentWithGroupNumber))
              )
                contentWithGroupNumber
              else
                setBoostLevel(contentWithGroupNumber, groupConfig.default)
            })
          (processed ++ currentGroupContentsWithBoostsAndGroupNumber, remaining)
      }
      result
    }

    contentsWithDefaultBoosts.getOrElse(contents)
  }
}
