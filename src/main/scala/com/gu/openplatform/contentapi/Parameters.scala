package com.gu.openplatform.contentapi

trait ParameterHolder {
  var parameterList: List[QueryParameter] = Nil
  def register(param: QueryParameter) = parameterList ::= param
}

trait Parameters[OwnerType <: ParameterHolder] 
    extends ParameterHolder with ParametersHelpers[OwnerType] {
      
  def parameters: Map[String, Any] = parameterList.flatMap(_.asTuple).toMap
  protected def self: OwnerType with ParameterHolder = this.asInstanceOf[OwnerType]
}

trait ParametersHelpers[OwnerType <: ParameterHolder] { self: Parameters[OwnerType] =>
  def stringParameter(name: String) = new StringParameter(self, name)
  def intParameter(name: String) = new IntParameter(self, name)
  def dateParameter(name: String) = new DateParameter(self, name)
  def boolParameter(name: String) = new BoolParameter(self, name)
}
