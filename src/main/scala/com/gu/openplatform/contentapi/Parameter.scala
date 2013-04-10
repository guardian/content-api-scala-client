package com.gu.openplatform.contentapi

trait Parameter {

  type Self
  type ParameterOwner <: Parameters[ParameterOwner]

  def owner: ParameterOwner
  def name: String
  def value: Option[Self]

  def asTuple = value.map(name -> _)

  def updated(newValue: Option[Self]): Parameter

  def apply(newValue: Self): ParameterOwner = apply(Some(newValue))

  def apply(newValue: Option[Self]): ParameterOwner = owner.updated(name, updated(newValue))

  def reset(): ParameterOwner = owner.updated(Map.empty)

}
