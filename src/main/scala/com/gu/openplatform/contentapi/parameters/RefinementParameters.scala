package com.gu.openplatform.contentapi.parameters

object RefinementParameters {
  def empty = RefinementParameters(None, None)

  implicit val renderRefinementParameters = new RenderParameters[RefinementParameters] {
    override def render(a: RefinementParameters): Map[String, String] = flatten(Map(
      "refinement-size" -> a.refinementSize.map(_.toString),
      "refinements" -> a.refinements
    ))
  }
}

case class RefinementParameters(refinementSize: Option[Int], refinements: Option[String]) extends QueryParameters