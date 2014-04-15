package com.gu.openplatform.contentapi.parameters

object ReferenceParameters {
  def empty = ReferenceParameters(None, None)

  implicit val renderReferenceParameters = new RenderParameters[ReferenceParameters] {
    override def render(a: ReferenceParameters): Map[String, String] = flatten(Map(
      "reference" -> a.reference,
      "reference-type" -> a.referenceType
    ))
  }
}

case class ReferenceParameters(
  reference: Option[String],
  referenceType: Option[String]
) extends QueryParameters
