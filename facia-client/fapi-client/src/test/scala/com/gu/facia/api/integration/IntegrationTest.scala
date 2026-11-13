package com.gu.facia.api.integration

import com.gu.contentapi.client.model.v1.TagType
import com.gu.facia.api.contentapi.ContentApi.{AdjustItemQuery, AdjustSearchQuery}
import com.gu.facia.api.models._
import com.gu.facia.api.utils.SectionKicker
import com.gu.facia.api.{ApiError, FAPI}
import com.gu.facia.client.models._
import lib.IntegrationTestConfig
import org.joda.time.DateTime
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.libs.json.{JsArray, JsString, Json}

// TODO: reinstate ignored tests when cmsFronts account has access to test fixtures
class IntegrationTest
    extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with OptionValues
    with IntegrationTestConfig {
  implicit val patience: PatienceConfig =
    PatienceConfig(Span(20, Seconds), Span(50, Millis))

  def makeCollectionJson(trails: Trail*) = CollectionJson(
    live = trails.toList,
    draft = None,
    treats = None,
    lastUpdated = new DateTime(1),
    updatedBy = "test",
    updatedEmail = "test@example.com",
    displayName = Some("displayName"),
    href = Some("href"),
    previously = None,
    None
  )

  def makeLatestTrailFor(id: String, uri: String) =
    Trail(
      id,
      0,
      None,
      Some(
        TrailMetaData(
          Map("snapUri" -> JsString(uri), "snapType" -> JsString("latest"))
        )
      ),
      None
    )
  def makeLinkSnapFor(id: String, uri: String) =
    Trail(
      id,
      0,
      None,
      Some(
        TrailMetaData(
          Map("snapUri" -> JsString(uri), "snapType" -> JsString("link"))
        )
      ),
      None
    )

  "getFronts" - {
    "should return a set of Front instances from the fronts JSON" in {
      FAPI
        .getFronts()
        .asFuture
        .futureValue
        .fold(
          err => fail(s"expected fronts, got error $err", err.cause),
          fronts => fronts.size should be > 0
        )
    }
  }

  "frontsForPath" - {
    "should return the front for the given path" in {
      FAPI
        .frontForPath("uk")
        .asFuture
        .futureValue
        .fold(
          err => fail(s"expected front, got error $err", err.cause),
          front => front.id should equal("uk")
        )
    }
  }

  "getCollection" - {
    "should return the collection with a given id" in {
      FAPI
        .getCollection("e394b4df-85b2-4e94-aa9d-7ecf3baddee4")
        .asFuture
        .futureValue
        .fold(
          err => fail(s"expected collection, got $err", err.cause),
          collection =>
            collection should have(
              Symbol("id")("e394b4df-85b2-4e94-aa9d-7ecf3baddee4")
            )
        )
    }
  }

  "frontCollections" - {
    "should return collections for the given front" ignore {
      FAPI
        .frontCollections("uk")
        .asFuture
        .futureValue
        .fold(
          err => fail(s"expected collection, got $err", err.cause),
          collections => collections.size should be > 0
        )
    }
  }

  "collectionContent" - {

    val collectionResponse: Either[ApiError, Collection] = FAPI
      .getCollection("e394b4df-85b2-4e94-aa9d-7ecf3baddee4")
      .asFuture
      .futureValue

    "fetch collection" in {
      collectionResponse.fold(
        err => fail(s"expected collection, got $err", err.cause),
        collection => collection
      )
    }

    "should return the curated content for the collection" ignore {
      val collection = collectionResponse.toOption.get
      FAPI
        .liveCollectionContentWithSnaps(collection)
        .asFuture
        .futureValue
        .fold(
          err => fail(s"expected collection, got $err", err.cause),
          curatedContent => curatedContent.size should be > 0
        )
    }

    "will use the provided function to adjust the query used to hydrate content" ignore {
      val adjust: AdjustSearchQuery = q => q.showTags("tone")
      val collection = collectionResponse.toOption.get
      FAPI
        .liveCollectionContentWithSnaps(collection, adjust)
        .asFuture
        .futureValue
        .fold(
          err => fail(s"expected collection, got $err", err.cause),
          curatedContent =>
            curatedContent
              .flatMap {
                case c: CuratedContent => Some(c)
                case _                 => None
              }
              .head
              .content
              .tags
              .exists(_.`type` == TagType.Tone) should equal(true)
        )
    }

    "for snaps" - {
      val latestSnapOne = makeLatestTrailFor("snap/1281727", "uk/culture")
      val latestSnapTwo = makeLatestTrailFor("snap/2372382", "technology")

      val plainSnapOne = makeLinkSnapFor("snap/347234723", "doesnotmatter")

      val normalTrail = Trail("internal-code/page/2144828", 0, None, None, None)

      val collectionConfig =
        CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())

      "should turn latest snaps into content" ignore {
        val collectionJson = makeCollectionJson(latestSnapOne, latestSnapTwo)
        val collection = Collection.fromCollectionJsonConfigAndContent(
          "id",
          Some(collectionJson),
          collectionConfig
        )
        val faciaContent = FAPI.liveCollectionContentWithSnaps(collection)

        faciaContent.asFuture.futureValue.fold(
          err => fail(s"expected to get two latest snaps, got $err", err.cause),
          { listOfFaciaContent =>
            val latestSnaps = listOfFaciaContent.collect { case ls: LatestSnap =>
              ls
            }
            latestSnaps.length should be(2)
            latestSnaps(0).latestContent
              .fold(fail("latest snap 0 content was empty")) { c =>
                c.tags.exists(_.id.contains("culture"))
              }
            latestSnaps(1).latestContent
              .fold(fail("latest snap 1 content was empty")) { c =>
                c.tags.exists(_.id.contains("technology"))
              }
          }
        )
      }

      "work with normal content and link snaps" ignore {
        val collectionJson = makeCollectionJson(
          latestSnapOne,
          normalTrail,
          plainSnapOne,
          latestSnapTwo
        )
        val collection = Collection.fromCollectionJsonConfigAndContent(
          "id",
          Some(collectionJson),
          collectionConfig
        )
        val faciaContent = FAPI.liveCollectionContentWithSnaps(collection)

        faciaContent.asFuture.futureValue.fold(
          err => fail(s"expected to get three items, got $err", err.cause),
          { listOfFaciaContent =>
            listOfFaciaContent.length should be(4)

            val normalContent = listOfFaciaContent.collect { case cc: CuratedContent =>
              cc
            }
            normalContent.length should be(1)
            normalContent.apply(0).headline should be(
              "PM returns from holiday after video shows US reporter beheaded by Briton"
            )

            val latestSnapContent = listOfFaciaContent.collect { case ls: LatestSnap =>
              ls
            }
            latestSnapContent.length should be(2)
            latestSnapContent.forall(_.latestContent.isEmpty) should be(false)

            val linkSnaps = listOfFaciaContent.collect { case ls: LinkSnap =>
              ls
            }
            linkSnaps.length should be(1)
            linkSnaps(0).id should be("snap/347234723")
            linkSnaps(0).snapUri should be(Some("doesnotmatter"))

          }
        )
      }
      "not request latest snaps in" ignore {
        val collectionJson =
          makeCollectionJson(latestSnapOne, normalTrail, latestSnapTwo)
        val collection = Collection.fromCollectionJsonConfigAndContent(
          "id",
          Some(collectionJson),
          collectionConfig
        )
        val faciaContent = FAPI.liveCollectionContentWithoutSnaps(collection)

        faciaContent.asFuture.futureValue.fold(
          err => fail(s"expected to get three items, got $err", err.cause),
          { listOfFaciaContent =>
            listOfFaciaContent.length should be(1)

            val normalContent = listOfFaciaContent.collect { case cc: CuratedContent =>
              cc
            }
            normalContent.apply(0).headline should be(
              "PM returns from holiday after video shows US reporter beheaded by Briton"
            )
          }
        )
      }

    }
  }

  "backfill - capi query in backfill object" - {
    val collection = Collection(
      "uk/business/regular-stories",
      "economy",
      None,
      Nil,
      None,
      Nil,
      Some(new DateTime(1)),
      Some("updatedBy"),
      Some("updatedBy@example.com"),
      CollectionConfig.empty.copy(backfill = Some(Backfill(`type` = "capi", query = "business?edition=uk"))),
      None
    )

    "can get the backfill for a collection" ignore {
      FAPI
        .backfillFromConfig(collection.collectionConfig)
        .asFuture
        .futureValue
        .fold(
          err => fail(s"expected backfill results, got $err", err.cause),
          backfillContents => backfillContents.size should be > 0
        )
    }

    "collection metadata is resolved on backfill content" ignore {
      FAPI
        .backfillFromConfig(
          collection.collectionConfig.copy(showSections = true)
        )
        .asFuture
        .futureValue
        .fold(
          err => fail(s"expected backfill results, got $err", err.cause),
          backfillContents =>
            backfillContents.head
              .asInstanceOf[CuratedContent]
              .kicker
              .value shouldBe a[SectionKicker]
        )
    }

    "item query can be adjusted" ignore {
      val adjust: AdjustItemQuery = q => q.showTags("all")
      FAPI
        .backfillFromConfig(
          collection.collectionConfig.copy(showSections = true),
          adjustItemQuery = adjust
        )
        .asFuture
        .futureValue
        .fold(
          err => fail(s"expected backfill results, got $err", err.cause),
          backfillContents =>
            backfillContents.head
              .asInstanceOf[CuratedContent]
              .content
              .tags
              .exists(_.id.contains("business")) should equal(true)
        )
    }

    "search query can be adjusted" ignore {
      val testCollection = collection.copy(collectionConfig = CollectionConfig.empty.copy(
        backfill = Some(
          Backfill(
            `type` = "capi",
            query = "search?tag=sustainable-business/series/finance&use-date=published"
          )
        ),
        showSections = true
      ))
      val adjust: AdjustSearchQuery = q => q.showTags("series")
      FAPI
        .backfillFromConfig(
          testCollection.collectionConfig,
          adjustSearchQuery = adjust
        )
        .asFuture
        .futureValue
        .fold(
          err => fail(s"expected backfill results, got $err", err.cause),
          backfillContents =>
            backfillContents.head
              .asInstanceOf[CuratedContent]
              .content
              .tags
              .exists(
                _.id.contains("sustainable-business/series/finance")
              ) should equal(true)
        )
    }
  }

  "backfill - empty" - {
    val collection = Collection(
      "uk/business/regular-stories",
      "economy",
      None,
      Nil,
      None,
      Nil,
      Some(new DateTime(1)),
      Some("updatedBy"),
      Some("updatedBy@example.com"),
      CollectionConfig.empty,
      None
    )

    "returns empty list" in {
      FAPI
        .backfillFromConfig(collection.collectionConfig)
        .asFuture
        .futureValue
        .fold(
          err => fail(s"expected backfill results, got $err", err.cause),
          backfillContents => backfillContents.size should be(0)
        )
    }
  }

  "backfill - from collection" - {
    val collection = Collection(
      "us/business",
      "economy",
      None,
      Nil,
      None,
      Nil,
      Some(new DateTime(1)),
      Some("updatedBy"),
      Some("updatedBy@example.com"),
      CollectionConfig.empty,
      None
    )

    "inheriting from a non existing collection" in {
      val child = collection.copy(
        collectionConfig = CollectionConfig.empty.copy(
          backfill = Some(
            Backfill(
              `type` = "collection",
              query = "this-collection-id-does-not-exist"
            )
          )
        )
      )

      FAPI
        .backfillFromConfig(child.collectionConfig)
        .asFuture
        .futureValue
        .fold(
          err =>
            err.message should equal(
              "Collection config not found for this-collection-id-does-not-exist"
            ),
          backfillContents => backfillContents.size should be(0)
        )
    }

    "inheriting from an empty collection" in {
      val child = collection.copy(
        collectionConfig = CollectionConfig.empty.copy(
          backfill = Some(
            Backfill(
              `type` = "collection",
              query = "15ce06c4-c082-4b3b-9971-88cc0ad15c87"
            )
          )
        )
      )

      FAPI
        .backfillFromConfig(child.collectionConfig)
        .asFuture
        .futureValue
        .fold(
          err => fail(s"expected backfill results, got $err", err.cause),
          backfillContents => backfillContents.size should be(0)
        )
    }

    "inheriting from a valid collection" ignore {
      val child = collection.copy(
        collectionConfig = CollectionConfig.empty.copy(
          backfill = Some(
            Backfill(
              `type` = "collection",
              query = "db6b8256-f9d1-43d1-bf3f-1c245b094e57"
            )
          )
        )
      )

      FAPI
        .backfillFromConfig(child.collectionConfig)
        .asFuture
        .futureValue
        .fold(
          err => fail(s"expected backfill results, got $err", err.cause),
          backfillContents => {
            backfillContents.size should be(2)
            backfillContents(0)
              .asInstanceOf[CuratedContent]
              .headline should equal("Life in North Korea – the early years")
            backfillContents(1)
              .asInstanceOf[CuratedContent]
              .headline should equal(
              "Strictly Come Dancing 2015 – Custom headline"
            )
          }
        )
    }

    "inheriting from a valid collection but don't ignore parent backfill if capi" ignore {
      val child = collection.copy(
        collectionConfig = CollectionConfig.empty.copy(
          backfill = Some(
            Backfill(
              `type` = "collection",
              query = "15d5ef63-dc8c-4ad0-a89e-bb414449cddc"
            )
          )
        )
      )

      FAPI
        .backfillFromConfig(child.collectionConfig)
        .asFuture
        .futureValue
        .fold(
          err => fail(s"expected backfill results, got $err", err.cause),
          backfillContents => {
            backfillContents.size should be > 1
            backfillContents.head
              .asInstanceOf[CuratedContent]
              .headline should equal(
              "Italy’s naked statues and other great diplomatic cover-ups"
            )
          }
        )
    }
  }

  "backfill - invalid configuration" - {
    val collection = Collection(
      "uk/business/regular-stories",
      "economy",
      None,
      Nil,
      None,
      Nil,
      Some(new DateTime(1)),
      Some("updatedBy"),
      Some("updatedBy@example.com"),
      CollectionConfig.empty.copy(
        backfill = Some(
          Backfill(
            `type` = "invalid-backfill-configuration",
            query = "any"
          )
        )
      ),
      None
    )

    "returns an empty list" in {
      FAPI
        .backfillFromConfig(collection.collectionConfig)
        .asFuture
        .futureValue
        .fold(
          err => fail(s"expected backfill results, got $err", err.cause),
          backfillContents => backfillContents.size should be(0)
        )
    }
  }

  "Supporting Items" - {
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

    val supportingTrailOne = makeTrail("internal-code/page/2144828")
    val supportingTrailTwo = makeTrail("internal-code/page/2383149")

    val trailWithSupporting = makeTrailWithSupporting(
      "internal-code/page/2382509",
      supportingTrailOne,
      supportingTrailTwo
    )

    val collectionConfig =
      CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())
    val collectionJson = makeCollectionJson(trailWithSupporting)

    val latestSnapOne = makeLatestTrailFor("snap/1281727", "uk/culture")
    val latestSnapTwo = makeLatestTrailFor("snap/2372382", "technology")
    val plainSnapOne = makeLinkSnapFor("snap/347234723", "doesnotmatter")
    val trailWithSupportingAndLatestSnaps = makeTrailWithSupporting(
      "internal-code/page/2381864",
      supportingTrailOne,
      latestSnapOne,
      latestSnapTwo,
      supportingTrailTwo,
      plainSnapOne
    )
    val collectionJsonSupportingWithLatestSnaps =
      makeCollectionJson(trailWithSupportingAndLatestSnaps)

    "should be filled correctly" ignore {
      val collection = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Option(collectionJson),
        collectionConfig
      )
      val faciaContent = FAPI.liveCollectionContentWithoutSnaps(collection)

      faciaContent.asFuture.futureValue.fold(
        err =>
          fail(
            s"expected to get one item with supporting, got $err",
            err.cause
          ),
        { listOfFaciaContent =>
          listOfFaciaContent.length should be(1)

          listOfFaciaContent.apply(0) match {
            case c: CuratedContent =>
              c.supportingContent.length should be(2)
            case somethingElse =>
              fail(
                s"expected only CuratedContent, got ${somethingElse.getClass.getName}"
              )
          }
        }
      )
    }

    "should not fill in latest snaps in supporting items" ignore {
      val collection = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Option(collectionJsonSupportingWithLatestSnaps),
        collectionConfig
      )
      val faciaContent = FAPI.liveCollectionContentWithoutSnaps(collection)

      faciaContent.asFuture.futureValue.fold(
        err =>
          fail(
            s"expected to get one item with supporting and latest snaps, got $err",
            err.cause
          ),
        { listOfFaciaContent =>
          listOfFaciaContent.length should be(1)

          listOfFaciaContent.apply(0) match {
            case c: CuratedContent =>
              c.supportingContent.length should be(5)
              c.supportingContent.collect { case s: SupportingCuratedContent =>
                s
              }.length should be(2)
              val latestSnaps = c.supportingContent.collect { case l: LatestSnap =>
                l
              }
              latestSnaps.length should be(2)
              latestSnaps.forall(_.latestContent.isDefined == false) should be(
                true
              )
              c.supportingContent.collect { case l: LinkSnap =>
                l
              }.length should be(1)
            case somethingElse =>
              fail(
                s"expected only CuratedContent, got ${somethingElse.getClass.getName}"
              )
          }
        }
      )
    }

    "should fill in latest snaps in supporting items" ignore {
      val collection = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Option(collectionJsonSupportingWithLatestSnaps),
        collectionConfig
      )
      val faciaContent = FAPI.liveCollectionContentWithSnaps(collection)

      faciaContent.asFuture.futureValue.fold(
        err =>
          fail(
            s"expected to get one item with supporting and latest snaps, got $err",
            err.cause
          ),
        { listOfFaciaContent =>
          listOfFaciaContent.length should be(1)

          listOfFaciaContent.apply(0) match {
            case c: CuratedContent =>
              c.supportingContent.length should be(5)
              val supportingContent = c.supportingContent.collect { case s: SupportingCuratedContent =>
                s
              }
              supportingContent.length should be(2)
              val latestSnaps = c.supportingContent.collect { case l: LatestSnap =>
                l
              }
              latestSnaps.length should be(2)
              latestSnaps.forall(_.latestContent.isDefined) should be(true)
              val linkSnaps = c.supportingContent.collect { case l: LinkSnap =>
                l
              }
              linkSnaps.length should be(1)

              c.supportingContent(0)
                .asInstanceOf[SupportingCuratedContent]
                .headline should be(
                "PM returns from holiday after video shows US reporter beheaded by Briton"
              )
              c.supportingContent(1)
                .asInstanceOf[LatestSnap]
                .latestContent
                .get
                .sectionId should be(Some("culture"))
              c.supportingContent(2)
                .asInstanceOf[LatestSnap]
                .latestContent
                .get
                .sectionId should be(Some("technology"))
              c.supportingContent(3)
                .asInstanceOf[SupportingCuratedContent]
                .headline should be(
                "Abbey to offer unique new perspective on Battle of Hastings"
              )
              c.supportingContent(4).asInstanceOf[LinkSnap].id should be(
                "snap/347234723"
              )

            case somethingElse =>
              fail(
                s"expected only CuratedContent, got ${somethingElse.getClass.getName}"
              )
          }
        }
      )
    }
  }

  "Treats" - {
    def makeCollectionJsonWithTreats(treats: Trail*) = CollectionJson(
      live = Nil,
      draft = None,
      treats = Option(treats.toList),
      lastUpdated = new DateTime(1),
      updatedBy = "test",
      updatedEmail = "test@example.com",
      displayName = Some("displayName"),
      href = Some("href"),
      previously = None,
      targetedTerritory = None
    )

    val normalTrail = Trail("internal-code/page/2144828", 0, None, None, None)
    val normalTrailTwo =
      Trail("internal-code/page/2383149", 0, None, None, None)
    val collectionConfig =
      CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())

    "should request normal item treats" ignore {
      val collectionJson =
        makeCollectionJsonWithTreats(normalTrail, normalTrailTwo)
      val collection = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Some(collectionJson),
        collectionConfig
      )
      val faciaContent = FAPI.getTreatsForCollection(collection)

      faciaContent.asFuture.futureValue.fold(
        err => fail(s"expected 2 treat result, got $err", err.cause),
        treatsContents => {
          treatsContents.size should be(2)
          treatsContents.head.asInstanceOf[CuratedContent].headline should be(
            "PM returns from holiday after video shows US reporter beheaded by Briton"
          )
          treatsContents
            .apply(1)
            .asInstanceOf[CuratedContent]
            .headline should be(
            "Abbey to offer unique new perspective on Battle of Hastings"
          )
        }
      )
    }

    "should request latest snap treats" ignore {
      val latestSnapOne = makeLatestTrailFor("snap/1281727", "uk/culture")
      val latestSnapTwo = makeLatestTrailFor("snap/2372382", "technology")
      val collectionJson =
        makeCollectionJsonWithTreats(latestSnapOne, latestSnapTwo)
      val collection = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Some(collectionJson),
        collectionConfig
      )
      val faciaContent = FAPI.getTreatsForCollection(
        collection,
        adjustItemQuery = itemQuery => itemQuery.showTags("all")
      )

      faciaContent.asFuture.futureValue.fold(
        err => fail(s"expected 2 treat result, got $err", err.cause),
        treatsContents => {
          treatsContents.size should be(2)
          treatsContents.head
            .asInstanceOf[LatestSnap]
            .latestContent
            .get
            .tags
            .exists(_.sectionId == Some("culture")) should be(true)
          treatsContents
            .apply(1)
            .asInstanceOf[LatestSnap]
            .latestContent
            .get
            .tags
            .exists(_.sectionId == Some("technology")) should be(true)
        }
      )
    }

    "Should request a mix of both" ignore {
      val latestSnapOne = makeLatestTrailFor("snap/1281727", "uk/culture")
      val latestSnapTwo = makeLatestTrailFor("snap/2372382", "technology")
      val collectionJson = makeCollectionJsonWithTreats(
        latestSnapOne,
        normalTrail,
        latestSnapTwo,
        normalTrailTwo
      )
      val collection = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Some(collectionJson),
        collectionConfig
      )
      val faciaContent = FAPI.getTreatsForCollection(
        collection,
        adjustItemQuery = itemQuery => itemQuery.showTags("all")
      )

      faciaContent.asFuture.futureValue.fold(
        err => fail(s"expected 2 treat result, got $err", err.cause),
        treatsContents => {
          treatsContents.size should be(4)
          treatsContents.head
            .asInstanceOf[LatestSnap]
            .latestContent
            .get
            .tags
            .exists(_.sectionId == Some("culture")) should be(true)
          treatsContents
            .apply(1)
            .asInstanceOf[CuratedContent]
            .headline should be(
            "PM returns from holiday after video shows US reporter beheaded by Briton"
          )
          treatsContents
            .apply(2)
            .asInstanceOf[LatestSnap]
            .latestContent
            .get
            .tags
            .exists(_.sectionId == Some("technology")) should be(true)
          treatsContents
            .apply(3)
            .asInstanceOf[CuratedContent]
            .headline should be(
            "Abbey to offer unique new perspective on Battle of Hastings"
          )

        }
      )

    }
  }

  "Draft" - {
    def makeCollectionJsonWithDraft(draft: List[Trail], live: Trail*) =
      CollectionJson(
        live = live.toList,
        draft = Option(draft),
        treats = None,
        lastUpdated = new DateTime(1),
        updatedBy = "test",
        updatedEmail = "test@example.com",
        displayName = Some("displayName"),
        href = Some("href"),
        previously = None,
        targetedTerritory = None
      )

    val normalTrail = Trail("internal-code/page/2144828", 0, None, None, None)
    val normalTrailTwo =
      Trail("internal-code/page/2383149", 0, None, None, None)
    val collectionConfig =
      CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())

    "should request draft items" ignore {
      val collectionJson =
        makeCollectionJsonWithDraft(List(normalTrail, normalTrailTwo))
      val collection = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Some(collectionJson),
        collectionConfig
      )
      val faciaContent = FAPI.draftCollectionContentWithoutSnaps(collection)

      faciaContent.asFuture.futureValue.fold(
        err => fail(s"expected 2 results, got $err", err.cause),
        contents => {
          contents.size should be(2)
          contents.head.asInstanceOf[CuratedContent].headline should be(
            "PM returns from holiday after video shows US reporter beheaded by Briton"
          )
          contents.apply(1).asInstanceOf[CuratedContent].headline should be(
            "Abbey to offer unique new perspective on Battle of Hastings"
          )
        }
      )
    }

    "should return nothing" in {
      val collectionJson =
        makeCollectionJsonWithDraft(List(normalTrail, normalTrailTwo))
      val collection = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Some(collectionJson),
        collectionConfig
      )
      val faciaContent = FAPI.liveCollectionContentWithoutSnaps(collection)

      faciaContent.asFuture.futureValue.fold(
        err => fail(s"expected 0 results, got $err", err.cause),
        contents => {
          contents.size should be(0)
        }
      )
    }

    "should request latest snaps in draft" ignore {
      val latestSnapOne = makeLatestTrailFor("snap/1281727", "uk/culture")
      val latestSnapTwo = makeLatestTrailFor("snap/2372382", "technology")
      val collectionJson =
        makeCollectionJsonWithDraft(List(latestSnapOne, latestSnapTwo))
      val collection = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Some(collectionJson),
        collectionConfig
      )
      val faciaContent = FAPI.draftCollectionContentWithSnaps(
        collection,
        adjustSnapItemQuery = itemQuery => itemQuery.showTags("all")
      )

      faciaContent.asFuture.futureValue.fold(
        err => fail(s"expected 2 results, got $err", err.cause),
        contents => {
          contents.size should be(2)
          contents.head
            .asInstanceOf[LatestSnap]
            .latestContent
            .get
            .tags
            .exists(_.sectionId == Some("culture")) should be(true)
          contents
            .apply(1)
            .asInstanceOf[LatestSnap]
            .latestContent
            .get
            .tags
            .exists(_.sectionId == Some("technology")) should be(true)
        }
      )
    }

    "should request what is in live when draft is None" - {
      "for normal content" ignore {
        val collectionJson = makeCollectionJson(normalTrail, normalTrailTwo)
        val collection = Collection.fromCollectionJsonConfigAndContent(
          "id",
          Some(collectionJson),
          collectionConfig
        )

        val faciaContent = FAPI.draftCollectionContentWithoutSnaps(collection)

        faciaContent.asFuture.futureValue.fold(
          err => fail(s"expected 2 results, got $err", err.cause),
          contents => {
            contents.size should be(2)
            contents.head.asInstanceOf[CuratedContent].headline should be(
              "PM returns from holiday after video shows US reporter beheaded by Briton"
            )
            contents.apply(1).asInstanceOf[CuratedContent].headline should be(
              "Abbey to offer unique new perspective on Battle of Hastings"
            )
          }
        )
      }

      "for latest snaps" ignore {
        val latestSnapOne = makeLatestTrailFor("snap/1281727", "uk/culture")
        val latestSnapTwo = makeLatestTrailFor("snap/2372382", "technology")
        val collectionJson = makeCollectionJson(
          normalTrail,
          normalTrailTwo,
          latestSnapOne,
          latestSnapTwo
        )
        val collection = Collection.fromCollectionJsonConfigAndContent(
          "id",
          Some(collectionJson),
          collectionConfig
        )

        val faciaContent = FAPI.draftCollectionContentWithSnaps(
          collection,
          adjustSnapItemQuery = itemQuery => itemQuery.showTags("all")
        )

        faciaContent.asFuture.futureValue.fold(
          err => fail(s"expected 4 results, got $err", err.cause),
          contents => {
            contents.size should be(4)
            contents.head.asInstanceOf[CuratedContent].headline should be(
              "PM returns from holiday after video shows US reporter beheaded by Briton"
            )
            contents.apply(1).asInstanceOf[CuratedContent].headline should be(
              "Abbey to offer unique new perspective on Battle of Hastings"
            )
            contents
              .apply(2)
              .asInstanceOf[LatestSnap]
              .latestContent
              .get
              .tags
              .exists(_.sectionId == Some("culture")) should be(true)
            contents
              .apply(3)
              .asInstanceOf[LatestSnap]
              .latestContent
              .get
              .tags
              .exists(_.sectionId == Some("technology")) should be(true)
          }
        )
      }
    }

    "Should request a mix of both" - {
      val normalTrailThree =
        Trail("internal-code/page/2379309", 0, None, None, None)
      val latestSnapOne = makeLatestTrailFor("snap/1281727", "uk/culture")
      val latestSnapTwo = makeLatestTrailFor("snap/2372382", "technology")
      val collectionJson = makeCollectionJsonWithDraft(
        List(latestSnapOne, normalTrail, latestSnapTwo, normalTrailTwo),
        normalTrailThree
      )
      val collection = Collection.fromCollectionJsonConfigAndContent(
        "id",
        Some(collectionJson),
        collectionConfig
      )

      "for draft" ignore {
        val faciaContent = FAPI.draftCollectionContentWithSnaps(
          collection,
          adjustSnapItemQuery = itemQuery => itemQuery.showTags("all")
        )
        faciaContent.asFuture.futureValue.fold(
          err => fail(s"expected 4 results, got $err", err.cause),
          contents => {
            contents.size should be(4)
            contents.head
              .asInstanceOf[LatestSnap]
              .latestContent
              .get
              .tags
              .exists(_.sectionId == Some("culture")) should be(true)
            contents.apply(1).asInstanceOf[CuratedContent].headline should be(
              "PM returns from holiday after video shows US reporter beheaded by Briton"
            )
            contents
              .apply(2)
              .asInstanceOf[LatestSnap]
              .latestContent
              .get
              .tags
              .exists(_.sectionId == Some("technology")) should be(true)
            contents.apply(3).asInstanceOf[CuratedContent].headline should be(
              "Abbey to offer unique new perspective on Battle of Hastings"
            )
          }
        )
      }

      "for live" ignore {
        val faciaContentLive = FAPI.liveCollectionContentWithoutSnaps(
          collection,
          adjustSearchQuery = searchQuery => searchQuery.showTags("all")
        )
        faciaContentLive.asFuture.futureValue.fold(
          err => fail(s"expected 1 results, got $err", err.cause),
          contents => {
            contents.size should be(1)
            contents.head.asInstanceOf[CuratedContent].headline should be(
              "Simone Lia on making new friends"
            )
          }
        )
      }
    }
  }

  def fail(message: String, cause: Option[Throwable]): Nothing = {
    cause.map(fail(message, _)).getOrElse(fail(message))
  }
}
