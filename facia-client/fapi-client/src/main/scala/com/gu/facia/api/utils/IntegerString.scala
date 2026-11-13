package com.gu.facia.api.utils

import scala.util.Try

object IntegerString {
  def unapply(s: String): Option[Int] = Try {
    s.toInt
  }.toOption
}
