package com.gu.openplatform.contentapi.parameters

object PaginationParameters {
  def empty = PaginationParameters(None, None)

  implicit val renderPaginationParameters = new RenderParameters[PaginationParameters] {
    override def render(a: PaginationParameters): Map[String, String] = flatten(Map(
      "page" -> a.page.map(_.toString),
      "page-size" -> a.page.map(_.toString)
    ))
  }
}

case class PaginationParameters(
  page: Option[Int],
  pageSize: Option[Int]
) extends QueryParameters
