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

case class StringParameter[OwnerType <: ParameterHolder](name: String)(implicit owner: OwnerType)
  extends Parameter[String, OwnerType](owner, name)
    
case class IntParameter[OwnerType <: ParameterHolder](name: String)(implicit owner: OwnerType)
  extends Parameter[Int, OwnerType](owner, name)

case class DateParameter[OwnerType <: ParameterHolder](name: String)(implicit owner: OwnerType)
  extends Parameter[ReadableInstant, OwnerType](owner, name)

case class BoolParameter[OwnerType <: ParameterHolder](name: String)(implicit owner: OwnerType)
  extends Parameter[Boolean, OwnerType](owner, name) {

  def apply(): OwnerType = apply(true)
}