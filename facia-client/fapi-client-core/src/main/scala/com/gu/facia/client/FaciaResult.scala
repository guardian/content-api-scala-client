package com.gu.facia.client

sealed trait FaciaResult

case class FaciaSuccess(get: Array[Byte]) extends FaciaResult
case class FaciaUnknownError(message: String) extends FaciaResult

case class FaciaNotFound(message: String) extends FaciaResult
case class FaciaNotAuthorized(message: String) extends FaciaResult
