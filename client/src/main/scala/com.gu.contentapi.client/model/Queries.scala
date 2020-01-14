package com.gu.contentapi.client.model

import com.gu.contentapi.client.utils.QueryStringParams
import com.gu.contentapi.client.{Parameter, Parameters}
import com.gu.contentatom.thrift.AtomType

sealed trait ContentApiQuery {
  def parameters: Map[String, String]

  def pathSegment: String

  override def toString = s"""${getClass.getSimpleName}(${getUrl("")})"""

  private def url(location: String, parameters: Map[String, String]): String = {
    require(!location.contains('?'), "must not specify parameters in URL")

    location + QueryStringParams(parameters)
  }

  def getUrl(targetUrl: String, customParameters: Map[String, String] = Map.empty): String =
    url(s"$targetUrl/${pathSegment}", parameters ++ customParameters)
}

trait SearchQueryBase[Self <: SearchQueryBase[Self]]
  extends ContentApiQuery
     with ShowParameters[Self]
     with ShowReferencesParameters[Self]
     with OrderByParameter[Self]
     with UseDateParameter[Self]
     with PaginationParameters[Self]
     with FilterParameters[Self]
     with FilterExtendedParameters[Self]
     with FilterSearchParameters[Self] {
  this: Self =>
}

case class ItemQuery(id: String, parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
  with EditionParameters[ItemQuery]
  with ShowParameters[ItemQuery]
  with ShowReferencesParameters[ItemQuery]
  with ShowExtendedParameters[ItemQuery]
  with PaginationParameters[ItemQuery]
  with OrderByParameter[ItemQuery]
  with UseDateParameter[ItemQuery]
  with FilterParameters[ItemQuery]
  with FilterExtendedParameters[ItemQuery]
  with FilterSearchParameters[ItemQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(id, parameterMap)

  def itemId(contentId: String): ItemQuery =
    copy(id = contentId)

  override def pathSegment: String = id
}

case class SearchQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends SearchQueryBase[SearchQuery] {

  def withParameters(parameterMap: Map[String, Parameter]): SearchQuery = copy(parameterMap)

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
  with FilterSearchParameters[SectionsQuery]
  with FilterSectionParameters[SectionsQuery]{

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

case class AtomsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
  with AtomsParameters[AtomsQuery]
  with PaginationParameters[AtomsQuery]
  with UseDateParameter[AtomsQuery]
  with OrderByParameter[AtomsQuery]
  with FilterSearchParameters[AtomsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "atoms"
}

case class AtomUsageQuery(atomType: AtomType, atomId: String, parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
  with PaginationParameters[AtomUsageQuery] {
  
  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterHolder = parameterMap)

  override def pathSegment: String = s"atom/${atomType.toString.toLowerCase}/$atomId/usage"
}

case class RecipesQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
  with PaginationParameters[RecipesQuery]
  with RecipeParameters[RecipesQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "atoms/recipes"
}

case class ReviewsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
  with PaginationParameters[ReviewsQuery]
  with ReviewSpecificParameters[ReviewsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "atoms/reviews"
}

case class GameReviewsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
    with ReviewSpecificParameters[GameReviewsQuery]
    with PaginationParameters[GameReviewsQuery]
    with GameParameters[GameReviewsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "atoms/reviews/game"
}

case class RestaurantReviewsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
    with ReviewSpecificParameters[RestaurantReviewsQuery]
    with PaginationParameters[RestaurantReviewsQuery]
    with RestaurantParameters[RestaurantReviewsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "atoms/reviews/restaurant"
}

case class FilmReviewsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery
    with ReviewSpecificParameters[FilmReviewsQuery]
    with PaginationParameters[FilmReviewsQuery]
    with FilmParameters[FilmReviewsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "atoms/reviews/film"
}

case class NextQuery[Q <: PaginatedApiQuery[Q]](originalQuery: Q, contentId: String)
  extends ContentApiQuery {
  
  def parameters: Map[String, String] = originalQuery.parameters.filterKeys(not(isPaginationParameter)).toMap

  override def pathSegment: String = s"""content/${contentId}/next"""
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
  def showStats = BoolParameter("show-stats")
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
}

trait PaginationParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def page = IntParameter("page")
  def pageSize = IntParameter("page-size")
}

trait OrderByParameter[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def orderBy = StringParameter("order-by")
}

trait UseDateParameter[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
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
  def sponsorshipType = StringParameter("sponsorship-type")
}

trait FilterSectionParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def sponsorshipType = StringParameter("sponsorship-type")
}

trait FilterSearchParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def q = StringParameter("q")
}

trait AtomsParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def types = StringParameter("types")
  def searchFields = StringParameter("searchFields")
  def fromDate = DateParameter("from-date")
  def toDate = DateParameter("to-date")
}

trait RecipeParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def title = StringParameter("title")
  def credits = StringParameter("credits")
  def categories = StringParameter("category")
  def cuisines = StringParameter("cuisine")
  def dietary = StringParameter("dietary")
  def celebration = StringParameter("celebration")
  def ingredients = StringParameter("ingredients")
  def maxTime = IntParameter("max-time")
}

trait ReviewSpecificParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def reviewer = StringParameter("reviewer")
  def maxRating = IntParameter("max-rating")
  def minRating = IntParameter("min-rating")
}

trait FilmParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def name = StringParameter("name")
  def genres = StringParameter("genres")
  def actors = StringParameter("actors")
  def directors = StringParameter("directors")
}

trait GameParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def gameName = StringParameter("name")
}

trait RestaurantParameters[Owner <: Parameters[Owner]] extends Parameters[Owner] { this: Owner =>
  def restaurantName = StringParameter("name")
}
