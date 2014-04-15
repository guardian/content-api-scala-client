package com.gu.openplatform.contentapi.parameters

object ShowReferencesParameters {
  def empty = ShowReferencesParameters(None)
}

case class ShowReferencesParameters(showReferences: Option[String]) extends QueryParameters