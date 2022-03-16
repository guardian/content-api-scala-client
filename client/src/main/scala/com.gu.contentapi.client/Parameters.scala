package com.gu.contentapi.client

import java.time.Instant

trait Parameters[Owner <: Parameters[Owner]] { self: Owner =>
  def stringParam(key: String, value: String): Owner = {
    withParameter(StringParameter(key, Some(value)))
  }

  def intParam(key: String, value: Int): Owner = {
    withParameter(IntParameter(key, Some(value)))
  }

  def boolParam(key: String, value: Boolean): Owner = {
    withParameter(BoolParameter(key, Some(value)))
  }

  def dateParam(key: String, value: Instant): Owner = {
    withParameter(DateParameter(key, Some(value)))
  }

  def withParameter(parameter: Parameter): Owner = {
    withParameters(parameterHolder.updated(parameter.name, parameter))
  }

  def withParameters(parameterMap: Map[String, Parameter]): Owner

  def has(param: String) = parameterHolder.contains(param)

  protected def parameterHolder: Map[String, Parameter]

  def parameters: Map[String, String] = {
    // Scala 2.13 introduces `parameterHolder.view.mapValues`, but 2.12 borks at that
    // parameterHolder.view.mapValues(_.value).toMap.collect { case (k, Some(v)) => (k, v.toString) }
    parameterHolder.mapValues(_.value).toMap.collect { case (k, Some(v)) => (k, v.toString) }
  }

  protected trait OwnedParameter extends Parameter {
    type ParameterOwner = Owner
    def owner = self
  }

  case class StringParameter(name: String, value: Option[String] = None) extends OwnedParameter {
    type Self = String
    def withValue(newValue: Option[String]) = copy(value = newValue)
  }

  case class IntParameter(name: String, value: Option[Int] = None) extends OwnedParameter {
    type Self = Int
    def withValue(newValue: Option[Int]) = copy(value = newValue)
  }

  case class DateParameter(name: String, value: Option[Instant] = None) extends OwnedParameter {
    type Self = Instant
    def withValue(newValue: Option[Instant]) = copy(value = newValue)
  }

  case class BoolParameter(name: String, value: Option[Boolean] = None) extends OwnedParameter {
    type Self = Boolean
    def withValue(newValue: Option[Boolean]) = copy(value = newValue)
    def apply(): Owner = apply(true)
  }
}
