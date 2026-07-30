package com.gu.contentapi.client

import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.thrift.ThriftDeserializer
import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}


class Decoder[Response <: ThriftStruct](codec: ThriftStructCodec[Response]) {
  def decode(data: Array[Byte]): Response = ThriftDeserializer.deserialize(data, codec)
}

trait PaginationDecoder[Response, Element] {
  val pageSize: Response => Int
  val pages: Response => Int
  val currentPage: Response => Int
  val elements: Response => collection.Seq[Element]
}

object Decoder {
  type PageableResponseDecoder[Response <: ThriftStruct, Element] = Decoder[Response] with PaginationDecoder[Response, Element]

  def pageableResponseDecoder[R <: ThriftStruct, E](c: ThriftStructCodec[R])(
    fPageSize: R => Int,
    fPages: R => Int,
    fCurrentPage: R => Int,
    fElements: R => collection.Seq[E]
  ): PageableResponseDecoder[R, E] =
    new Decoder[R](c) with PaginationDecoder[R, E] {
      val pageSize: R => Int = fPageSize
      val pages: R => Int = fPages
      val currentPage: R => Int = fCurrentPage
      val elements: R => collection.Seq[E] = fElements
    }

  implicit val itemDecoder: Decoder[ItemResponse] = new Decoder(ItemResponse)
  implicit val tagsDecoder: PageableResponseDecoder[TagsResponse, Tag] = pageableResponseDecoder(TagsResponse)(_.pageSize, _.pages, _.currentPage, _.results)
  implicit val sectionsQuery: Decoder[SectionsResponse] = new Decoder(SectionsResponse)
  implicit val editionsDecoder: Decoder[EditionsResponse] = new Decoder(EditionsResponse)
  implicit val videoStatsDecoder: Decoder[VideoStatsResponse] = new Decoder(VideoStatsResponse)
  implicit val atomsDecoder: Decoder[AtomsResponse] = new Decoder(AtomsResponse)
  implicit val searchDecoder: PageableResponseDecoder[SearchResponse, Content] = pageableResponseDecoder(SearchResponse)(_.pageSize, _.pages, _.currentPage, _.results)
  implicit val atomUsageDecoder: PageableResponseDecoder[AtomUsageResponse, String] = pageableResponseDecoder(AtomUsageResponse)(_.pageSize, _.pages, _.currentPage, _.results)
}
