package com.gu.contentapi.client

import com.gu.contentapi.client.Decoder.PageableResponseDecoder
import com.twitter.scrooge.ThriftStruct

package object model {

  implicit class RichPageableResponse[R <: ThriftStruct, E](response: R)(implicit prd: PageableResponseDecoder[R, E]) {

    val impliesNoFurtherResults: Boolean = prd.elements(response).size < prd.pageSize(response)
  }

  private[model] def not[A](f: A => Boolean): A => Boolean = !f(_)

  private[model] val isPaginationParameter = Set("page")
}