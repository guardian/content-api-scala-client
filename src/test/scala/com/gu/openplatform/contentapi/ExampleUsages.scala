package com.gu.openplatform.contentapi

import connection.Api
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FeatureSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class ExampleUsages extends FeatureSpec with ShouldMatchers {

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
}