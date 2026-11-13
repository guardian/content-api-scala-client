package com.gu.facia.api.utils

import com.gu.facia.api.models.FaciaContent
import FaciaContentUtils._

object MediaType {
  def fromFaciaContent(faciaContent: FaciaContent): Option[MediaType] =
    mediaType(faciaContent)
}

sealed trait MediaType

case object Gallery extends MediaType
case object Video extends MediaType
case object Audio extends MediaType
