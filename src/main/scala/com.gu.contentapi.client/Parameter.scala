package com.gu.contentapi.client

trait Parameter {

  type Self
  type ParameterOwner <: Parameters[ParameterOwner]

  def owner: ParameterOwner
  def name: String
  def value: Option[Self]

  def asTuple = value.map(name -> _)

  def withValue(newValue: Option[Self]): Parameter

  def apply(newValue: Self): ParameterOwner = apply(Some(newValue))

  def apply(newValue: Option[Self]): ParameterOwner = owner.withParameter(this.withValue(newValue))

  def reset(): ParameterOwner = owner.withParameters(Map.empty)

}
