package com.gu.contentapi.client

object ExampleResponses {

  val notFoundJson =
    """{
      |  "response": {
      |    "status": "error",
      |    "message": "The requested resource could not be found."
      |  }
      |}""".stripMargin

  val notFoundResponse = HttpResponse(body = notFoundJson, statusCode = 404, statusMessage = "Not Found")

  val cyclistsJson =
    """{
      |  "response": {
      |    "status": "ok",
      |    "userTier": "developer",
      |    "total": 1,
      |    "content": {
      |      "type": "article",
      |      "sectionId": "commentisfree",
      |      "webTitle": "Just like cyclists, pedestrians must find a sense of self-righteousness | Zoe Williams",
      |      "webPublicationDate": "2012-08-01T20:15:00Z",
      |      "id": "commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry",
      |      "webUrl": "http://www.theguardian.com/commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry",
      |      "apiUrl": "http://content.guardianapis.com/commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry",
      |      "sectionName": "Comment is free"
      |    }
      |  }
      |}""".stripMargin

  val cyclistsResponse = HttpResponse(body = cyclistsJson, statusCode = 200, statusMessage = "OK")

  val removedJson =
    """{
      |  "response": {
      |    "status": "ok",
      |    "userTier": "developer",
      |    "total": 3,
      |    "startIndex": 1,
      |    "pageSize": 10,
      |    "currentPage": 1,
      |    "pages": 1,
      |    "orderBy": "newest",
      |    "results": [
      |      "some/removed/content/0",
      |      "some/removed/content/1",
      |      "some/removed/content/2"
      |    ]
      |  }
      |}
    """.stripMargin

  val removedResponse = HttpResponse(body = removedJson, statusCode = 200, statusMessage = "OK")

  val responses = Map(
    "http://content.guardianapis.com/commentisfree/2012/aug/01/cyclists-like-pedestrians-must-get-angry?api-key=test" -> cyclistsResponse,
    "http://content.guardianapis.com/content/removed?reason=expired&api-key=test" -> removedResponse
  )
}
