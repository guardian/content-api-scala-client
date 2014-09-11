package com.gu.contentapi.client.connection

import scala.concurrent.ExecutionContext
import com.gu.contentapi.client.model.{ItemResponse, Content}
import com.gu.contentapi.client.{Api, ApiError}
import com.gu.contentapi.client.connection._
import org.scalatest.{Matchers, FlatSpec, BeforeAndAfterEach}
import dispatch._

class HttpTest extends FlatSpec with Matchers with BeforeAndAfterEach {
  import ExecutionContext.Implicits.global

  info("Tests that each type of client can actually perform a call to the api")

  "Api" should "be able to call the api" in {

    val promisedContent: Future[Content] = for {
      response <- Api.item.itemId("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry").response
    } yield response.content.get

    promisedContent.apply().id should be ("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry")
  }

  "DispatchAsyncHttp" should "return API errors as a broken promise" in {
    val brokenPromise: Future[ItemResponse] = Api.item.itemId("fdsfgs").response
    brokenPromise.recover { case error => error should be (ApiError(404, "Not Found")) } apply()
  }

  override protected def beforeEach() {
    // avoid upsetting rate limit of free tier,
    Thread.sleep(500)
  }
}
