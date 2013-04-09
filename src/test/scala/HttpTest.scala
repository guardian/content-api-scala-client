import com.gu.openplatform.contentapi.model.{ItemResponse, Content}
import com.gu.openplatform.contentapi.{ApiError, SyncApi, Api}
import com.gu.openplatform.contentapi.connection._
import concurrent.ExecutionContext
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterEach}
import com.gu.openplatform.contentapi.util.IdInstances._
import com.gu.openplatform.contentapi.util.DispatchPromiseInstances._
import dispatch._


class HttpTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  import ExecutionContext.Implicits.global

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

  val syncApi = new SyncApi with DispatchSyncHttp {
    implicit val executionContext = ExecutionContext.global
  }

  "DispatchSyncHttp" should "be able to call the api" in {
    testClient(syncApi)
  }

  "DispatchSyncHttp" should "follow redirects" in {
    //redirects to /video
    syncApi.item.itemId("type/video").response.tag.get.id should be("type/video")
  }

  "DispatchAsyncHttp" should "be able to call the api" in {

    val promisedContent: dispatch.Promise[Content] = for {
      response <- DispatchAsyncApi.item.itemId("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry").response
    } yield response.content.get

    promisedContent.apply().id should be ("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry")
  }

  "DispatchAsyncHttp" should "return API errors as a broken promise" in {
    val brokenPromise: dispatch.Promise[ItemResponse] = DispatchAsyncApi.item.itemId("fdsfgs").response
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

  object DispatchAsyncApi extends Api[dispatch.Promise] with DispatchAsyncHttp {
    val executionContext = ExecutionContext.global
  }

}
