package com.gu.contentapi.client.model

case class ContentApiRecoverableException(httpStatus: Int, httpMessage: String) extends Exception(httpMessage)
