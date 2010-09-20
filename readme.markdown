Scala client for the Guardian's OpenPlatform Content API
========================================================

Introduction
------------

This library provides a simple client wrapper round the [guardian.co.uk][g.co.uk]
[Open Platform][openplatform] [Content API][api], for scala 2.8.0.

[g.co.uk]: http://guardian.co.uk
[openplatform]: http://www.guardian.co.uk/open-platform
[api]: http://content.guardianapis.com


To use from sbt:

    val guardianGithub = "Guardian Github Releases" at "http://guardian.github.com/maven/repo-releases"
    val contentApiClient = "com.gu.openplatform" %% "content-api-client" % "1.3" withSources()

To use from maven:

    <dependency>
        <groupId>com.gu.openplatform</groupId>
        <artifactId>content-api-client_2.8.0</artifactId>
        <version>1.3</version>
    </dependency>

    ...

    <repository>
        <id>com.gu</id>
        <name>Guardian Github Releases</name>
        <url>http://guardian.github.com/maven/repo-releases</url>
    </repository>

What calls can I make on the Content API?
=========================================

There are four different types of request that can be made on the Content API: content search, tag search,
section search and single item. The best place to look for documentation on these is to visit
http://content.guardianapis.com/ in a web browser, which will bring up the API explorer.

Content Search
--------------

The content search, on http://content.guardianapis.com/search, allows searching for content:

    // return total number of items of content
    Api.search.total

    // display the web titles of the 10 most recent items of content
    Api.search.foreach(c => println(c.webTitle))

    // display the web titles of 11-20th most recent items of content
    Api.search.page(2).foreach(c => println(c.webTitle))

    // get most recent content matching a search term
    Api.search.q("tottenham hotspur").foreach(c => println(c.webTitle))

    // get most relevant content matching a search term
    Api.search.q("tottenham hotspur white hart lane").orderBy("relevance").foreach(c => println(c.webTitle))

    // content matching multiple tags
    Api.search.tags("football/tottenham-hotspur,tone/matchreports").foreach(c => println(c.webTitle))

Tag Search
----------

The tag search, on http://content.guardianapis.com/tags, allows searching for tags:

    // return the first 10 tags
    Api.tags.foreach(tag => println(tag.tagType + ":" + tag.webTitle))

    // return the first 10 series tags
    Api.tags.tagType("series").foreach(tag => println(tag.tagType + ":" + tag.webTitle))

Section Search
--------------

The section search, on http://content.guardianapis.com/sections, allows searching for sections:

    // return all sections
    Api.sections.foreach(section => println(section.id))

Item
----

Every content item on http://www.guardian.co.uk should be available on the same url on
http://content.guardianapis.com:

    // content return
    Api.item.itemId("/politics/2010/sep/20/nick-clegg-conference-speech").content.get.webTitle

    // tag return
    Api.item.itemId("/travel/france").tag.get.webTitle

    // latest content for tag
    Api.item.itemId("/travel/france").results.foreach(c => println(c.webTitle))


More reading
============

Further examples can be found in [ExampleUsageTest.scala](src/test/scala/ExampleUsageTest.scala).

