package com.gu.openplatform.contentapi.parameters

object FilterParameters {
  def empty = FilterParameters(None, None, None, None, None)

  implicit val renderFilterParameters = new RenderParameters[FilterParameters] {
    def render(params: FilterParameters): Map[String, String] = flatten(Map(
      "q" -> params.q,
      "section" -> params.section,
      "ids" -> params.ids,
      "tag" -> params.tag,
      "folder" -> params.folder
    ))
  }
}

case class FilterParameters(
  q: Option[String],
  section: Option[String],
  ids: Option[String],
  tag: Option[String],
  folder: Option[String]
) extends QueryParameters
