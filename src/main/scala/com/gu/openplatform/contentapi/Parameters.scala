package com.gu.openplatform.contentapi

trait ParameterHolder {
  var parameterList: List[QueryParameter] = Nil
  def register(param: QueryParameter) = parameterList ::= param
}

trait Parameters[OwnerType <: ParameterHolder] extends ParameterHolder {
  def parameters: Map[String, Any] = parameterList.flatMap(_.asTuple).toMap
  protected def self: OwnerType with ParameterHolder = this.asInstanceOf[OwnerType]
}



