package com.gu.contentapi.client

import com.gu.contentapi.client.model.v1._
import com.gu.contentatom.thrift.Atom

/** Typeclass witnessing how to extract the id for
  * a Next or Prev query
  */
trait PaginatedApiResult[Response] {
  /** the id for a next query, if any */
  def getNextId: Response => Option[String]
  /** the id for a prev query, if any */
  def getPrevId: Response => Option[String]
}

/** Instances of [[PaginatedApiResult]] for response types
  * potentially containing pages of results
  */
object PaginatedApiResult {

  implicit val searchResponse = new PaginatedApiResult[SearchResponse] {
    def getNextId = r => if (r.pages == r.currentPage) None else r.results.lastOption.map(_.id)
    def getPrevId = r => if (1 == r.currentPage) None else r.results.headOption.map(_.id)
  }

  implicit val itemResponse = new PaginatedApiResult[ItemResponse] {
    def getNextId = r => if (r.pages.exists(ps => r.currentPage.exists(cp => ps == cp))) None else r.results.flatMap(_.lastOption.map(_.id))
    def getPrevId = r => if (r.currentPage.exists(cp => 1 == cp)) None else r.results.flatMap(_.lastOption.map(_.id))
  }

  implicit val tagsResponse = new PaginatedApiResult[TagsResponse] {
    def getNextId = r => if (r.pages == r.currentPage) None else r.results.lastOption.map(_.id)
    def getPrevId = r => if (1 == r.currentPage) None else r.results.headOption.map(_.id)
  }

  implicit val atomsResponse = new PaginatedApiResult[AtomsResponse] {
    def getNextId = r => if (r.pages == r.currentPage) None else r.results.lastOption.map(_.id)
    def getPrevId = r => if (1 == r.currentPage) None else r.results.headOption.map(_.id)
  }

}