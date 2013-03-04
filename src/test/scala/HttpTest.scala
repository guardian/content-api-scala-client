import com.gu.openplatform.contentapi.model.{ItemResponse, Content}
import com.gu.openplatform.contentapi.{ApiError, SyncApi, Api}
import com.gu.openplatform.contentapi.connection._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterEach}


class HttpTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  info("Tests that each type of client can actually perform a call to the api")

  "ApacheSyncHttpClient" should "be able to call the api" in {
    testClient(new SyncApi with ApacheSyncHttpClient)
  }

  "MultiThreadedApacheSyncHttpClient" should "be able to call the api" in {
    testClient(new SyncApi with MultiThreadedApacheSyncHttpClient)
  }

  "JavaNetSyncHttp" should "be able to call the api" in {
    testClient(new SyncApi with JavaNetSyncHttp)
  }

  "DispatchSyncHttp" should "be able to call the api" in {
    val api = new SyncApi with DispatchSyncHttp
    testClient(api)
  }

  "DispatchSyncHttp" should "follow redirects" in {
    val api = new SyncApi with DispatchSyncHttp

    //redirects to /video
    api.item.itemId("type/video").response.tag.get.id should be("type/video")
  }

  import com.gu.openplatform.contentapi.util.DispatchPromiseInstances._

  "DispatchAsyncHttp" should "be able to call the api" in {
    val api = new Api[dispatch.Promise] with DispatchAsyncHttp

    val promisedContent: dispatch.Promise[Content] = for {
      response <- api.item.itemId("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry").response
    } yield response.content.get

    promisedContent.apply.id should be ("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry")
  }

  "DispatchAsyncHttp" should "return API errors as a broken promise" in {
    val api = new Api[dispatch.Promise] with DispatchAsyncHttp
    val brokenPromise: dispatch.Promise[ItemResponse] = api.item.itemId("fdsfgs").response
    brokenPromise.recover { case error => error should be (ApiError(404, "Not Found")) } apply()
  }

  override protected def beforeEach() {
    // avoid upsetting rate limit of free tier,
    Thread.sleep(500)
  }

  private def testClient(api: SyncApi) {
    val content = api.item.itemId("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry").response
      .content.get

    content.id should be ("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry")
  }

}
