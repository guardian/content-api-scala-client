package com.gu.facia.api.utils

import com.gu.contentapi.client.ContentApiClient
import com.gu.facia.api.contentapi.ContentApi
import com.gu.facia.api.contentapi.ContentApi._
import com.gu.facia.api.models.{CollectionConfig, CuratedContent, FaciaContent}
import com.gu.facia.api.{FAPI, Response}
import com.gu.facia.client.ApiClient
import com.gu.facia.client.models.{Backfill, TrailMetaData}

import scala.concurrent.ExecutionContext

case class InvalidBackfillConfiguration(msg: String) extends Exception(msg)

sealed trait BackfillResolver
case class CapiBackfill(query: String, collectionConfig: CollectionConfig) extends BackfillResolver
case class CollectionBackfill(parentCollectionId: String) extends BackfillResolver
case object EmptyBackfill extends BackfillResolver

object BackfillResolver {
  def resolveFromConfig(
      collectionConfig: CollectionConfig
  ): BackfillResolver = {
    collectionConfig.backfill match {
      case Some(Backfill("capi", query: String)) =>
        CapiBackfill(query, collectionConfig)
      case Some(Backfill("collection", query: String)) =>
        CollectionBackfill(query)
      case Some(Backfill(backFillType, _)) => EmptyBackfill
      case None                            => EmptyBackfill
    }
  }

  def backfill(
      resolver: BackfillResolver,
      adjustSearchQuery: AdjustSearchQuery = identity,
      adjustItemQuery: AdjustItemQuery = identity
  )(implicit
      capiClient: ContentApiClient,
      faciaClient: ApiClient,
      ec: ExecutionContext
  ): Response[List[FaciaContent]] = {
    resolver match {
      case CapiBackfill(query, collectionConfig) =>
        val capiQuery = ContentApi
          .buildBackfillQuery(query)
          .map(adjustSearchQuery)
          .left
          .map(adjustItemQuery)
        val backfillResponse =
          ContentApi.getBackfillResponse(capiClient, capiQuery)
        for {
          backfillContent <- ContentApi.backfillContentFromResponse(
            backfillResponse
          )
        } yield {
          backfillContent.map(
            CuratedContent.fromTrailAndContent(
              _,
              TrailMetaData.empty,
              None,
              collectionConfig,
              None
            )
          )
        }
      case CollectionBackfill(parentCollectionId) =>
        val collectionBackfillResult =
          for {
            parentCollection <- FAPI.getCollection(parentCollectionId)
            curatedCollection <- FAPI.liveCollectionContentWithSnaps(
              parentCollection,
              adjustSearchQuery,
              adjustItemQuery
            )
            nestedBackfill <- parentCollection.collectionConfig.backfill match {
              case Some(Backfill("capi", query)) =>
                backfill(
                  CapiBackfill(query, parentCollection.collectionConfig),
                  adjustSearchQuery,
                  adjustItemQuery
                )
              case _ => backfill(EmptyBackfill)
            }
          } yield {
            (curatedCollection ++ nestedBackfill).distinct
          }

        collectionBackfillResult recover { err =>
          List()
        }

      case EmptyBackfill => Response.Right(Nil)
    }
  }
}
