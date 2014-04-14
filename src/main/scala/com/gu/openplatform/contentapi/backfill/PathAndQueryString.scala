package com.gu.openplatform.contentapi.backfill

object PathAndQueryString {
  private val QueryStringParts = """([^?]+)\?(.*)""".r
  private val KeyValuePair = """([^=]+)=(.*)""".r

  def extract(uri: String) = uri match {
    case QueryStringParts(path, uri) => (path, (uri split "&" collect {
      case KeyValuePair(key, value) => (key, value)
    }).toSeq)
    case _ => (uri, Nil)
  }
}
