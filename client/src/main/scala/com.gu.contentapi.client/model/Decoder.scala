package com.gu.contentapi.client

import com.gu.contentapi.client.model._
import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.thrift.ThriftDeserializer
import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}

/** Typeclass witnessing how to unmarshall a Thrift stream of bytes
  * into a concrete data type
  * @tparam Query the query type
  */
trait Decoder[Query] {
  /** the response type corresponding to `Query` */
  type Response <: ThriftStruct
  /** the type of codecs unmarshalling instances of `Response` */
  type Codec <: ThriftStructCodec[Response]
  /** the codec */
  def codec: Codec
  /** performs the unmarshalling
    * @return a function taking an array of bytes into a `Response`
    */
  def decode: Array[Byte] => Response = ThriftDeserializer.deserialize(_, codec)
}

private[client] object Decoder {

  type Aux[Q, R] = Decoder[Q] { type Response = R }

  def apply[Q](implicit d: Decoder[Q]): Decoder[Q] = d

  private def instance[Q, R <: ThriftStruct, C <: ThriftStructCodec[R]](c: C) = new Decoder[Q] {
    type Response = R
    type Codec = C
    val codec = c
  }

  private def atomsDecoder[Query] = instance[Query, AtomsResponse, AtomsResponse.type](AtomsResponse)

  implicit val itemQuery = instance[ItemQuery, ItemResponse, ItemResponse.type](ItemResponse)
  implicit val searchQuery = instance[SearchQuery, SearchResponse, SearchResponse.type](SearchResponse)
  implicit val tagsQuery = instance[TagsQuery, TagsResponse, TagsResponse.type](TagsResponse)
  implicit val sectionsQuery = instance[SectionsQuery, SectionsResponse, SectionsResponse.type](SectionsResponse)
  implicit val editionsQuery = instance[EditionsQuery, EditionsResponse, EditionsResponse.type](EditionsResponse)
  implicit val removedContentQuery = instance[RemovedContentQuery, RemovedContentResponse, RemovedContentResponse.type](RemovedContentResponse)
  implicit val videoStatsQuery = instance[VideoStatsQuery, VideoStatsResponse, VideoStatsResponse.type](VideoStatsResponse)
  implicit val atomsQuery = atomsDecoder[AtomsQuery]
  implicit val recipesQuery = atomsDecoder[RecipesQuery]
  implicit val reviewsQuery = atomsDecoder[ReviewsQuery]
  implicit val gameReviewsQuery = atomsDecoder[GameReviewsQuery]
  implicit val restaurantReviewsQuery = atomsDecoder[RestaurantReviewsQuery]
  implicit val filmReviewsQuery = atomsDecoder[FilmReviewsQuery]
  implicit val storiesQuery = instance[StoriesQuery, StoriesResponse, StoriesResponse.type](StoriesResponse)
  implicit def nextQuery[Q <: PaginatedApiQuery[Q]](implicit d: Decoder[Q]) = instance[NextQuery[Q], d.Response, d.Codec](d.codec)
  implicit def prevQuery[Q <: PaginatedApiQuery[Q]](implicit d: Decoder[Q]) = instance[PrevQuery[Q], d.Response, d.Codec](d.codec)

}