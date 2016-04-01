package com.gu.contentapi.client.parser


import com.gu.contentapi.client.model.v1.{SearchResponse => SearchResponseThrift}
import com.gu.contentapi.client.model.v1.{ErrorResponse => ErrorResponseThrift}
import com.gu.contentapi.client.model.v1.{ItemResponse => ItemResponseThrift}
import com.gu.contentapi.client.model.v1.{TagsResponse => TagsResponseThrift}
import com.gu.contentapi.client.model.v1.{EditionsResponse => EditionsResponseThrift}
import com.gu.contentapi.client.model.v1.{SectionsResponse => SectionsResponseThrift}
import com.gu.contentapi.client.model.v1.{RemovedContentResponse => RemovedContentResponseThrift}
import com.gu.contentapi.client.model.v1._

import com.gu.contentapi.client.model.ItemResponse
import com.gu.contentapi.client.model.SearchResponse
import com.gu.contentapi.client.model.RemovedContentResponse
import com.gu.contentapi.client.model.TagsResponse
import com.gu.contentapi.client.model.SectionsResponse
import com.gu.contentapi.client.model.EditionsResponse
import com.gu.contentapi.client.model.ErrorResponse

import com.twitter.scrooge.ThriftEnum
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import org.json4s.{CustomSerializer, DefaultFormats}
import org.json4s.JsonAST._
import org.json4s.native.JsonMethods
import com.gu.storypackage.model.v1.{ArticleType, Group}
import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.quiz._
import com.gu.contentatom.thrift.atom.viewpoints._

import scala.util.{Try, Success, Failure}

object JsonParser {

  implicit val formats = DefaultFormats + AtomSerializer + ContentTypeSerializer + DateTimeSerializer +
    MembershipTierSerializer + OfficeSerializer + AssetTypeSerializer + ElementTypeSerializer +
    TagTypeSerializer + CrosswordTypeSerializer + StoryPackageArticleTypeSerializer + StoryPackageGroupSerializer

  /*
    We need to keep the all the old json parsers to keep the tests healthy;
    however we're not using them anymore in the actual client. In the future, if we decide
    to keep thrift only we can fix those test and delete the duplicate parsers below.
   */

  def parseItem(json: String): ItemResponse = {
    (JsonMethods.parse(json) \ "response").transformField(fixFields).extract[ItemResponse]
  }

  def parseItemThrift(json: String): ItemResponseThrift = {
    (JsonMethods.parse(json) \ "response").transformField(fixFields).extract[ItemResponseThrift]
  }

  def parseSearch(json: String): SearchResponse = {
    (JsonMethods.parse(json) \ "response").transformField(fixFields).extract[SearchResponse]
  }

  def parseSearchThrift(json: String): SearchResponseThrift = {
    (JsonMethods.parse(json) \ "response").transformField(fixFields).extract[SearchResponseThrift]
  }

  def parseRemovedContent(json: String): RemovedContentResponse = {
    (JsonMethods.parse(json) \ "response").extract[RemovedContentResponse]
  }

  def parseRemovedContentThrift(json: String): RemovedContentResponseThrift = {
    (JsonMethods.parse(json) \ "response").extract[RemovedContentResponseThrift]
  }

  def parseTags(json: String): TagsResponse = {
    (JsonMethods.parse(json) \ "response").extract[TagsResponse]
  }

  def parseTagsThrift(json: String): TagsResponseThrift = {
    (JsonMethods.parse(json) \ "response").extract[TagsResponseThrift]
  }

  def parseSections(json: String): SectionsResponse = {
    (JsonMethods.parse(json) \ "response").extract[SectionsResponse]
  }

  def parseSectionsThrift(json: String): SectionsResponseThrift = {
    (JsonMethods.parse(json) \ "response").extract[SectionsResponseThrift]
  }

  def parseEditions(json: String): EditionsResponse = {
    (JsonMethods.parse(json) \ "response").extract[EditionsResponse]
  }

  def parseEditionsThrift(json: String): EditionsResponseThrift = {
    (JsonMethods.parse(json) \ "response").extract[EditionsResponseThrift]
  }

  def parseError(json: String): Option[ErrorResponse] = for {
    parsedJson <- JsonMethods.parseOpt(json)
    response = parsedJson \ "response"
    errorResponse <- response.extractOpt[ErrorResponse]
  } yield errorResponse

  def parseErrorThrift(json: String): Option[ErrorResponseThrift] = for {
    parsedJson <- JsonMethods.parseOpt(json)
    response = parsedJson \ "response"
    errorResponse <- response.extractOpt[ErrorResponseThrift]
  } yield errorResponse

  def parseContent(json: String): Content = {
    JsonMethods.parse(json).transformField(fixFields).extract[Content]
  }

  private def fixFields: PartialFunction[JField, JField] = {
    case JField("summary", JString(s)) => JField("summary", JBool(s.toBoolean))
    case JField("keyEvent", JString(s)) => JField("keyEvent", JBool(s.toBoolean))
    case JField("pinned", JString(s)) => JField("pinned", JBool(s.toBoolean))
    case JField("showInRelatedContent", JString(s)) => JField("showInRelatedContent", JBool(s.toBoolean))
    case JField("shouldHideAdverts", JString(s)) => JField("shouldHideAdverts", JBool(s.toBoolean))
    case JField("hasStoryPackage", JString(s)) => JField("hasStoryPackage", JBool(s.toBoolean))
    case JField("isExpired", JString(s)) => JField("isExpired", JBool(s.toBoolean))
    case JField("syndicatable", JString(s)) => JField("syndicatable", JBool(s.toBoolean))
    case JField("subscriptionDatabases", JString(s)) => JField("subscriptionDatabases", JBool(s.toBoolean))
    case JField("developerCommunity", JString(s)) => JField("developerCommunity", JBool(s.toBoolean))
    case JField("commentable", JString(s)) => JField("commentable", JBool(s.toBoolean))
    case JField("liveBloggingNow", JString(s)) => JField("liveBloggingNow", JBool(s.toBoolean))
    case JField("isPremoderated", JString(s)) => JField("isPremoderated", JBool(s.toBoolean))
    case JField("wordcount", JString(s)) => JField("wordcount", JInt(s.toInt))
    case JField("newspaperPageNumber", JString(s)) => JField("newspaperPageNumber", JInt(s.toInt))
    case JField("starRating", JString(s)) => JField("starRating", JInt(s.toInt))
    case JField("isInappropriateForSponsorship", JString(s)) => JField("isInappropriateForSponsorship", JBool(s.toBoolean))
    case JField("isInappropriateForAdverts", JString(s)) => JField("isInappropriateForAdverts", JBool(s.toBoolean))
    case JField("internalPageCode", JString(s)) => JField("internalPageCode", JInt(s.toInt))
    case JField("internalStoryPackageCode", JString(s)) => JField("internalStoryPackageCode", JInt(s.toInt))
    case JField("width", JString(s)) => JField("width", JInt(s.toInt))
    case JField("height", JString(s)) => JField("height", JInt(s.toInt))
    case JField("duration", JString(s)) => JField("duration", JInt(s.toInt))
    case JField("durationMinutes", JString(s)) => JField("durationMinutes", JInt(s.toInt))
    case JField("durationSeconds", JString(s)) => JField("durationSeconds", JInt(s.toInt))
    case JField("isMaster", JString(s)) => JField("isMaster", JBool(s.toBoolean))
    case JField("sizeInBytes", JString(s)) => JField("sizeInBytes", JLong(s.toLong))
    case JField("blockAds", JString(s)) => JField("blockAds", JBool(s.toBoolean))
    case JField("allowUgc", JString(s)) => JField("allowUgc", JBool(s.toBoolean))
    case JField("displayCredit", JString(s)) => JField("displayCredit", JBool(s.toBoolean))
    case JField("legallySensitive", JString(s)) => JField("legallySensitive", JBool(s.toBoolean))
    case JField("sensitive", JString(s)) => JField("sensitive", JBool(s.toBoolean))
    case JField("explicit", JString(s)) => JField("explicit", JBool(s.toBoolean))
    case JField("clean", JString(s)) => JField("clean", JBool(s.toBoolean))
    case JField("safeEmbedCode", JString(s)) => JField("safeEmbedCode", JBool(s.toBoolean))
  }

  def generateJson[A <: ThriftEnum]: PartialFunction[Any, JString] = {
    case a: ThriftEnum => JString(a.name)
  }

  /** Normalise a string for use in enum lookup by removing hyphens */
  def norm(s: String): String = s.replaceAllLiterally("-", "")
}

import JsonParser._


object Helper {
  def createChangeRecord(obj: JObject,  dateField: String, userField: String): Option[ChangeRecord] = {
    val optionalDate = (obj \ dateField).extractOpt[String].map(new DateTime(_).getMillis)
    optionalDate.map { d =>
      val user = (obj \ userField).extractOpt[com.gu.contentatom.thrift.User]
      ChangeRecord(d, user)
    }
  }

  def fixAtomsFields: PartialFunction[JField, JField] = {
    case JField("date", JString(s)) => JField("date", JLong(new DateTime(s).getMillis))
  }

  def createAtom(atom: JObject, atomType: AtomType, atomData: AtomData): Atom = {
    val atomId = (atom \ "id").extract[String]
    val labels = (atom \ "labels").extract[Seq[String]]
    val defaultHtml = (atom \ "defaultHtml").extract[String]

    val lastModified = createChangeRecord(atom, "lastModifiedDate", "lastModifiedBy")
    val created = createChangeRecord(atom, "createdDate", "createdBy")
    val published = createChangeRecord(atom, "publishDate", "publishBy")

    val revision = (atom \ "revision").extract[Long]
    val change = ContentChangeDetails(lastModified, created, published, revision)

    Atom(atomId, atomType, labels, defaultHtml, atomData, change)
  }

  // For adding a new atom type define a new function here and then add it to the AtomSerializer

  def getQuizIfExists(rawAtom: JObject): Option[Seq[Atom]] = {
    Try((rawAtom \ "quizzes").extract[Seq[JObject]]) match {
      case Success(quizAtoms) =>
        Some(quizAtoms map { quizAtom =>
          val atomData = AtomData.Quiz((quizAtom \ "data").extract[QuizAtom])
          createAtom(quizAtom, AtomType.Quiz, atomData)
        })
      case Failure(_) => None
    }
  }

  def getViewpointsIfExists(rawAtom: JObject): Option[Seq[Atom]] = {
    Try((rawAtom \ "viewpoints").extract[Seq[JObject]]) match {
      case Success(viewpointAtoms) =>
        Some(viewpointAtoms map { viewpointAtom =>
          val atomData = AtomData.Viewpoints((viewpointAtom \ "data").transformField(fixAtomsFields).extract[ViewpointsAtom])
          createAtom(viewpointAtom, AtomType.Viewpoints, atomData)
        })
      case Failure(_) => None
    }
  }
}

import Helper._

object AtomSerializer extends CustomSerializer[Atoms](format => (
  {
    case rawAtom: JObject => Atoms(quizzes = getQuizIfExists(rawAtom), viewpoints = getViewpointsIfExists(rawAtom))
    case JNull => null
  },
  generateJson[ContentType]
  )) {
}

object ContentTypeSerializer extends CustomSerializer[ContentType](format => (
  {
    case JString(s) => ContentType.valueOf(norm(s)).getOrElse(ContentType.Article)
    case JNull => null
  },
   generateJson[ContentType]
  ))

object MembershipTierSerializer extends CustomSerializer[MembershipTier](format => (
  {
    case JString(s) => MembershipTier.valueOf(norm(s)).getOrElse(MembershipTier.MembersOnly)
    case JNull => null
  },
   generateJson[MembershipTier]
  ))

object OfficeSerializer extends CustomSerializer[Office](format => (
  {
    case JString(s) => Office.valueOf(norm(s)).getOrElse(Office.Uk)
    case JNull => null
  },
   generateJson[Office]
  ))

object AssetTypeSerializer extends CustomSerializer[AssetType](format => (
  {
    case JString(s) => AssetType.valueOf(norm(s)).getOrElse(AssetType.Image)
    case JNull => null
  },
   generateJson[AssetType]
  ))

object ElementTypeSerializer extends CustomSerializer[ElementType](format => (
  {
    case JString(s) => ElementType.valueOf(norm(s)).getOrElse(ElementType.Text)
    case JNull => null
  },
   generateJson[ElementType]
  ))

object TagTypeSerializer extends CustomSerializer[TagType](format => (
  {
    case JString(s) => TagType.valueOf(norm(s)).getOrElse(TagType.Contributor)
    case JNull => null
  },
   generateJson[TagType]
  ))

object CrosswordTypeSerializer extends CustomSerializer[CrosswordType](format => (
  {
    case JString(s) => CrosswordType.valueOf(norm(s)).getOrElse(CrosswordType.Quick)
    case JNull => null
  },
   generateJson[CrosswordType]
  ))

object StoryPackageArticleTypeSerializer extends CustomSerializer[ArticleType](format => (
  {
    case JString(s) => ArticleType.valueOf(norm(s)).getOrElse(ArticleType.Article)
    case JNull => null
  },
   generateJson[ArticleType]
  ))

object StoryPackageGroupSerializer extends CustomSerializer[Group](format => (
  {
    case JString(s) => Group.valueOf(norm(s)).getOrElse(Group.Linked)
    case JNull => null
  },
   generateJson[Group]
  ))

object DateTimeSerializer extends CustomSerializer[CapiDateTime](format => (
  {
    case JString(s) => {
      CapiDateTime.apply(ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(s).getMillis)
    }
    case JNull => null
  },
  {
    // This is never used because we never generate JSON, only parse it
    case d: CapiDateTime => JString(new DateTime(d.dateTime).toString)
  }
  ))
