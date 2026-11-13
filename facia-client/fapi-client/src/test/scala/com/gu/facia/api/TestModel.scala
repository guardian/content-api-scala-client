package com.gu.facia.api

import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.model.schemaorg.SchemaOrg
import play.api.libs.json._

import scala.io.Source

object TestModel {

  private def getJson(fileName: String): JsValue =
    Json.parse(
      Source.fromURL(getClass.getResource(s"/branding/$fileName")).mkString
    )

  trait HasName[A] { def nameOf(a: A): String }

  object HasName {
    implicit object ContentType extends HasName[ContentType] {
      def nameOf(t: ContentType): String = t.name
    }
    implicit object TagType extends HasName[TagType] {
      def nameOf(t: TagType): String = t.name
    }
    implicit object ElementType extends HasName[ElementType] {
      def nameOf(t: ElementType): String = t.name
    }
    implicit object SponsorshipType extends HasName[SponsorshipType] {
      def nameOf(t: SponsorshipType): String = t.name
    }
    implicit object SponsorshipPackage extends HasName[SponsorshipPackage] {
      def nameOf(t: SponsorshipPackage): String = t.name
    }
  }

  private def byName[A](otherName: String)(a: A)(implicit
      n: HasName[A]
  ): Boolean =
    n.nameOf(a).toLowerCase == otherName.replaceAll("-", "")

  implicit val capiDateReads: Reads[CapiDateTime] = __.read[String] map { dateString =>
    CapiDateTime(
      LocalDateTime
        .parse(dateString, ISO_OFFSET_DATE_TIME)
        .toInstant(UTC)
        .toEpochMilli,
      dateString
    )
  }

  case class TestSponsorshipTargeting(
      publishedSince: Option[CapiDateTime],
      validEditions: Option[Seq[String]]
  ) extends SponsorshipTargeting
  implicit val testSponsorshipTargeting: Reads[TestSponsorshipTargeting] =
    Json.reads[TestSponsorshipTargeting]

  case class TestLogoDimensions(width: Int, height: Int) extends SponsorshipLogoDimensions
  implicit val testLogoDimensionsFormat: Reads[TestLogoDimensions] =
    Json.reads[TestLogoDimensions]

  case class TestSponsorship(
      sponsorshipTypeName: String,
      sponsorshipPackageName: Option[String],
      sponsorName: String,
      sponsorLogo: String,
      sponsorLink: String,
      targeting: Option[TestSponsorshipTargeting],
      aboutLink: Option[String],
      sponsorLogoDimensions: Option[TestLogoDimensions],
      highContrastSponsorLogo: Option[String],
      highContrastSponsorLogoDimensions: Option[TestLogoDimensions]
  ) extends Sponsorship {
    def sponsorshipType: SponsorshipType =
      SponsorshipType.list.find(byName(sponsorshipTypeName)(_)).get
    def sponsorshipPackage: Option[SponsorshipPackage] =
      sponsorshipPackageName.flatMap(p => SponsorshipPackage.list.find(byName(p)(_)))
    def validFrom = None
    def validTo = None
  }
  implicit val testSponsorShipFormat: Reads[TestSponsorship] =
    Json.reads[TestSponsorship]

  case class StubSection(
      id: String,
      webTitle: String,
      activeSponsorships: Option[Seq[TestSponsorship]]
  ) extends Section {
    def webUrl: String = ""
    def apiUrl: String = ""
    def editions: Seq[Edition] = Nil
  }
  implicit val stubSectionFormat: Reads[StubSection] = Json.reads[StubSection]

  case class StubFields(isInappropriateForSponsorship: Option[Boolean]) extends ContentFields {
    def headline: Option[String] = None
    def standfirst: Option[String] = None
    def trailText: Option[String] = None
    def byline: Option[String] = None
    def bylineHtml: Option[String] = None
    def main: Option[String] = None
    def body: Option[String] = None
    def newspaperPageNumber: Option[Int] = None
    def starRating: Option[Int] = None
    def contributorBio: Option[String] = None
    def membershipAccess: Option[MembershipTier] = None
    def wordcount: Option[Int] = None
    def commentCloseDate: Option[CapiDateTime] = None
    def commentable: Option[Boolean] = None
    def creationDate: Option[CapiDateTime] = None
    def displayHint: Option[String] = None
    def firstPublicationDate: Option[CapiDateTime] = None
    def hasStoryPackage: Option[Boolean] = None
    def internalComposerCode: Option[String] = None
    def internalOctopusCode: Option[String] = None
    def internalPageCode: Option[Int] = None
    def internalStoryPackageCode: Option[Int] = None
    def isPremoderated: Option[Boolean] = None
    def lastModified: Option[CapiDateTime] = None
    def liveBloggingNow: Option[Boolean] = None
    def newspaperEditionDate: Option[CapiDateTime] = None
    def productionOffice: Option[Office] = None
    def publication: Option[String] = None
    def scheduledPublicationDate: Option[CapiDateTime] = None
    def secureThumbnail: Option[String] = None
    def shortUrl: Option[String] = None
    def shouldHideAdverts: Option[Boolean] = None
    def showInRelatedContent: Option[Boolean] = None
    def thumbnail: Option[String] = None
    def legallySensitive: Option[Boolean] = None
    def allowUgc: Option[Boolean] = None
    def sensitive: Option[Boolean] = None
    def lang: Option[String] = None
    def internalRevision: Option[Int] = None
    def internalContentCode: Option[Int] = None
    def isLive: Option[Boolean] = None
    def internalShortId: Option[String] = None
    def shortSocialShareText: Option[String] = None
    def socialShareText: Option[String] = None
    def bodyText: Option[String] = None
    def charCount: Option[Int] = None
    def internalVideoCode: Option[String] = None
    def shouldHideReaderRevenue: Option[Boolean] = None
    def internalCommissionedWordcount: Option[Int] = None
    def showAffiliateLinks: Option[Boolean] = None
    def showTableOfContents: Option[Boolean] = None
    def livePricingEnabled: Option[Boolean] = None
  }
  implicit val stubFieldsFormat: Reads[StubFields] = Json.reads[StubFields]

  case class StubTag(
      id: String,
      typeName: String,
      webTitle: String,
      activeSponsorships: Option[Seq[TestSponsorship]]
  ) extends Tag {
    def `type`: TagType = TagType.list.find(byName(typeName)(_)).get
    def sectionId: Option[String] = None
    def sectionName: Option[String] = None
    def webUrl: String = ""
    def apiUrl: String = ""
    def references: Seq[Reference] = Nil
    def description: Option[String] = None
    def bio: Option[String] = None
    def bylineImageUrl: Option[String] = None
    def bylineLargeImageUrl: Option[String] = None
    def podcast: Option[Podcast] = None
    def firstName: Option[String] = None
    def lastName: Option[String] = None
    def emailAddress: Option[String] = None
    def twitterHandle: Option[String] = None
    def paidContentType: Option[String] = None
    def paidContentCampaignColour: Option[String] = None
    def rcsId: Option[String] = None
    def r2ContributorId: Option[String] = None
    def entityIds: Option[scala.collection.Set[String]] = None
    def tagCategories: Option[scala.collection.Set[String]] = None
    def campaignInformationType: Option[String] = None
    def internalName: Option[String] = None
    def keywordType: Option[com.gu.contentapi.client.model.v1.KeywordType] =
      None
  }
  implicit val stubTagFormat: Reads[StubTag] = Json.reads[StubTag]

  case class StubElement(
      id: String,
      relation: String,
      typeName: String
  ) extends Element {
    def `type`: ElementType = ElementType.list.find(byName(typeName)(_)).get
    def galleryIndex: Option[Int] = None
    def assets: Seq[Asset] = Nil
  }
  implicit val stubElementFormat: Reads[StubElement] = Json.reads[StubElement]

  case class StubItem(
      id: String,
      typeName: String,
      section: Option[StubSection],
      fields: Option[StubFields],
      tags: Seq[StubTag],
      elements: Option[Seq[StubElement]]
  ) extends Content {
    def `type`: ContentType = ContentType.list.find(byName(typeName)(_)).get
    def sectionId: Option[String] = None
    def sectionName: Option[String] = None
    def webPublicationDate: Option[CapiDateTime] = None
    def webTitle: String = ""
    def webUrl: String = ""
    def apiUrl: String = ""
    def references: Seq[Reference] = Nil
    def isExpired: Option[Boolean] = None
    def blocks: Option[Blocks] = None
    def rights: Option[Rights] = None
    def crossword: Option[Crossword] = None
    def atoms: Option[Atoms] = None
    def stats: Option[ContentStats] = None
    def debug: Option[Debug] = None
    def isGone: Option[Boolean] = None
    def isHosted: Boolean = false
    def pillarId: Option[String] = None
    def pillarName: Option[String] = None
    def aliasPaths: Option[Seq[AliasPath]] = None
    def channels: Option[collection.Seq[ContentChannel]] = None
    def schemaOrg: Option[SchemaOrg] = None
    def abTests: Option[collection.Seq[ABTest]] = None
    def isVariantOf: Option[String] = None
  }
  implicit val stubItemFormat: Reads[StubItem] = Json.reads[StubItem]

  def getContentItem(fileName: String): Content =
    getJson(fileName).validate[StubItem].get
  def getTag(fileName: String): Tag = getJson(fileName).validate[StubTag].get

}
