package com.gu.openplatform.contentapi.parameters

object ShowParameters {
  def empty = ShowParameters(
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None
  )

  implicit val renderShowParameters = new RenderParameters[ShowParameters] {
    override def render(a: ShowParameters): Map[String, String] = flatten(Map(
      "show-fields" -> a.showFields,
      "show-snippets" -> a.showSnippets,
      "show-tags" -> a.showTags,
      "show-factboxes" -> a.showFactboxes,
      "show-media" -> a.showMedia,
      "show-elements" -> a.showElements,
      "show-related" -> a.showRelated.map(_.toString),
      "show-editors-picks" -> a.showEditorsPicks.map(_.toString),
      "edition" -> a.edition,
      "show-most-viewed" -> a.showMostViewed.map(_.toString),
      "show-story-package" -> a.showStoryPackage.map(_.toString),
      "show-best-bets" -> a.showBestBets.map(_.toString),
      "snippet-pre" -> a.snippetPre,
      "snippet-post" -> a.snippetPost,
      "show-inline-elements" -> a.showInlineElements,
      "show-expired" -> a.showExpired.map(_.toString)
    ))
  }
}

case class ShowParameters(
  showFields: Option[String],
  showSnippets: Option[String],
  showTags: Option[String],
  showFactboxes: Option[String],
  showMedia: Option[String],
  showElements: Option[String],
  showRelated: Option[Boolean],
  showEditorsPicks: Option[Boolean],
  edition: Option[String],
  showMostViewed: Option[Boolean],
  showStoryPackage: Option[Boolean],
  showBestBets: Option[Boolean],
  snippetPre: Option[String],
  snippetPost: Option[String],
  showInlineElements: Option[String],
  showExpired: Option[Boolean]
) extends QueryParameters