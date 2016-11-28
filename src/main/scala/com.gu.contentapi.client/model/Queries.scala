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

case class RemovedContentQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
  with RemovedReasonParameters[RemovedContentQuery]
  with PaginationParameters[RemovedContentQuery]
  with OrderingParameters[RemovedContentQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  def pathSegment: String = "content/removed"
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

case class EditionsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
  with FilterSearchParameters[EditionsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "editions"
}

case class VideoStatsQuery(
  edition: Option[String] = None,
  section: Option[String] = None,
  parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
  with FilterSearchParameters[VideoStatsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(edition, section, parameterMap)

  override def pathSegment: String = Seq(Some("stats/videos"), edition, section).flatten.mkString("/")
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
  def showAtoms  = StringParameter("show-atoms")
  def showSection = BoolParameter("show-section")
}

trait ShowReferencesParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def showReferences = StringParameter("show-references")
}

trait ShowExtendedParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def showStoryPackage = BoolParameter("show-story-package")
  def showRelated = BoolParameter("show-related")
  def showMostViewed = BoolParameter("show-most-viewed")
  def showEditorsPicks = BoolParameter("show-editors-picks")
  def showPackages = BoolParameter("show-packages")
  def showStats = BoolParameter("show-stats")
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
  def contentType = StringParameter("type")
  def lang = StringParameter("lang")
  def starRating = IntParameter("star-rating")
  def membershipAccess = StringParameter("membership-access")
  def containsElement = StringParameter("contains-element")
  def commentable = BoolParameter("commentable")
  def filename = StringParameter("filename")
}

trait FilterTagParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def tagType = StringParameter("type")
}

trait FilterSearchParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def q = StringParameter("q")
}

// Supports values gone, expired and takendown.
trait RemovedReasonParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def reason = StringParameter("reason")
}
