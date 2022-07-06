package com.gu.contentapi.client

import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.thrift.ThriftDeserializer
import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}


class Decoder[Response <: ThriftStruct](codec: ThriftStructCodec[Response]) {
  def decode(data: Array[Byte]): Response = ThriftDeserializer.deserialize(data, codec)
}

trait PaginationDecoder[Response, Element] {
  val pageSize: Response => Int
  val currentPage: Response => Int
  val pages: Response => Int
  val elements: Response => collection.Seq[Element]
}

object Decoder {
  type PageableResponseDecoder[Response <: ThriftStruct, Element] = Decoder[Response] with PaginationDecoder[Response, Element]

  def pageableResponseDecoder[R <: ThriftStruct, E](c: ThriftStructCodec[R])(
    ps: R => Int,
    cp: R => Int,
    p: R => Int,
    el: R => collection.Seq[E]
  ): PageableResponseDecoder[R, E] =
    new Decoder[R](c) with PaginationDecoder[R, E] {
      val pageSize: R => Int = ps
      val currentPage: R => Int = cp
      val pages: R => Int = p
      val elements: R => collection.Seq[E] = el
    }

  implicit val itemDecoder: Decoder[ItemResponse] = new Decoder(ItemResponse)
  implicit val tagsDecoder: PageableResponseDecoder[TagsResponse, Tag] = pageableResponseDecoder(TagsResponse)(_.pageSize, _.currentPage, _.pages, _.results)
  implicit val sectionsQuery: Decoder[SectionsResponse] = new Decoder(SectionsResponse)
  implicit val editionsDecoder: Decoder[EditionsResponse] = new Decoder(EditionsResponse)
  implicit val videoStatsDecoder: Decoder[VideoStatsResponse] = new Decoder(VideoStatsResponse)
  implicit val atomsDecoder: Decoder[AtomsResponse] = pageableResponseDecoder(AtomsResponse)(_.pageSize, _.currentPage, _.pages, _.results)
  implicit val searchDecoder: PageableResponseDecoder[SearchResponse, Content] = pageableResponseDecoder(SearchResponse)(_.pageSize, _.currentPage, _.pages, _.results)
  implicit val atomUsageDecoder: Decoder[AtomUsageResponse] = new Decoder(AtomUsageResponse)
}
