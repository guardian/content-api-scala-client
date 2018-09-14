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
  /** the codec unmarshalling instances of `Response` */
  def codec: ThriftStructCodec[Response]
  /** performs the unmarshalling
    * @return a function taking an array of bytes into a `Response`
    */
  def decode: Array[Byte] => Response = ThriftDeserializer.deserialize(_, codec)
}

private[client] object Decoder {

  type Aux[Q, R] = Decoder[Q] { type Response = R }

  private def apply[Q, R <: ThriftStruct](c: ThriftStructCodec[R]) = new Decoder[Q] {
    type Response = R
    def codec = c
  }

  private def atomsDecoder[Query] = apply[Query, AtomsResponse](AtomsResponse)

  implicit val itemQuery = apply[ItemQuery, ItemResponse](ItemResponse)
  implicit val tagsQuery = apply[TagsQuery, TagsResponse](TagsResponse)
  implicit val sectionsQuery = apply[SectionsQuery, SectionsResponse](SectionsResponse)
  implicit val editionsQuery = apply[EditionsQuery, EditionsResponse](EditionsResponse)
  implicit val removedContentQuery = apply[RemovedContentQuery, RemovedContentResponse](RemovedContentResponse)
  implicit val videoStatsQuery = apply[VideoStatsQuery, VideoStatsResponse](VideoStatsResponse)
  implicit val atomsQuery = atomsDecoder[AtomsQuery]
  implicit val recipesQuery = atomsDecoder[RecipesQuery]
  implicit val reviewsQuery = atomsDecoder[ReviewsQuery]
  implicit val gameReviewsQuery = atomsDecoder[GameReviewsQuery]
  implicit val restaurantReviewsQuery = atomsDecoder[RestaurantReviewsQuery]
  implicit val filmReviewsQuery = atomsDecoder[FilmReviewsQuery]
  implicit val storiesQuery = apply[StoriesQuery, StoriesResponse](StoriesResponse)
  implicit def searchQueryBase[T <: SearchQueryBase[T]] = apply[T, SearchResponse](SearchResponse)
  implicit def nextQuery[Q <: PaginatedApiQuery[Q]](implicit d: Decoder[Q]) = apply[NextQuery[Q], d.Response](d.codec)
  implicit def atomsUsageQuery = apply[AtomUsageQuery, AtomUsageResponse](AtomUsageResponse)
}
