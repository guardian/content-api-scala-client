package lib

import com.gu.contentapi.client.model.v1.{ContentType, Content}

trait TestContent {
  val baseContent = Content(
    id = "content-id",
    `type` = ContentType.Article,
    sectionId = Some("section-id"),
    sectionName = Some("Section Name"),
    webPublicationDate = None,
    webTitle = "webTitle",
    webUrl = "webUrl",
    apiUrl = "apiUrl",
    fields = None,
    tags = Nil,
    elements = None,
    references = Nil,
    isExpired = None,
    blocks = None,
    rights = None,
    crossword = None
  )
}
