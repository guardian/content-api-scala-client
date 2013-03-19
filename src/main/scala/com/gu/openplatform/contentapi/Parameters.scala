package com.gu.openplatform.contentapi

trait Parameters[Owner] {

  protected def self: Owner

  private var parameterList: List[QueryParameter] = Nil

  final def register(param: QueryParameter) = parameterList ::= param

  def parameters: Map[String, Any] = parameterList.flatMap(_.asTuple).toMap
}
