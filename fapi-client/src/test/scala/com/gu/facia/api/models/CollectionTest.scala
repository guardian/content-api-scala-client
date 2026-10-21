package com.gu.facia.api.models

import com.gu.contentapi.client.model.v1.{Content, ContentFields, ContentType}
import com.gu.facia.api.utils.ContentApiUtils._
import com.gu.facia.api.utils._
import com.gu.facia.client.models.{
  Branded,
  CollectionConfigJson,
  CollectionJson,
  Trail,
  TrailMetaData
}
import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsArray, JsString, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import com.gu.contentapi.client.ContentApiClient

class CollectionTest
    extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with OneInstancePerTest {
  val trailMetadata = spy(TrailMetaData.empty)
  val trail =
    Trail("internal-code/page/123", 1, None, Some(trailMetadata), None)
  val collectionJson = CollectionJson(
    live = List(trail),
    draft = None,
    treats = None,
    lastUpdated = new DateTime(1),
    updatedBy = "test",
    updatedEmail = "test@example.com",
    displayName = Some("displayName"),
    href = Some("collectionHref"),
    previously = None,
    targetedTerritory = None
  )
  val content = Content(
    id = "content-id",
    `type` = ContentType.Article,
    sectionId = Some("section-id"),
    sectionName = Some("Section Name"),
    webPublicationDate = None,
    webTitle = "webTitle",
    webUrl = "webUrl",
    apiUrl = "apiUrl",
    fields = Some(
      ContentFields(
        headline = Some("Content headline"),
        trailText = Some("Content trailtext"),
        byline = Some("Content byline"),
        internalPageCode = Some(123)
      )
    ),
    tags = Nil,
    elements = None,
    references = Nil,
    isExpired = None,
    blocks = None,
    rights = None,
    crossword = None
  )

  val contents = Set(content)
  val collectionConfig = CollectionConfig.fromCollectionJson(
    CollectionConfigJson.withDefaults(
      metadata = Some(List(Branded)),
      href = Some("collectionConfigHref")
    )
  )

  def makeLatestSnap(
      id: String = "id",
      maybeFrontPublicationDate: Option[Long] = None,
      snapUri: Option[String] = None,
      snapCss: Option[String] = None,
      latestContent: Option[Content] = None,
      headline: Option[String] = None,
      href: Option[String] = None,
      trailText: Option[String] = None,
      group: String = "0",
      image: Option[FaciaImage] = None,
      contentProperties: ContentProperties =
        ContentProperties.fromResolvedMetaData(ResolvedMetaData.Default),
      byline: Option[String] = None,
      kicker: Option[ItemKicker] = None
  ): LatestSnap =
    LatestSnap(
      id,
      maybeFrontPublicationDate,
      DefaultCardstyle,
      ContentFormat.defaultContentFormat,
      snapUri,
      snapCss,
      latestContent,
      headline,
      href,
      trailText,
      group,
      image,
      contentProperties,
      byline,
      kicker,
      latestContent map (_.brandingByEdition) getOrElse Map.empty,
      atomId = None,
      None
    )

  def makeLinkSnap(
      id: String = "id",
      maybeFrontPublicationDate: Option[Long] = None,
      snapType: String = Snap.DefaultType,
      snapUri: Option[String] = None,
      snapCss: Option[String] = None,
      atomId: Option[String] = None,
      headline: Option[String] = None,
      href: Option[String] = None,
      trailText: Option[String] = None,
      group: String = "0",
      image: Option[FaciaImage] = None,
      contentProperties: ContentProperties =
        ContentProperties.fromResolvedMetaData(ResolvedMetaData.Default),
      byline: Option[String] = None,
      kicker: Option[ItemKicker] = None
  ): LinkSnap =
    LinkSnap(
      id,
      maybeFrontPublicationDate,
      snapType,
      snapUri,
      snapCss,
      atomId,
      headline,
      href,
      trailText,
      group,
      image,
      contentProperties,
      byline,
      kicker,
      Map.empty,
      None
    )

  "fromCollectionJson" - {

    val collection = Collection.fromCollectionJsonConfigAndContent(
      "id",
      Some(collectionJson),
      collectionConfig
    )

    "creates a Facia collection from the collection JSON and provided config" in {
      collection should have(
        Symbol("id")("id"),
        Symbol("draft")(None),
        Symbol("href")(Some("collectionConfigHref")),
        Symbol("lastUpdated")(Some(new DateTime(1))),
        Symbol("updatedBy")(Some("test")),
        Symbol("updatedEmail")(Some("test@example.com")),
        Symbol("displayName")("displayName")
      )

      collection.collectionConfig should have(
        Symbol("href")(Some("collectionConfigHref"))
      )
    }

    "creates metadata from provided config JSON" in {
      collection.collectionConfig should have(
        Symbol("metadata")(Some(List(Branded)))
      )
    }
  }

  "liveContent" - {
    val collection = Collection.fromCollectionJsonConfigAndContent(
      "id",
      Some(collectionJson),
      collectionConfig
    )
    val capiClient = mock[ContentApiClient]

    "Uses content fields when no facia override exists" in {

      Collection
        .liveContent(collection, contents)(capiClient, global)
        .map(curatedContent =>
          curatedContent.head should have(
            Symbol("headline")("Content headline")
          )
        )
    }

    "Resolves metadata from facia where fields exist" in {
      when(trailMetadata.headline).thenReturn(Some("trail headline"))
      when(trailMetadata.href).thenReturn(Some("trail href"))
      when(trailMetadata.customKicker).thenReturn(Some("Custom kicker"))
      when(trailMetadata.showKickerCustom).thenReturn(Some(true))

      Collection
        .liveContent(collection, contents)(capiClient, global)
        .map(curatedContent =>
          curatedContent.head should have(
            Symbol("headline")("trail headline"),
            Symbol("href")(Some("trail href")),
            Symbol("kicker")(Some(FreeHtmlKicker("Custom kicker")))
          )
        )

    }

    "excludes trails where no corresponding content is found" in {
      val trail2 = Trail("content-id-2", 2, None, Some(trailMetadata), None)

      Collection.liveContent(collection, contents)(capiClient, global).map {
        curatedContent =>
          val curatedIds = curatedContent.collect { case c: CuratedContent =>
            c.content.id
          }

          curatedIds should equal(List("content-id"))
      }
    }

    "Successfully retrieve snaps from snapContent for latest snaps" in {
      val snapOne = Trail(
        "snap/1415985080061",
        1,
        None,
        Some(
          TrailMetaData(
            Map("snapType" -> JsString("link"), "snapUri" -> JsString("abc"))
          )
        ),
        None
      )
      val snapTwo = Trail(
        "snap/5345345215342",
        1,
        None,
        Some(
          TrailMetaData(
            Map("snapType" -> JsString("link"), "snapCss" -> JsString("css"))
          )
        ),
        None
      )
      val snapLatestOne = Trail(
        "snap/8474745745660",
        1,
        None,
        Some(
          TrailMetaData(
            Map("snapType" -> JsString("latest"), "href" -> JsString("uk"))
          )
        ),
        None
      )
      val snapLatestTwo = Trail(
        "snap/4324234234234",
        1,
        None,
        Some(
          TrailMetaData(
            Map("snapType" -> JsString("latest"), "href" -> JsString("culture"))
          )
        ),
        None
      )

      val snapContentOne = Content(
        id = "content-id-one",
        `type` = ContentType.Article,
        sectionId = Some("section-id"),
        sectionName = Some("Section Name"),
        webPublicationDate = None,
        webTitle = "webTitle",
        webUrl = "webUrl",
        apiUrl = "apiUrl",
        fields = Some(
          ContentFields(
            headline = Some("Content headline"),
            trailText = Some("Content trailtext"),
            byline = Some("Content byline")
          )
        ),
        tags = Nil,
        elements = None,
        references = Nil,
        isExpired = None,
        blocks = None,
        rights = None,
        crossword = None
      )

      val snapContentTwo = Content(
        id = "content-id-two",
        `type` = ContentType.Article,
        sectionId = Some("section-id"),
        sectionName = Some("Section Name"),
        webPublicationDate = None,
        webTitle = "webTitle",
        webUrl = "webUrl",
        apiUrl = "apiUrl",
        fields = Some(
          ContentFields(
            headline = Some("Content headline"),
            trailText = Some("Content trailtext"),
            byline = Some("Content byline")
          )
        ),
        tags = Nil,
        elements = None,
        references = Nil,
        isExpired = None,
        blocks = None,
        rights = None,
        crossword = None
      )

      val snapContent = Map(
        "snap/8474745745660" -> Some(snapContentOne),
        "snap/4324234234234" -> Some(snapContentTwo)
      )

      val collectionJsonTwo = collectionJson.copy(live =
        List(snapOne, snapTwo, snapLatestOne, snapLatestTwo)
      )

      val collection = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Some(collectionJsonTwo),
        collectionConfig
      )

      Collection
        .liveContent(collection, Set.empty, snapContent)(capiClient, global)
        .map { faciaContent =>
          faciaContent.length should be(4)
          faciaContent(0) should be(
            makeLinkSnap(
              id = "snap/1415985080061",
              maybeFrontPublicationDate = Option(1L),
              snapUri = Some("abc")
            )
          )
          faciaContent(1) should be(
            makeLinkSnap(
              id = "snap/5345345215342",
              maybeFrontPublicationDate = Option(1L),
              snapCss = Some("css")
            )
          )
          faciaContent(2) should be(
            makeLatestSnap(
              id = "snap/8474745745660",
              maybeFrontPublicationDate = Option(1L),
              href = Some("uk"),
              headline = Some("Content headline"),
              byline = Some("Content byline"),
              trailText = Some("Content trailtext"),
              latestContent = Some(snapContentOne)
            )
          )
          faciaContent(3) should be(
            makeLatestSnap(
              id = "snap/4324234234234",
              maybeFrontPublicationDate = Option(1L),
              href = Some("culture"),
              headline = Some("Content headline"),
              byline = Some("Content byline"),
              trailText = Some("Content trailtext"),
              latestContent = Some(snapContentTwo)
            )
          )
        }
    }
  }

  "liveIdsWithoutSnaps" - {
    val trailOne =
      Trail("internal-code/page/1", 1, None, Some(trailMetadata), None)
    val trailTwo =
      Trail("internal-code/page/2", 1, None, Some(trailMetadata), None)
    val trailThree = Trail(
      "artanddesign/gallery/2014/feb/28/beyond-basquiat-black-artists",
      1,
      None,
      Some(trailMetadata),
      None
    )
    val snapOne =
      Trail("snap/1415985080061", 1, None, Some(trailMetadata), None)
    val snapTwo =
      Trail("snap/5345345215342", 1, None, Some(trailMetadata), None)

    val collectionJsonTwo = collectionJson.copy(live =
      List(trailOne, snapOne, snapTwo, trailTwo, trailThree)
    )

    val collection = Collection.fromCollectionJsonConfigAndContent(
      "id",
      Some(collectionJsonTwo),
      collectionConfig
    )

    "Collection should start with 5 items" in {
      collection.live.length should be(5)
    }

    "Removes snaps from a collection and returns the ids to query" in {
      Collection.liveContentIdsWithoutSnaps(collection) should be
      List(
        "internal-code/page/1",
        "internal-code/page/2",
        "artanddesign/gallery/2014/feb/28/beyond-basquiat-black-artists"
      )
    }

    "Empty out snaps" in {
      val collectionJsonAllSnaps =
        collectionJson.copy(live = List(snapOne, snapTwo))
      val collectionAllSnaps = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Some(collectionJsonAllSnaps),
        collectionConfig
      )

      Collection.liveContentIdsWithoutSnaps(collectionAllSnaps) should be(
        List.empty
      )
    }
  }

  "Collection" - {
    "filter out the snaps in live" in {
      val trailOne =
        Trail("internal-code/page/1", 1, None, Some(trailMetadata), None)
      val trailTwo =
        Trail("internal-code/page/2", 1, None, Some(trailMetadata), None)
      val snapOne = Trail(
        "snap/1415985080061",
        1,
        None,
        Some(
          TrailMetaData(
            Map("snapType" -> JsString("link"), "snapUri" -> JsString("abc"))
          )
        ),
        None
      )
      val snapTwo = Trail(
        "snap/5345345215342",
        1,
        None,
        Some(
          TrailMetaData(
            Map("snapType" -> JsString("link"), "snapCss" -> JsString("css"))
          )
        ),
        None
      )
      val snapLatestOne = Trail(
        "snap/8474745745660",
        1,
        None,
        Some(
          TrailMetaData(
            Map("snapType" -> JsString("latest"), "href" -> JsString("uk"))
          )
        ),
        None
      )
      val snapLatestTwo = Trail(
        "snap/4324234234234",
        1,
        None,
        Some(
          TrailMetaData(
            Map("snapType" -> JsString("latest"), "href" -> JsString("culture"))
          )
        ),
        None
      )

      val snapContentOne = Content(
        id = "content-id-one",
        `type` = ContentType.Article,
        sectionId = Some("section-id"),
        sectionName = Some("Section Name"),
        webPublicationDate = None,
        webTitle = "webTitle",
        webUrl = "webUrl",
        apiUrl = "apiUrl",
        fields = Some(
          ContentFields(
            headline = Some("Content headline"),
            trailText = Some("Content trailtext"),
            byline = Some("Content byline")
          )
        ),
        tags = Nil,
        elements = None,
        references = Nil,
        isExpired = None,
        blocks = None,
        rights = None,
        crossword = None
      )

      val snapContentTwo = Content(
        id = "content-id-two",
        `type` = ContentType.Article,
        sectionId = Some("section-id"),
        sectionName = Some("Section Name"),
        webPublicationDate = None,
        webTitle = "webTitle",
        webUrl = "webUrl",
        apiUrl = "apiUrl",
        fields = Some(
          ContentFields(
            headline = Some("Content headline"),
            trailText = Some("Content trailtext"),
            byline = Some("Content byline")
          )
        ),
        tags = Nil,
        elements = None,
        references = Nil,
        isExpired = None,
        blocks = None,
        rights = None,
        crossword = None
      )

      val snapContent = Map(
        "snap/8474745745660" -> Some(snapContentOne),
        "snap/4324234234234" -> Some(snapContentTwo)
      )

      val collectionJsonTwo = collectionJson.copy(live =
        List(snapOne, snapTwo, trailOne, snapLatestOne, snapLatestTwo, trailTwo)
      )

      val collection = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Some(collectionJsonTwo),
        collectionConfig
      )

      val collectionWithoutSnaps = Collection.withoutSnaps(collection)

      collectionWithoutSnaps.live.length should be(2)
      collectionWithoutSnaps.live(0).id should be("internal-code/page/1")
      collectionWithoutSnaps.live(1).id should be("internal-code/page/2")
    }

    "filter out the snaps in draft" in {
      val trailOne =
        Trail("internal-code/page/1", 1, None, Some(trailMetadata), None)
      val trailTwo =
        Trail("internal-code/page/2", 1, None, Some(trailMetadata), None)
      val snapOne = Trail(
        "snap/1415985080061",
        1,
        None,
        Some(
          TrailMetaData(
            Map("snapType" -> JsString("link"), "snapUri" -> JsString("abc"))
          )
        ),
        None
      )
      val snapTwo = Trail(
        "snap/5345345215342",
        1,
        None,
        Some(
          TrailMetaData(
            Map("snapType" -> JsString("link"), "snapCss" -> JsString("css"))
          )
        ),
        None
      )
      val snapLatestOne = Trail(
        "snap/8474745745660",
        1,
        None,
        Some(
          TrailMetaData(
            Map("snapType" -> JsString("latest"), "href" -> JsString("uk"))
          )
        ),
        None
      )
      val snapLatestTwo = Trail(
        "snap/4324234234234",
        1,
        None,
        Some(
          TrailMetaData(
            Map("snapType" -> JsString("latest"), "href" -> JsString("culture"))
          )
        ),
        None
      )

      val snapContentOne = Content(
        id = "content-id-one",
        `type` = ContentType.Article,
        sectionId = Some("section-id"),
        sectionName = Some("Section Name"),
        webPublicationDate = None,
        webTitle = "webTitle",
        webUrl = "webUrl",
        apiUrl = "apiUrl",
        fields = Some(
          ContentFields(
            headline = Some("Content headline"),
            trailText = Some("Content trailtext"),
            byline = Some("Content byline")
          )
        ),
        tags = Nil,
        elements = None,
        references = Nil,
        isExpired = None,
        blocks = None,
        rights = None,
        crossword = None
      )

      val snapContentTwo = Content(
        id = "content-id-two",
        `type` = ContentType.Article,
        sectionId = Some("section-id"),
        sectionName = Some("Section Name"),
        webPublicationDate = None,
        webTitle = "webTitle",
        webUrl = "webUrl",
        apiUrl = "apiUrl",
        fields = Some(
          ContentFields(
            headline = Some("Content headline"),
            trailText = Some("Content trailtext"),
            byline = Some("Content byline")
          )
        ),
        tags = Nil,
        elements = None,
        references = Nil,
        isExpired = None,
        blocks = None,
        rights = None,
        crossword = None
      )

      val snapContent = Map(
        "snap/8474745745660" -> Some(snapContentOne),
        "snap/4324234234234" -> Some(snapContentTwo)
      )

      val collectionJsonTwo = collectionJson.copy(draft =
        Option(
          List(
            snapOne,
            snapTwo,
            trailTwo,
            snapLatestOne,
            snapLatestTwo,
            trailOne
          )
        )
      )

      val collection = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Some(collectionJsonTwo),
        collectionConfig
      )

      val collectionWithoutSnaps = Collection.withoutSnaps(collection)

      collectionWithoutSnaps.draft.map(_.length) should be(Some(2))
      collectionWithoutSnaps.draft.map(_.apply(0).id) should be(
        Some("internal-code/page/2")
      )
      collectionWithoutSnaps.draft.map(_.apply(1).id) should be(
        Some("internal-code/page/1")
      )
    }
  }

  "Internal code" - {
    val capiClient = mock[ContentApiClient]

    val baseContent = Content(
      id = "content-id-one",
      `type` = ContentType.Article,
      sectionId = Some("section-id"),
      sectionName = Some("Section Name"),
      webPublicationDate = None,
      webTitle = "webTitle",
      webUrl = "webUrl",
      apiUrl = "apiUrl",
      fields = None,
      tags = Nil,
      elements = None,
      references = Nil,
      isExpired = None,
      blocks = None,
      rights = None,
      crossword = None
    )

    val contents = Set(
      baseContent.copy(fields =
        Some(
          ContentFields(
            internalPageCode = Some(3),
            headline = Some("red apple"),
            trailText = Some("Content trailtext"),
            byline = Some("Content byline")
          )
        )
      ),
      baseContent.copy(fields =
        Some(
          ContentFields(
            internalPageCode = Some(2),
            headline = Some("straight banana"),
            trailText = Some("Content trailtext"),
            byline = Some("Content byline")
          )
        )
      ),
      baseContent.copy(fields =
        Some(
          ContentFields(
            internalPageCode = Some(6),
            headline = Some("hairy kiwi"),
            trailText = Some("Content trailtext"),
            byline = Some("Content byline")
          )
        )
      ),
      baseContent.copy(fields =
        Some(
          ContentFields(
            internalPageCode = Some(7),
            headline = Some("straw berry"),
            trailText = Some("Content trailtext"),
            byline = Some("Content byline")
          )
        )
      )
    )

    def makeTrail(id: String) =
      Trail(id, 0, None, None, None)
    def makeTrailWithSupporting(id: String, supporting: Trail*) =
      Trail(
        id,
        0,
        None,
        Some(
          TrailMetaData(
            Map("supporting" -> JsArray(supporting.map(Json.toJson(_))))
          )
        ),
        None
      )
    "trails with internalPageCode" in {
      val trail =
        Trail("internal-code/page/2", 1, None, Some(trailMetadata), None)
      val collectionJsonWithPageCode = collectionJson.copy(live = List(trail))
      val collection = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Some(collectionJsonWithPageCode),
        collectionConfig
      )
      Collection.liveContent(collection, contents)(capiClient, global).map {
        content =>
          content.length should be(1)
          content.head should have(
            Symbol("headline")("straight banana")
          )
      }
    }
    "supporting with internalPageCode" in {
      val supporting = makeTrail("internal-code/page/2")
      val trail = makeTrailWithSupporting("internal-code/page/7", supporting)
      val collectionJsonWithPageCode = collectionJson.copy(live = List(trail))
      val collection = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Some(collectionJsonWithPageCode),
        collectionConfig
      )
      Collection.liveContent(collection, contents)(capiClient, global).map {
        content =>
          content.length should be(1)
          content.head should have(
            Symbol("headline")("straw berry")
          )
          val supportingContent =
            FaciaContentUtils.supporting(content.head).head
          FaciaContentUtils.headline(supportingContent) should be(
            "straight banana"
          )
      }
    }
    "AspectRatio.getAspectRatio" in {
      val defaultCollectionType = collectionConfig.collectionType
      val defaultCollectionConfig =
        CollectionConfig.empty.copy(collectionType = defaultCollectionType)
      val scrollableFeatureConfig =
        CollectionConfig.empty.copy(collectionType = "scrollable/feature")
      val scrollableSmallConfig =
        CollectionConfig.empty.copy(collectionType = "scrollable/small")
      val scrollableHighlightsConfig =
        CollectionConfig.empty.copy(collectionType = "scrollable/highlights")

      CollectionConfig.getAspectRatio(defaultCollectionConfig) should be(
        CollectionConfig.AspectRatio.Landscape53
      )
      CollectionConfig.getAspectRatio(scrollableFeatureConfig) should be(
        CollectionConfig.AspectRatio.Portrait45
      )
      CollectionConfig.getAspectRatio(scrollableSmallConfig) should be(
        CollectionConfig.AspectRatio.Landscape54
      )
      CollectionConfig.getAspectRatio(scrollableHighlightsConfig) should be(
        CollectionConfig.AspectRatio.Square
      )
    }

  }

  "maxSupportingItems" - {
    "should return 2 for non-splash cards in flexible/general collections with default boost level" in {
      val isSplashCard = false
      val collectionType = "flexible/general"
      val boostLevel = BoostLevel.Default.label
      Collection.maxSupportingItems(
        isSplashCard,
        collectionType,
        boostLevel
      ) shouldBe 2
    }

    "should return 2 for non-splash cards in flexible/special collections with default boost level" in {
      val isSplashCard = false
      val collectionType = "flexible/special"
      val boostLevel = BoostLevel.Default.label
      Collection.maxSupportingItems(
        isSplashCard,
        collectionType,
        boostLevel
      ) shouldBe 2
    }

    "should return 4 for non-splash cards in flexible/special collections with mega boost boost level" in {
      val isSplashCard = false
      val collectionType = "flexible/special"
      val boostLevel = BoostLevel.MegaBoost.label
      Collection.maxSupportingItems(
        isSplashCard,
        collectionType,
        boostLevel
      ) shouldBe 4
    }

    "should return 4 for splash cards in flexible/general collections" in {
      val isSplashCard = true
      val collectionType = "flexible/general"
      val boostLevel = BoostLevel.Default.label
      Collection.maxSupportingItems(
        isSplashCard,
        collectionType,
        boostLevel
      ) shouldBe 4
    }

    "should return 4 for splash cards in flexible/special collections" in {
      val isSplashCard = true
      val collectionType = "flexible/special"
      val boostLevel = BoostLevel.Default.label
      Collection.maxSupportingItems(
        isSplashCard,
        collectionType,
        boostLevel
      ) shouldBe 4
    }

    "should return 4 for any other beta collection type" in {
      val isSplashCard = false
      val collectionType = "scrollable/small"
      val boostLevel = BoostLevel.Default.label
      Collection.maxSupportingItems(
        isSplashCard,
        collectionType,
        boostLevel
      ) shouldBe 4
    }
  }
  "isSplashCard" - {
    "should return true for any card in a splash group of a flexible general" in {
      val collectionType = "flexible/general"
      val splashGroup = "3"
      val trailMetaData =
        Some(TrailMetaData(Map("group" -> JsString(splashGroup))))
      val trail = Trail("internal-code/page/1", 1, None, trailMetaData, None)
      val trailIndex = 0
      val collectionHasSnap = false;
      Collection.isSplashCard(
        trail,
        trailIndex,
        collectionType,
        collectionHasSnap
      ) should be(true)
    }
    "should return false for any card not in the splash group of a flexible general" in {
      val collectionType = "flexible/general"
      val bigGroup = "1"
      val trailMetaData =
        Some(TrailMetaData(Map("group" -> JsString(bigGroup))))
      val trail = Trail("internal-code/page/1", 1, None, trailMetaData, None)
      val trailIndex = 0
      val collectionHasSnap = false;
      Collection.isSplashCard(
        trail,
        trailIndex,
        collectionType,
        collectionHasSnap
      ) should be(false)
    }
    "should return true for the first card of a flexible special when there is no snap" in {
      val collectionType = "flexible/special"
      val trailIndex = 0
      val collectionHasSnap = false;
      Collection.isSplashCard(
        trail,
        trailIndex,
        collectionType,
        collectionHasSnap
      ) should be(true)
    }
    "should return false for the first card in a flexible special where there is a snap" in {
      val collectionType = "flexible/special"
      val trailIndex = 0
      val collectionHasSnap = true;
      Collection.isSplashCard(
        trail,
        trailIndex,
        collectionType,
        collectionHasSnap
      ) should be(false)
    }
    "should return true for the second card of a flexible special when there is a snap" in {
      val collectionType = "flexible/special"
      val trailIndex = 1
      val collectionHasSnap = true;
      Collection.isSplashCard(
        trail,
        trailIndex,
        collectionType,
        collectionHasSnap
      ) should be(true)
    }
    "should return false for the third card of a flexible special when there is a snap" in {
      val collectionType = "flexible/special"
      val trailIndex = 2
      val collectionHasSnap = true;
      Collection.isSplashCard(
        trail,
        trailIndex,
        collectionType,
        collectionHasSnap
      ) should be(false)
    }
    "should return false if the container is not flexible general or flexible special" in {
      val collectionType = "scrollable/medium"
      val trailIndex = 0
      val collectionHasSnap = false;
      Collection.isSplashCard(
        trail,
        trailIndex,
        collectionType,
        collectionHasSnap
      ) should be(false)
    }
  }

}
