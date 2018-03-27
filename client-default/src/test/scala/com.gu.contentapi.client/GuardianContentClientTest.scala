package com.gu.contentapi.client

import com.gu.contentatom.thrift.{AtomData, AtomType}
import com.gu.contentapi.client.model.v1.{ContentType, ErrorResponse}
import com.gu.contentapi.client.model.{ItemQuery, SearchQuery}
import java.time.Instant
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Inside, Matchers, OptionValues}

import scala.concurrent.ExecutionContext.Implicits.global

object GuardianContentClientTest {
  private final val ApiKeyProperty = "CAPI_TEST_KEY"
  private val apiKey: String = {
    Option(System.getProperty(ApiKeyProperty)) orElse Option(System.getenv(ApiKeyProperty))
  }.orNull ensuring(_ != null, s"Please supply a $ApiKeyProperty as a system property or an environment variable")
}

class GuardianContentClientTest extends FlatSpec with Matchers with ScalaFutures with OptionValues with BeforeAndAfterAll with Inside {
  import GuardianContentClientTest.apiKey
  private val api = new GuardianContentClient(apiKey)
  private val TestItemPath = "commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry"

  override def afterAll() {
    api.shutdown()
  }

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  "client interface" should "successfully call the Content API" in {
    val query = ItemQuery(TestItemPath)
    val content = for {
      response <- api.getResponse(query)
    } yield response.content.get
    content.futureValue.id should be (TestItemPath)
  }

  it should "return errors as a broken promise" in {
    val query = ItemQuery("something-that-does-not-exist")
    val errorTest = api.getResponse(query) recover { case error =>
      error should be (GuardianContentApiError(404, "Not Found", Some(ErrorResponse("error", "The requested resource could not be found."))))
    }
    errorTest.futureValue
  }

  it should "handle error responses" in {
    val query = SearchQuery().pageSize(500)
    val errorTest = api.getResponse(query) recover { case error =>
      error should be (GuardianContentApiError(400, "Bad Request", Some(ErrorResponse("error", "page-size must be an integer between 0 and 200"))))
    }
    errorTest.futureValue
  }

  it should "perform a given item query" in {
    val query = ItemQuery(TestItemPath)
    val content = for (response <- api.getResponse(query)) yield response.content.get
    content.futureValue.id should be (TestItemPath)
  }

  it should "perform a given removed content query" in {
    val query = api.removedContent.reason("expired")
    val results = for (response <- api.getResponse(query)) yield response.results
    val fResults = results.futureValue
    fResults.size should be (10)
  }

  it should "perform a given atoms query" in {
    val query = api.atoms.types("explainer")
    val results = for (response <- api.getResponse(query)) yield response.results
    val fResults = results.futureValue
    fResults.size should be (10)
  }

  it should "perform a given recipes query" in {
    val query = api.recipes.cuisines("thai")
    val results = for (response <- api.getResponse(query)) yield response.results
    val fResults = results.futureValue
    fResults.size should be (10)
  }

  it should "perform a given reviews query" in {
    val query = api.reviews.minRating(3)
    val results = for (response <- api.getResponse(query)) yield response.results
    val fResults = results.futureValue
    fResults.size should be (10)
  }

  it should "perform a given game review query" in {
    val query = api.gameReviews.minRating(3)
    val results = for (response <- api.getResponse(query)) yield response.results
    val fResults = results.futureValue
    fResults.size should be (10)
  }

  it should "perform a given film review query" in {
    val query = api.filmReviews.maxRating(4)
    val results = for (response <- api.getResponse(query)) yield response.results
    val fResults = results.futureValue
    fResults.size should be (10)
  }

  it should "perform a given restaurant review query" in {
    val query = api.restaurantReviews.minRating(1)
    val results = for (response <- api.getResponse(query)) yield response.results
    val fResults = results.futureValue
    fResults.size should be (10)
  }

  it should "perform a given search query using the type filter" in {
    val query = api.search.contentType("article")
    val results = for (response <- api.getResponse(query)) yield response.results
    val fResults = results.futureValue
    fResults.size should be (10)
    fResults.foreach(_.`type` should be(ContentType.Article))
  }

  it should "perform an atom query" in {
    val query = ItemQuery("/atom/quiz/3c244199-01a8-4836-a638-daabf9aca341")
    val quiz = for (response <- api.getResponse(query)) yield response.quiz.get
    val fQuiz = quiz.futureValue
    fQuiz.atomType should be (AtomType.Quiz)
    fQuiz.id should be ("3c244199-01a8-4836-a638-daabf9aca341")
    inside(fQuiz.data) {
      case AtomData.Quiz(data) =>
        data.title should be ("Andy Burnham quiz")
        data.content.questions should have size 10
    }
  }

}
