package com.gu.openplatform.contentapi

import org.joda.time.ReadableInstant

trait QueryParameter {
  def asTuple: Option[(String, Any)]
}

// This class is heavily modelled on net.liftweb.record.Field
abstract class Parameter[Self, Owner <: Parameters[Owner]](owner: Owner, name: String) extends QueryParameter {
  var value: Option[Self] = None

  def asTuple = value.map(name -> _)

  def apply(newValue: Self) = { value = Some(newValue); owner }
  def apply(newValue: Option[Self]) = { value = newValue; owner }
  def reset() = { value = None; owner }

  owner.register(this)
}

class StringParameter[Owner <: Parameters[Owner]](owner: Owner, name: String)
  extends Parameter[String, Owner](owner, name)

class IntParameter[Owner <: Parameters[Owner]](owner: Owner, name: String)
  extends Parameter[Int, Owner](owner, name)

class DateParameter[Owner <: Parameters[Owner]](owner: Owner, name: String)
  extends Parameter[ReadableInstant, Owner](owner, name)

class BoolParameter[Owner <: Parameters[Owner]](owner: Owner, name: String)
  extends Parameter[Boolean, Owner](owner, name) {

  def apply(): Owner = apply(true)
}
