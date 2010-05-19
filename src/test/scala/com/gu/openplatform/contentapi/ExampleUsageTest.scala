package com.gu.openplatform.contentapi

import connection.Api
import model.ItemResponse
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FeatureSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class ExampleUsageTest extends FeatureSpec with ShouldMatchers {

  feature("Pagination:") {

    scenario("get the most recent 10 items") {

      val latest10Items = Api.searchQuery.search

      latest10Items.pageSize should be (10)
      latest10Items.results.foreach ( item => println(item.webTitle))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }

    scenario("get the second page of 10 recent items") {

      val items11to20 = Api.searchQuery.withPage(2).search

      items11to20.pageSize should be (10)
      items11to20.currentPage should be (2)
      items11to20.results.foreach ( item => println(item.webTitle))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }

    scenario("get the most recent 25 items") {

      val items11to20 = Api.searchQuery.withPageSize(25).search

      items11to20.pageSize should be (25)
      items11to20.results.foreach ( item => println(item.webTitle))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }

  }

  feature("Finding Content:") {

    scenario("find content by free text query") {
      val search = Api.searchQuery.withQueryTerm("tottenham hotspur").search

      search.total should be > (0)
      search.results.foreach (item => println(item.webTitle))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }

    scenario("ordering free text query results by relevance") {
      val search = Api.searchQuery
              .withQueryTerm("tottenham hotspur white hart lane")
              .orderBy("relevance")
              .search

      search.total should be > (0)
      search.results.foreach (item => println(item.webTitle))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }

    scenario("find content by tag") {
      val search = Api.searchQuery.withTagTerm("football/tottenham-hotspur").search

      search.total should be > (0)
      search.results.foreach (item => println(item.webTitle))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }

    scenario("find content by multiple tags") {
      val search = Api.searchQuery.withTagTerm("football/tottenham-hotspur,tone/matchreports").search

      search.total should be > (0)
      search.results.foreach (item => println(item.webTitle))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }

    scenario("find content in a section") {
      val search = Api.searchQuery.withSectionTerm("football").search

      search.total should be > (0)
      search.results.foreach (item => println(item.webTitle))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }

    scenario("find content between 2 dates") {
      // from date and to date can be used independently
      // input string is a date in YYYY-MM-DD format
      val search = Api.searchQuery.withFromDate("2009-01-01").withToDate("2009-12-31").search

      search.total should be > (0)
      search.results.foreach (item => println(item.webTitle))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }
  }

  feature ("configuring content display:") {

    scenario("retrieving all content's tags") {
      val search = Api.searchQuery.withPageSize(1).withShowTags("all").search

      val tags = search.results.head.tags.get // NB tags list is optional and not returned by default, hence the get
      tags.length should be > (0)
      tags.foreach (tag => println(tag.tagType + ":" +tag.webTitle))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }

    scenario("retrieving just the content's keywords") {
      val search = Api.searchQuery.withPageSize(1).withShowTags("keyword").search

      val tags = search.results.head.tags.get // NB tags list is optional and not returned by default, hence the get
      tags.foreach (tag => println(tag.tagType + ":" +tag.webTitle))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }

    scenario("retrieving an article's headline and trail") {
      val search = Api.searchQuery.withPageSize(1).withTagTerm("type/article")
              .withFields("headline,trail-text").search

      val fields: Map[String, String] = search.results.head.fields.get // NB fields map is optional and not returned by default, hence the get
      fields.keys.foreach (fieldKey => println(fieldKey + "->" +fields(fieldKey)))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }

    scenario("retrieving all article's fields") {
      val search = Api.searchQuery.withPageSize(1).withTagTerm("type/article")
              .withFields("all").search

      val fields: Map[String, String] = search.results.head.fields.get // NB fields map is optional and not returned by default, hence the get
      fields.keys.foreach (fieldKey => println(fieldKey + "->" +fields(fieldKey)))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }
  }

  feature("Finding tags") {

    // pagination and query terms work much like content search

    scenario("find some tags") {
      val search = Api.tagsQuery.tags
      search.results.foreach(tag => println(tag.webTitle))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }

    scenario("find tags representing series") {
      val search = Api.tagsQuery.withTypeTerm("series").tags
      search.results.foreach(tag => println(tag.tagType + ":" + tag.webTitle))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }

    scenario("find tags in the technology section") {
      val search = Api.tagsQuery.withSectionTerm("technology").tags
      search.results.foreach(tag => println(tag.webTitle + " (" + tag.sectionName.get + ")"))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }
  }

  feature("finding sections") {

    // not much to see here, the sections search is not even paginated, this gets you the list
    // of all section id you can use.

    // You can use the query term, q, parameter to restrict your seach, or just use your eyes.
    scenario("listing the sections") {
      val search = Api.sectionsQuery.sections
      search.results.foreach(section => println(section.id))

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }
  }

  feature("getting more information about an individual item") {

    scenario("loading a content item from seach results") {
      // why would you do this when you can flesh out results in the search?
      // the answer is you can get more information on the individual item's url
      // this includes pictures etc for galleries and articles (and more stuff in the future).
      // Unfortunately a key is needed to get at this data and I'm not handing my key out
      // here so the example is a bit noddy.

      val search = Api.searchQuery.withQueryTerm("tottenham hotspur").withPageSize(1).search

      val contentApiUrl = search.results.head.apiUrl
      println("following api url: " + contentApiUrl)

      val item: ItemResponse = Api.itemQuery.withApiUrl(contentApiUrl).query
      println("loaded " + item.content.get.webTitle)

      // if you run all these tests they will exceed the rate limit in the basic tier,
      // so putting in a cheeky sleep
      Thread.sleep(500)
    }
  }
}