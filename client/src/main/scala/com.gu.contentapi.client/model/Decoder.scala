package com.gu.contentapi.client

import com.gu.contentapi.client.model._
import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.thrift.ThriftDeserializer
import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}

private[client] trait Decoder[Query] {
  type Response <: ThriftStruct
  type Codec <: ThriftStructCodec[Response]
  
  def codec: Codec
  
  def decode: Array[Byte] => Response = ThriftDeserializer.deserialize(_, codec)
}

private[client] object Decoder {

  type Aux[Q, R] = Decoder[Q] { type Response = R }

  def apply[Q](implicit d: Decoder[Q]): Decoder[Q] = d

  implicit val itemQuery = new Decoder[ItemQuery] {
    type Response = ItemResponse
    type Codec = ItemResponse.type
    val codec = ItemResponse
  }

  implicit val searchQuery = new Decoder[SearchQuery] {
    type Response = SearchResponse
    type Codec = SearchResponse.type
    val codec = SearchResponse
  }

  implicit val tagsQuery = new Decoder[TagsQuery] {
    type Response = TagsResponse
    type Codec = TagsResponse.type
    val codec = TagsResponse
  }

  implicit val sectionsQuery = new Decoder[SectionsQuery] {
    type Response = SectionsResponse
    type Codec = SectionsResponse.type
    val codec = SectionsResponse
  }

  implicit val editionsQuery = new Decoder[EditionsQuery] {
    type Response = EditionsResponse
    type Codec = EditionsResponse.type
    val codec = EditionsResponse
  }

  implicit val removedContentQuery = new Decoder[RemovedContentQuery] {
    type Response = RemovedContentResponse
    type Codec = RemovedContentResponse.type
    val codec = RemovedContentResponse
  }

  implicit val videoStatsQuery = new Decoder[VideoStatsQuery] {
    type Response = VideoStatsResponse
    type Codec = VideoStatsResponse.type
    val codec = VideoStatsResponse
  }

  implicit val atomsQuery = new Decoder[AtomsQuery] {
    type Response = AtomsResponse
    type Codec = AtomsResponse.type
    val codec = AtomsResponse
  }

  implicit val recipesQuery = new Decoder[RecipesQuery] {
    type Response = AtomsResponse
    type Codec = AtomsResponse.type
    val codec = AtomsResponse
  }

  implicit val reviewsQuery = new Decoder[ReviewsQuery] {
    type Response = AtomsResponse
    type Codec = AtomsResponse.type
    val codec = AtomsResponse
  }

  implicit val gameReviewsQuery = new Decoder[GameReviewsQuery] {
    type Response = AtomsResponse
    type Codec = AtomsResponse.type
    val codec = AtomsResponse
  }

  implicit val restaurantReviewsQuery = new Decoder[RestaurantReviewsQuery] {
    type Response = AtomsResponse
    type Codec = AtomsResponse.type
    val codec = AtomsResponse
  }

  implicit val filmReviewsQuery = new Decoder[FilmReviewsQuery] {
    type Response = AtomsResponse
    type Codec = AtomsResponse.type
    val codec = AtomsResponse
  }

  implicit val storiesQuery = new Decoder[StoriesQuery] {
    type Response = StoriesResponse
    type Codec = StoriesResponse.type
    val codec = StoriesResponse
  }

  implicit def nextQuery[Q <: PaginatedApiQuery[Q]](implicit d: Decoder[Q]) = new Decoder[NextQuery[Q]] {  
    type Response = d.Response
    type Codec = d.Codec
    val codec = d.codec
  }

  implicit def prevQuery[Q <: PaginatedApiQuery[Q]](implicit d: Decoder[Q]) = new Decoder[PrevQuery[Q]] {
    type Response = d.Response
    type Codec = d.Codec
    val codec = d.codec
  }

}