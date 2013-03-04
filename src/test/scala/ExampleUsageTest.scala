import com.gu.openplatform.contentapi.Api
import java.io.IOException
import org.joda.time.DateMidnight
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FeatureSpec}

class ExampleUsageTest extends FeatureSpec with ShouldMatchers with BeforeAndAfterEach {

  override protected def beforeEach() {
    // if you run all these tests they will exceed the rate limit in the free tier,
    // so putting in a cheeky sleep
    Thread.sleep(500)
  }


  feature("Pagination:") {

    scenario("get the most recent 10 items") {

      val latest10Items = Api.search.response

      latest10Items.pageSize should be (10)
      latest10Items.results.foreach ( item => println(item.webTitle))
    }

    scenario("get the second page of 10 recent items") {

      val items11to20 = Api.search.page(2).response

      items11to20.pageSize should be (10)
      items11to20.currentPage should be (2)
      items11to20.results.foreach ( item => println(item.webTitle))
    }

    scenario("get the most recent 25 items") {

      val items11to20 = Api.search.pageSize(25).response

      items11to20.pageSize should be (25)
      items11to20.results.foreach ( item => println(item.webTitle))
    }

  }

  feature("Finding Content:") {

    scenario("find content by free text query") {
      val search = Api.search.q("tottenham hotspur")

      search.response.total should be > (0)
      search.response.results.foreach (item => println(item.webTitle))
    }

    scenario("ordering free text query results by relevance") {
      val search = Api.search
              .q("tottenham hotspur white hart lane")
              .orderBy("relevance")

      search.response.total should be > (0)
      search.response.results.foreach (item => println(item.webTitle))
    }

    scenario("find content by tag") {
      val search = Api.search.tag("football/tottenham-hotspur")

      search.response.total should be > (0)
      search.response.results.foreach (item => println(item.webTitle))
    }

    scenario("find content by multiple tags") {
      val search = Api.search.tag("football/tottenham-hotspur,tone/matchreports")

      search.response.total should be > (0)
      search.response.results.foreach (item => println(item.webTitle))
    }

    scenario("find content in a section") {
      val search = Api.search.section("football")

      search.response.total should be > (0)
      search.response.results.foreach (item => println(item.webTitle))
    }

    scenario("find content between 2 dates") {
      val search = Api.search
              .fromDate(new DateMidnight(2009, 1, 1))
              .toDate(new DateMidnight(2009, 12, 31))

      search.response.total should be > (0)
      search.response.results.foreach (item => println(item.webTitle))
    }

    scenario("did you mean?") {
      val search = Api.search.q("the green hills of ingland")

      search.response.total should be (0)
      println("Did you mean " + search.response.didYouMean + "?")
    }
  }

  feature ("configuring content display:") {

    scenario("retrieving all content's tags") {
      val search = Api.search.pageSize(1).showTags("all")

      val tags = search.response.results.head.tags
      tags.length should be > (0)
      tags.foreach (tag => println(tag.tagType + ":" +tag.webTitle))
    }

    scenario("retrieving just the content's keywords") {
      val search = Api.search.pageSize(1).showTags("keyword")

      val tags = search.response.results.head.tags
      tags.foreach (tag => println(tag.tagType + ":" +tag.webTitle))
    }

    scenario("retrieving an article's headline and trail") {
      val search = Api.search.pageSize(1).tag("type/article")
              .showFields("headline,trail-text")

      val fields = search.response.results.head.fields.getOrElse(Map())
      fields.keys.foreach (fieldKey => println(fieldKey + "->" +fields(fieldKey)))
    }

    scenario("retrieving all article's fields") {
      val search = Api.search.pageSize(1).tag("type/article")
              .showFields("all")

      val fields = search.response.results.head.fields.getOrElse(Map())
      fields.keys.foreach (fieldKey => println(fieldKey + "->" +fields(fieldKey)))
    }
  }

  feature("Finding tags") {

    // pagination and query terms work much like content search

    scenario("find some tags") {
      Api.tags.response.results.foreach(tag => println(tag.webTitle))
    }

    scenario("find tags representing series") {
      Api.tags.tagType("series").response.results.foreach(tag => println(tag.tagType + ":" + tag.webTitle))
    }

    scenario("find tags in the technology section") {
      Api.tags.section("technology").response.results.foreach(tag => println(tag.webTitle + " (" + tag.sectionName.get + ")"))
    }
  }

  feature("finding sections") {

    // not much to see here, the sections search is not even paginated, this gets you the list
    // of all section id you can use.

    // You can use the query term, q, parameter to restrict your seach, or just use your eyes.
    scenario("listing the sections") {
      Api.sections.response.results.foreach(section => println(section.id))
    }
  }

  feature("getting more information about an individual item") {

    scenario("loading a content item from seach results") {
      // why would you do this when you can flesh out results in the search?
      // the answer is you can get more information on the individual item's url
      // this includes pictures etc for galleries and articles (and more stuff in the future).
      // Unfortunately a key is needed to get at this data and I'm not handing my key out
      // here so the example is a bit noddy.

      val search = Api.search.q("tottenham hotspur").pageSize(1)

      val contentApiUrl = search.response.results.head.apiUrl
      println("following api url: " + contentApiUrl)

      val item = Api.item.apiUrl(contentApiUrl)
      println("loaded " + item.response.content.get.webTitle)
    }

    scenario("loading lead content for a tag") {
      val item = Api.item.itemId("world/iraq")
      item.response.leadContent.size should  be > (0)
      item.response.leadContent.foreach (item => println(item.webTitle))
    }

    scenario("loading a stories story package") {

      // look at the content on the homepage and find the first item that has a package
      val networkFront = Api.item.itemId("").showFields("has-story-package").showEditorsPicks(true).pageSize(1)
      val contentWithPackage = networkFront.response.editorsPicks.filter(_.fields.get("hasStoryPackage") == "true").head

      val contentApiUrl = contentWithPackage.apiUrl
      println("following api url: " + contentApiUrl)

      val item = Api.item.apiUrl(contentApiUrl).showStoryPackage(true)
      println("loaded " + item.response.content.get.webTitle)
      item.response.storyPackage.size should be > 0

      println("story package headlines:")
      item.response.storyPackage foreach (c => println("\t" + c.webTitle))
    }

    scenario("loading editors picks for the us homepage") {

      val usNetworkFront = Api.item.itemId("").edition("US").showEditorsPicks(true)

      println("US network front editors picks:")
      usNetworkFront.response.editorsPicks.foreach (c => println("\t" + c.webTitle))
    }
  }

  feature("getting the most viewed content in a section") {

    scenario("showing the most viewed for a section") {

      val politicsSection = Api.item.itemId("politics").showMostViewed()

      println("most viewed for politics:")
      politicsSection.response.mostViewed.foreach (c => println("\t" + c.webTitle))
    }
  }

  feature("getting expired content") {

      scenario("cannot load expired content if I am not an internal user") {

        val expiredArticle = Api.item.itemId("football/2012/sep/14/zlatan-ibrahimovic-paris-st-germain-toulouse")
          .showExpired()

        val error = intercept[IOException]{ expiredArticle.response.content }
        error.getMessage should include("400")
      }
    }

  feature("refining search.response.results") {

    scenario("finding the most popular keywords for a seach") {
      val search = Api.search.pageSize(1).section("music")
              .showRefinements("keyword").refinementSize(20)

      search.response.refinementGroups foreach { group =>
        println(group.refinementType)
        group.refinements.foreach { refinement =>
          println("\t" + refinement.displayName + " (" + refinement.count + ")")
        }
      }
    }
  }

  feature("contributor bios and pictures") {
    scenario("show contributor bios") {
      Api.tags.tagType("contributor").response.results.filter(_.bio.isDefined).foreach(tag => println(tag.webTitle + ":" + tag.bio.get))
    }
    scenario("show contributor byline Pictures") {
      Api.tags.tagType("contributor").response.results.foreach(tag => println(tag.webTitle + ":" + tag.bylineImageUrl.getOrElse("None")))
    }
  }
  
  feature("editorial folders are available") {
    scenario("can query for folders") {
      println("Query folders")

      Api.folders.response.results.foreach(folder => println("    "+folder.id+": "+folder.webTitle))
    }

    scenario("can query tags by folder") {
      println("Tags by Folder: folder/traveleditorsindex/travelawards")
      Api.tags.ids("folder/traveleditorsindex/travelawards").response.results.foreach(
        tag => println("    "+tag.webTitle))
    }

    scenario("can query content by folder") {
      println("Content by Folder: folder/traveleditorsindex/travelawards")
      Api.item.itemId("folder/traveleditorsindex/travelawards").response.results.foreach(
        content => println("    "+content.webTitle))
    }

    /**
     * Note that this is similar to the above
     */
    scenario("can filter content search by folder") {
      println("Content Search by Folder: folder/traveleditorsindex/travelawards")
      Api.search.q("sausages").folder("folder/traveleditorsindex/travelawards").response.results.foreach(
        content => println("    "+content.webTitle))
    }
  }
}
