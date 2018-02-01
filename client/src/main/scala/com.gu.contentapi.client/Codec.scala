package com.gu.contentapi.client

import com.gu.contentapi.client.model._
import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.thrift.ThriftDeserializer

private[client] trait Codec[Q] {
  type R
  def decode: Array[Byte] => R
}

private[client] object Codec {
  implicit val item = new Codec[ItemQuery] {
    type R = ItemResponse
    def decode = ThriftDeserializer.deserialize(_, ItemResponse)
  }

  implicit val searchQuery = new Codec[SearchQuery] {
    type R = SearchResponse
    def decode = ThriftDeserializer.deserialize(_, SearchResponse)
  }

  implicit val tagsQuery = new Codec[TagsQuery] {
    type R = TagsResponse
    def decode = ThriftDeserializer.deserialize(_, TagsResponse)
  }

  implicit val sectionsQuery = new Codec[SectionsQuery] {
    type R = SectionsResponse
    def decode = ThriftDeserializer.deserialize(_, SectionsResponse)
  }

  implicit val editionsQuery = new Codec[EditionsQuery] {
    type R = EditionsResponse
    def decode = ThriftDeserializer.deserialize(_, EditionsResponse)
  }

  implicit val removedContentQuery = new Codec[RemovedContentQuery] {
    type R = RemovedContentResponse
    def decode = ThriftDeserializer.deserialize(_, RemovedContentResponse)
  }

  implicit val videoStatsQuery = new Codec[VideoStatsQuery] {
    type R = VideoStatsResponse
    def decode = ThriftDeserializer.deserialize(_, VideoStatsResponse)
  }

  implicit val atomsQuery = new Codec[AtomsQuery] {
    type R = AtomsResponse
    def decode = ThriftDeserializer.deserialize(_, AtomsResponse)
  }

  implicit val recipesQuery = new Codec[RecipesQuery] {
    type R = AtomsResponse
    def decode = ThriftDeserializer.deserialize(_, AtomsResponse)
  }

  implicit val reviewsQuery = new Codec[ReviewsQuery] {
    type R = AtomsResponse
    def decode = ThriftDeserializer.deserialize(_, AtomsResponse)
  }

  implicit val gameReviewsQuery = new Codec[GameReviewsQuery] {
    type R = AtomsResponse
    def decode = ThriftDeserializer.deserialize(_, AtomsResponse)
  }

  implicit val restaurantReviewsQuery = new Codec[RestaurantReviewsQuery] {
    type R = AtomsResponse
    def decode = ThriftDeserializer.deserialize(_, AtomsResponse)
  }

  implicit val filmReviewsQuery = new Codec[FilmReviewsQuery] {
    type R = AtomsResponse
    def decode = ThriftDeserializer.deserialize(_, AtomsResponse)
  }

  implicit val storiesQuery = new Codec[StoriesQuery] {
    type R = StoriesResponse
    def decode = ThriftDeserializer.deserialize(_, StoriesResponse)
  }

  implicit def nextQuery[Q <: ContentApiQuery] = new Codec[NextQuery[Q]] {
    type R = SearchResponse
    def decode = ThriftDeserializer.deserialize(_, SearchResponse)
  }

}