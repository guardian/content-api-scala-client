package com.gu.openplatform.contentapi

package object util {
  implicit class RichString(s: String) {
    def /(s2: String) = s.stripSuffix("/") + "/" + s2.stripPrefix("/")
  }
}
