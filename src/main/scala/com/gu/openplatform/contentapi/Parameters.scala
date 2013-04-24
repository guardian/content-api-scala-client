package com.gu.openplatform.contentapi

import org.joda.time.ReadableInstant

trait Parameters[Owner <: Parameters[Owner]] { self: Owner =>

  def updated(parameterName: String, parameter: Parameter): Owner =
    updated(parameterHolder.updated(parameterName, parameter))

  def updated(parameterMap: Map[String, Parameter]): Owner

  protected def parameterHolder: Map[String, Parameter]

  def parameters: Map[String, String] =
    parameterHolder.mapValues(_.value).collect { case (k, Some(v)) => (k, v.toString) }


  protected trait OwnedParameter extends Parameter {
    type ParameterOwner = Owner
    def owner = self
  }

  case class StringParameter(name: String, value: Option[String] = None) extends OwnedParameter {
    type Self = String
    def updated(newValue: Option[String]) = copy(value = newValue)
  }

  case class IntParameter(name: String, value: Option[Int] = None) extends OwnedParameter {
    type Self = Int
    def updated(newValue: Option[Int]) = copy(value = newValue)
  }

  case class DateParameter(name: String, value: Option[ReadableInstant] = None) extends OwnedParameter {
    type Self = ReadableInstant
    def updated(newValue: Option[ReadableInstant]) = copy(value = newValue)
  }

  case class BoolParameter(name: String, value: Option[Boolean] = None) extends OwnedParameter {
    type Self = Boolean
    def updated(newValue: Option[Boolean]) = copy(value = newValue)
    def apply(): Owner = apply(true)
  }

}
