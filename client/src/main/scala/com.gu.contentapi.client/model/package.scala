package com.gu.contentapi.client

package object model {
  type PaginatedApiQuery[A <: Parameters[A]] = ContentApiQuery with PaginationParameters[A] with OrderByParameter[A]

  private[model] def not[A](f: A => Boolean): A => Boolean = !f(_)

  private[model] val isPaginationParameter = Set("page")
}