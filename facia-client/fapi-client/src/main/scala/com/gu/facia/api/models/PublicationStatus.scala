package com.gu.facia.api.models

sealed trait PublicationStatus

object PublicationStatus {
  case object Live extends PublicationStatus
  case object Draft extends PublicationStatus
}
