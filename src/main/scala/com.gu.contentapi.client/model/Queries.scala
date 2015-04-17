package com.gu.contentapi.client.model

import com.gu.contentapi.client.utils.QueryStringParams
import com.gu.contentapi.client.{Parameter, Parameters}

sealed trait ContentApiQuery {
  def parameters: Map[String, String]

  def pathSegment: String

  override def toString = {
    s"${getClass.getSimpleName}(/$pathSegment${QueryStringParams(parameters)})"
  }
}

case class ItemQuery(id: String, parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
  with EditionParameters[ItemQuery]
  with ContentParameters[ItemQuery]
  with ShowParameters[ItemQuery]
  with ShowReferencesParameters[ItemQuery]
  with ShowExtendedParameters[ItemQuery]
  with PaginationParameters[ItemQuery]
  with OrderingParameters[ItemQuery]
  with FilterParameters[ItemQuery]
  with FilterExtendedParameters[ItemQuery]
  with FilterSearchParameters[ItemQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(id, parameterMap)

  def itemId(contentId: String): ItemQuery =
    copy(id = contentId)

  override def pathSegment: String = id
}

case class SearchQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
  with ContentParameters[SearchQuery]
  with ShowParameters[SearchQuery]
  with ShowReferencesParameters[SearchQuery]
  with OrderingParameters[SearchQuery]
  with PaginationParameters[SearchQuery]
  with FilterParameters[SearchQuery]
  with FilterExtendedParameters[SearchQuery]
  with FilterSearchParameters[SearchQuery] {
  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "search"
}

case class TagsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
  with ShowReferencesParameters[TagsQuery]
  with PaginationParameters[TagsQuery]
  with FilterParameters[TagsQuery]
  with FilterTagParameters[TagsQuery]
  with FilterSearchParameters[TagsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "tags"
}

case class SectionsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
  with FilterSearchParameters[SectionsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "sections"
}

case class CollectionQuery(collectionId: String, parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
  with ShowParameters[CollectionQuery]
  with ShowReferencesParameters[CollectionQuery]
  with FilterParameters[CollectionQuery]
  with FilterExtendedParameters[CollectionQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(collectionId, parameterMap)

  def collectionId(collectionId: String): CollectionQuery =
    copy(collectionId = collectionId)

  override def pathSegment: String = s"collections/$collectionId"
}

trait ContentParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def contentSet = StringParameter("content-set")
}

trait EditionParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def edition = StringParameter("edition")
}

trait ShowParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def showFields = StringParameter("show-fields")
  def showTags = StringParameter("show-tags")
  def showElements = StringParameter("show-elements")
  def showRights = StringParameter("show-rights")
  def showBlocks = StringParameter("show-blocks")
}

trait ShowReferencesParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def showReferences = StringParameter("show-references")
}

trait ShowExtendedParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def showStoryPackage = BoolParameter("show-story-package")
  def showRelated = BoolParameter("show-related")
  def showMostViewed = BoolParameter("show-most-viewed")
  def showEditorsPicks = BoolParameter("show-editors-picks")
}

trait PaginationParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def page = IntParameter("page")
  def pageSize = IntParameter("page-size")
}

trait OrderingParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def orderBy = StringParameter("order-by")
  def useDate = StringParameter("use-date")
}

trait FilterParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def section = StringParameter("section")
  def reference = StringParameter("reference")
  def referenceType = StringParameter("reference-type")
  def productionOffice = StringParameter("production-office")
}

trait FilterExtendedParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def tag = StringParameter("tag")
  def ids = StringParameter("ids")
  def rights = StringParameter("rights")
  def leadContent = StringParameter("lead-content")
  def fromDate = DateParameter("from-date")
  def toDate = DateParameter("to-date")
}

trait FilterTagParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def tagType = StringParameter("type")
}

trait FilterSearchParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def q = StringParameter("q")
}
