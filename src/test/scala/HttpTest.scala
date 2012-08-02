import com.gu.openplatform.contentapi.Api
import com.gu.openplatform.contentapi.connection.{DispatchGzipHttp, JavaNetHttp, MultiThreadedApacheHttpClient, ApacheHttpClient}
import org.joda.time.DateMidnight
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterEach, FeatureSpec}

class HttpTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  info("Tests that each type of client can actually perform a call to the api")

  "ApacheHttpClient" should "be able to call the api" in {
    testClient(new Api with ApacheHttpClient)
  }

  "MultiThreadedApacheHttpClient" should "be able to call the api" in {
    testClient(new Api with MultiThreadedApacheHttpClient)
  }

  "JavaNetHttp" should "be able to call the api" in {
    testClient(new Api with JavaNetHttp)
  }

  "DispatchGzipHttp" should "be able to call the api" in {
    testClient(new Api with DispatchGzipHttp)
  }

  override protected def beforeEach() {
    // avoid upsetting rate limit of free tier,
    Thread.sleep(500)
  }

  private def testClient(api: Api) {
    val content = api.item.itemId("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry").response
      .content.get

    content.id should be ("commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry")
  }

}