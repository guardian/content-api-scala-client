package com.gu.openplatform.contentapi.backfill

import com.gu.openplatform.contentapi.{Parameters, Api}

trait BackfillQueryBuilding[F[+_]] { self: Api[F] =>
  def buildQueryFromString(queryString: String): Either[ItemQuery, SearchQuery] = {
    val (path, parameters) = PathAndQueryString.extract(queryString)

    val baseQuery = if (path startsWith "search") {
      Right(search)
    } else {
      Left(item.itemId(path))
    }

    def addParams[A <: Parameters[A]](query: A) =
      parameters.foldLeft(query) { case (q, (key, value)) => q.stringParam(key, value) }

    baseQuery.left.map(addParams).right.map(addParams)
  }
}
