Content API Scala Client
========================

A Scala client for the Guardian's [Content API] (http://explorer.content.guardianapis.com/).


Usage
-----

Add the following to your SBT build definition:

```scala
libraryDependencies += "com.gu" %% "content-api-client" % "x.y"
```

There are four different types of request that can be made: for a single item, or to filter all content, tags, or sections.

### Single item

Every item on http://www.theguardian.com/ can be retrieved on the same path at http://content.guardianapis.com/. For example:

```scala
// print the web title of a content item
for (response <- Api.item.itemId("commentisfree/2013/jan/16/vegans-stomach-unpalatable-truth-quinoa").response)
yield for (content <- response.content) {
  println(content.webTitle)
}

// print the web title of a tag
for (response <- Api.item.itemId("travel/france").response)
yield for (tag <- response.tag) {
  println(tag.webTitle)
}

// print the web title of a section
for (response <- Api.item.itemId("environment").response)
yield for (section <- response.section) {
  println(section.webTitle)
}
```

### Content

Filtering or searching for multiple content items happens at http://content.guardianapis.com/search. For example:

```scala
// print the total number of content items
Api.search.response map { response =>
  println(response.total)
}

// print the web titles of the 10 most recent content items
Api.search.response map { response =>
  for (result <- response.results) println(result.webTitle)
}

// print the web titles of the 10 most recent content items matching a search term
Api.search.q("cheese on toast").response map { response =>
  for (result <- response.results) println(result.webTitle)
}

// print the web titles of the 10 most recent content items with certain tags
Api.search.tag("lifeandstyle/cheese,type/gallery").response map { response =>
  for (result <- response.results) println(result.webTitle)
}
```

### Tags

Filtering or searching for multiple tags happens at http://content.guardianapis.com/tags. For example:

```scala
// print the total number of tags
Api.tags.response map { response =>
  println(response.total)
}

// print the web titles of the first 10 tags
Api.tags.response map { response =>
  for (result <- response.results) println(result.webTitle)
}

// print the web titles and numbers of the first 10 tags in the books section with ISBN references
Api.tags.section("books").referenceType("isbn").showReferences("isbn").response map { response =>
  for (result <- response.results) println(result.webTitle + " -- " + result.references.head.id)
}
```

### Sections

Filtering or searching for multiple sections happens at http://content.guardianapis.com/sections. For example:

```scala
// print the web title of each section
Api.sections.response map { response =>
  for (result <- response.results) println(result.webTitle)
}
```
