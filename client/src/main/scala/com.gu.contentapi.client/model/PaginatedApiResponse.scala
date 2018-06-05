package com.gu.contentapi.client

import com.gu.contentapi.client.model.v1._
import com.gu.contentatom.thrift.Atom

/** Typeclass witnessing how to extract the id for
  * a paginated query
  */
trait PaginatedApiResponse[Response] {
  /** the id for a next query, if any */
  def getNextId: Response => Option[String]
}

/** Instances of [[PaginatedApiResponse]] for response types
  * potentially containing pages of results
  */
object PaginatedApiResponse {

  implicit val searchResponse = new PaginatedApiResponse[SearchResponse] {
    def getNextId = r => if (r.results.length < r.pageSize) None else r.results.lastOption.map(_.id)
  }

  implicit val tagsResponse = new PaginatedApiResponse[TagsResponse] {
    def getNextId = r => if (r.results.length < r.pageSize) None else r.results.lastOption.map(_.id)
  }

  implicit val atomsResponse = new PaginatedApiResponse[AtomsResponse] {
    def getNextId = r => if (r.results.length < r.pageSize) None else r.results.lastOption.map(_.id)
  }

}