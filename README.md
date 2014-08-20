Content API Scala Client
========================

A Scala client for the Guardian's [Content API] (http://explorer.content.guardianapis.com/).


Usage
-----

### Adding the dependency

Add the following lines to your [SBT build file] (http://www.scala-sbt.org/0.13.0/docs/Getting-Started/Basic-Def.html):

    libraryDependencies += "com.gu" %% "content-api-client" % "2.19"

### Making calls

There are four different types of request that can be made: for a single item, or to filter all content, tags, or sections.

#### Single item

Every item on http://www.theguardian.com/ can be retrieved on the same path at http://content.guardianapis.com/. For example:

    // a content item
    Api.item.itemId("/commentisfree/2013/jan/16/vegans-stomach-unpalatable-truth-quinoa").response map { response =>
      response.content.get.webTitle
    }

    // a tag
    Api.item.itemId("/travel/france").tag.response.map(_.webTitle)

    // latest content for a tag
    Api.item.itemId("/travel/france").response.foreach(_.results.foreach(content => println(content.webTitle)))

#### Content

Filtering or searching for multiple content items happens at http://content.guardianapis.com/search. For example:

    // total number of content items
    Api.search.response.map(_.total)

    // the web titles of the 10 most recent content items
    Api.search.response.map(_.foreach(content => println(content.webTitle)))

    // the web titles of 11-20th most recent items of content
    Api.search.page(2).response.foreach(_.foreach(content => println(content.webTitle)))

    // the most recent content matching a search term
    Api.search.q("cheese on toast").response.foreach(_.foreach(content => println(content.webTitle)))

    // the most relevant content matching a search term
    Api.search.q("cheese on toast").orderBy("relevance").response.foreach(_.foreach(content => println(content.webTitle)))

    // content matching multiple tags
    Api.search.tags("lifeandstyle/cheese,type/gallery").response.foreach(_.foreach(content => println(content.webTitle)))

#### Tag search

Filtering or searching for multiple tags happens at http://content.guardianapis.com/tags. For example:

    // return the first 10 tags
    Api.tags.response.foreach(_.foreach(tag => println(tag.tagType + ":" + tag.webTitle)))

    // return the first 10 series tags
    Api.tags.tagType("series").response.foreach(_.foreach(tag => println(tag.tagType + ":" + tag.webTitle)))

#### Section search

Filtering or searching for multiple sections happens at http://content.guardianapis.com/sections. For example:

    // return all sections
    Api.sections.foreach(_.foreach(section => println(section.id)))


More reading
------------

Further examples can be found in [ExampleUsageTest.scala] (src/test/scala/ExampleUsageTest.scala).
