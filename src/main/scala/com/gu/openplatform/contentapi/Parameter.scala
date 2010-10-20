package com.gu.openplatform.contentapi

import org.joda.time.ReadableInstant

trait QueryParameter {
  def asTuple: Option[(String, Any)]
}

// This class is heavily modelled on net.liftweb.record.Field
abstract class Parameter[ThisType, OwnerType <: ParameterHolder](owner: OwnerType, name: String) extends QueryParameter {
  var value: Option[ThisType] = None

  def asTuple = value.map(name -> _)

  def apply(newValue: ThisType) = { value = Some(newValue); owner }
  def apply(newValue: Option[ThisType]) = { value = newValue; owner }
  def reset() = { value = None; owner }

  owner.register(this)
}

class StringParameter[OwnerType <: ParameterHolder](owner: OwnerType, name: String)
  extends Parameter[String, OwnerType](owner, name)

class IntParameter[OwnerType <: ParameterHolder](owner: OwnerType, name: String)
  extends Parameter[Int, OwnerType](owner, name)

class DateParameter[OwnerType <: ParameterHolder](owner: OwnerType, name: String)
  extends Parameter[ReadableInstant, OwnerType](owner, name)

class BoolParameter[OwnerType <: ParameterHolder](owner: OwnerType, name: String)
  extends Parameter[Boolean, OwnerType](owner, name) {

  def apply(): OwnerType = apply(true)
}