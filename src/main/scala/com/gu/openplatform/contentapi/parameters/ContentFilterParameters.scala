package com.gu.openplatform.contentapi.parameters

import org.joda.time.DateTime

object ContentFilterParameters {
  def empty = ContentFilterParameters(None, None, None, None, None)

  implicit val contentFilterRenderParameters = new RenderParameters[ContentFilterParameters] {
    override def render(a: ContentFilterParameters): Map[String, String] = flatten(Map(
      "order-by" -> a.orderBy,
      "from" -> a.from.map(dateString),
      "to" -> a.to.map(dateString),
      "date-id" -> a.dateId,
      "use-date" -> a.useDate
    ))
  }
}

case class ContentFilterParameters(
  orderBy: Option[String],
  from: Option[DateTime],
  to: Option[DateTime],
  dateId: Option[String],
  useDate: Option[String]
) extends QueryParameters