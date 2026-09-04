package com.gu.contentapi.client.model

import com.gu.contentapi.client.Decoder.PageableResponseDecoder
import com.gu.contentapi.client.model.Direction.Next
import com.gu.contentapi.client.model.v1.{AtomUsageResponse, AtomsResponse, Content, EditionsResponse, ItemResponse, SearchResponse, SectionsResponse, Tag, TagsResponse, VideoStatsResponse}
import com.gu.contentapi.client.utils.QueryStringParams
import com.gu.contentapi.client.{Parameter, Parameters}
import com.gu.contentatom.thrift.AtomType
import com.twitter.scrooge.ThriftStruct

sealed trait ContentApiQuery[+Response <: ThriftStruct] {
  def parameters: Map[String, String]

  def pathSegment: String

  override def toString = s"""${getClass.getSimpleName}(${getUrl("")})"""

  private def url(location: String, parameters: Map[String, String]): String = {
    require(!location.contains('?'), "must not specify parameters in URL")

    location + QueryStringParams(parameters)
  }

  def getUrl(targetUrl: String, customParameters: Map[String, String] = Map.empty): String =
    url(s"$targetUrl/$pathSegment", parameters ++ customParameters)

}

abstract class PaginatedApiQuery[Response <: ThriftStruct, Element](
  implicit prd: PageableResponseDecoder[Response, Element]
) extends ContentApiQuery[Response] {

  /**
    * Produce a version of this query that explicitly sets previously ''unset'' pagination/ordering parameters,
    * matching how the Content API server decided to process the previous request.
    *
    * For instance, if the Content API decided to process https://content.guardianapis.com/search?q=brexit
    * with pageSize:10 & orderBy:relevance, that will have been detailed in the CAPI response - and therefore we
    * can copy those parameters into our following query so we don't change how we're ordering the results
    * as we paginate through them!
   */
  def setPaginationConsistentWith(response: Response): PaginatedApiQuery[Response, Element]

  def followingQueryGiven(response: Response, direction: Direction): Option[PaginatedApiQuery[Response, Element]] =
    if (response.impliesNoFurtherResults) None else setPaginationConsistentWith(response).followingQueryGivenFull(response, direction)

  /** Construct a query for the subsequent results after this response. This method will only be called if the
    * response was supplied a full page of results, meaning that there's the possibility of more results to fetch.
    */
  protected def followingQueryGivenFull(response: Response, direction: Direction): Option[PaginatedApiQuery[Response, Element]]
}

trait SearchQueryBase[Self <: SearchQueryBase[Self]]
  extends ContentApiQuery[SearchResponse]
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

case class ItemQuery(id: String, parameterHolder: Map[String, Parameter] = Map.empty, channelId: Option[String]=None)
  extends ContentApiQuery[ItemResponse]
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

  def withParameters(parameterMap: Map[String, Parameter]) = copy(id, parameterMap, channelId)

  def withChannelId(newChannel:String) = copy(id, parameterHolder, Some(newChannel))

  def withoutChannelId() = copy(id, parameterHolder, None)

  def itemId(contentId: String): ItemQuery =
    copy(id = contentId)

  override def pathSegment: String = channelId match {
    case None => id
    case Some(chl) => s"channel/$chl/item/$id"
  }
}

case class SearchQuery(parameterHolder: Map[String, Parameter] = Map.empty, channelId: Option[String] = None)
  extends PaginatedApiQuery[SearchResponse, Content] with SearchQueryBase[SearchQuery] {

  def setPaginationConsistentWith(response: SearchResponse): PaginatedApiQuery[SearchResponse, Content] =
    pageSize.setIfUndefined(response.pageSize).orderBy.setIfUndefined(response.orderBy)

  def withParameters(parameterMap: Map[String, Parameter]): SearchQuery = copy(parameterMap, channelId)

  /**
    * Make this search on a CAPI channel rather than against web-only content
    * For more information about channels, and the reason why your app should only be in one channel,
    * contact the Content API team
    * @param channelId the channel to search against, or "all" to search across all channels.
    */
  def withChannel(channelId:String):SearchQuery = copy(parameterHolder, Some(channelId))

  def withoutChannel(): SearchQuery = copy(parameterHolder, None)

  override def pathSegment: String = channelId match {
    case None=>"search"
    case Some(chnl)=>s"channel/$chnl/search"
  }

  protected override def followingQueryGivenFull(response: SearchResponse, direction: Direction) = for {
    lastResultInResponse <- response.results.lastOption
  } yield FollowingSearchQuery(this, lastResultInResponse.id, direction)

}

case class TagsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends PaginatedApiQuery[TagsResponse, Tag]
  with ShowReferencesParameters[TagsQuery]
  with PaginationParameters[TagsQuery]
  with FilterParameters[TagsQuery]
  with FilterTagParameters[TagsQuery]
  with FilterSearchParameters[TagsQuery] {

  def setPaginationConsistentWith(response: TagsResponse): PaginatedApiQuery[TagsResponse, Tag] =
    pageSize.setIfUndefined(response.pageSize)

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "tags"

  protected override def followingQueryGivenFull(response: TagsResponse, direction: Direction): Option[TagsQuery] = {
    val followingPage = response.currentPage + direction.delta
    if (followingPage >= 1 && followingPage <= response.pages) Some(page(followingPage)) else None
  }
}

case class SectionsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery[SectionsResponse]
  with FilterSearchParameters[SectionsQuery]
  with FilterSectionParameters[SectionsQuery]{

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "sections"
}

case class EditionsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery[EditionsResponse]
  with FilterSearchParameters[EditionsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "editions"
}

case class VideoStatsQuery(
  edition: Option[String] = None,
  section: Option[String] = None,
  parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery[VideoStatsResponse]
  with FilterSearchParameters[VideoStatsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(edition, section, parameterMap)

  override def pathSegment: String = Seq(Some("stats/videos"), edition, section).flatten.mkString("/")
}

case class AtomsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery[AtomsResponse]
  with AtomsParameters[AtomsQuery]
  with PaginationParameters[AtomsQuery]
  with UseDateParameter[AtomsQuery]
  with OrderByParameter[AtomsQuery]
  with FilterSearchParameters[AtomsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "atoms"
}

case class AtomUsageQuery(atomType: AtomType, atomId: String, parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery[AtomUsageResponse]
  with PaginationParameters[AtomUsageQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterHolder = parameterMap)

  override def pathSegment: String = s"atom/${atomType.toString.toLowerCase}/$atomId/usage"
}

@deprecated("Recipe atoms no longer exist and should not be relied upon. No data will be returned and this class will be removed in a future iteration of the library")
case class RecipesQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery[AtomsResponse]
    with PaginationParameters[RecipesQuery]
    with RecipeParameters[RecipesQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "atoms/recipes"
}

case class ReviewsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery[AtomsResponse]
  with PaginationParameters[ReviewsQuery]
  with ReviewSpecificParameters[ReviewsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "atoms/reviews"
}

case class GameReviewsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery[AtomsResponse]
    with ReviewSpecificParameters[GameReviewsQuery]
    with PaginationParameters[GameReviewsQuery]
    with GameParameters[GameReviewsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "atoms/reviews/game"
}

case class RestaurantReviewsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery[AtomsResponse]
    with ReviewSpecificParameters[RestaurantReviewsQuery]
    with PaginationParameters[RestaurantReviewsQuery]
    with RestaurantParameters[RestaurantReviewsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "atoms/reviews/restaurant"
}

case class FilmReviewsQuery(parameterHolder: Map[String, Parameter] = Map.empty)
  extends ContentApiQuery[AtomsResponse]
    with ReviewSpecificParameters[FilmReviewsQuery]
    with PaginationParameters[FilmReviewsQuery]
    with FilmParameters[FilmReviewsQuery] {

  def withParameters(parameterMap: Map[String, Parameter]) = copy(parameterMap)

  override def pathSegment: String = "atoms/reviews/film"
}

sealed trait Direction {
  val pathSegment: String
  val delta: Int
  def guidingElementIn[T](elements: Iterable[T]): Option[T]
}

object Direction {
  object Next extends Direction {
    override val pathSegment: String = "next"
    override val delta: Int = 1
    override def guidingElementIn[T](elements: Iterable[T]): Option[T] = elements.lastOption

  }
  object Previous extends Direction {
    override val pathSegment: String = "prev"
    override val delta: Int = -1
    override def guidingElementIn[T](elements: Iterable[T]): Option[T] = elements.headOption
  }

  def forPathSegment(pathSegment: String): Direction = pathSegment match {
    case Next.pathSegment => Next
    case Previous.pathSegment => Previous
  }
}

case class FollowingSearchQuery(
  originalQuery: PaginatedApiQuery[SearchResponse, Content], contentId: String, direction: Direction = Next
) extends PaginatedApiQuery[SearchResponse, Content] {

  def parameters: Map[String, String] = originalQuery.parameters.filterKeys(not(isPaginationParameter)).toMap

  override def pathSegment: String = s"content/$contentId/${direction.pathSegment}"

  override def setPaginationConsistentWith(response: SearchResponse): PaginatedApiQuery[SearchResponse, Content] =
    originalQuery.setPaginationConsistentWith(response)

  protected override def followingQueryGivenFull(response: SearchResponse, updatedDirection: Direction): Option[PaginatedApiQuery[SearchResponse, Content]] = for {
    content <- updatedDirection.guidingElementIn(response.results)
  } yield copy(contentId = content.id, direction = updatedDirection)

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
  def showAliasPaths = BoolParameter("show-alias-paths")
  def showSchemaOrg = BoolParameter("show-schemaorg")
  def showChannels = StringParameter("show-channels")
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
  def paths = StringParameter("paths")
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
  def queryFields = StringParameter("query-fields")
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
