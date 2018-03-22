package com.gu.contentapi.client

import com.gu.contentapi.client.model._
import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.thrift.ThriftDeserializer
import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}

private[client] trait Decoder[Q] {
  type R <: ThriftStruct
  type O <: ThriftStructCodec[R]
  
  def companion: O
  
  def decode: Array[Byte] => R = ThriftDeserializer.deserialize(_, companion)
}

private[client] object Decoder {

  def apply[Q](implicit d: Decoder[Q]): Decoder[Q] = d

  implicit val item = new Decoder[ItemQuery] {
    type R = ItemResponse
    type O = ItemResponse.type
    val companion = ItemResponse
  }

  implicit val searchQuery = new Decoder[SearchQuery] {
    type R = SearchResponse
    type O = SearchResponse.type
    val companion = SearchResponse
  }

  implicit val tagsQuery = new Decoder[TagsQuery] {
    type R = TagsResponse
    type O = TagsResponse.type
    val companion = TagsResponse
  }

  implicit val sectionsQuery = new Decoder[SectionsQuery] {
    type R = SectionsResponse
    type O = SectionsResponse.type
    val companion = SectionsResponse
  }

  implicit val editionsQuery = new Decoder[EditionsQuery] {
    type R = EditionsResponse
    type O = EditionsResponse.type
    val companion = EditionsResponse
  }

  implicit val removedContentQuery = new Decoder[RemovedContentQuery] {
    type R = RemovedContentResponse
    type O = RemovedContentResponse.type
    val companion = RemovedContentResponse
  }

  implicit val videoStatsQuery = new Decoder[VideoStatsQuery] {
    type R = VideoStatsResponse
    type O = VideoStatsResponse.type
    val companion = VideoStatsResponse
  }

  implicit val atomsQuery = new Decoder[AtomsQuery] {
    type R = AtomsResponse
    type O = AtomsResponse.type
    val companion = AtomsResponse
  }

  implicit val recipesQuery = new Decoder[RecipesQuery] {
    type R = AtomsResponse
    type O = AtomsResponse.type
    val companion = AtomsResponse
  }

  implicit val reviewsQuery = new Decoder[ReviewsQuery] {
    type R = AtomsResponse
    type O = AtomsResponse.type
    val companion = AtomsResponse
  }

  implicit val gameReviewsQuery = new Decoder[GameReviewsQuery] {
    type R = AtomsResponse
    type O = AtomsResponse.type
    val companion = AtomsResponse
  }

  implicit val restaurantReviewsQuery = new Decoder[RestaurantReviewsQuery] {
    type R = AtomsResponse
    type O = AtomsResponse.type
    val companion = AtomsResponse
  }

  implicit val filmReviewsQuery = new Decoder[FilmReviewsQuery] {
    type R = AtomsResponse
    type O = AtomsResponse.type
    val companion = AtomsResponse
  }

  implicit val storiesQuery = new Decoder[StoriesQuery] {
    type R = StoriesResponse
    type O = StoriesResponse.type
    val companion = StoriesResponse
  }

}