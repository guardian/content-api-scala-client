package com.gu.contentapi.client

import com.gu.contentapi.client.model.v1._
import com.gu.contentatom.thrift.Atom

trait PaginatedApiResult[Response] {
  type Result
  def getNextId: Response => Option[String]
  def getPrevId: Response => Option[String]
}

object PaginatedApiResult {

  implicit val searchResponse = new PaginatedApiResult[SearchResponse] {
    type Result = Content
    def getNextId = r => if (r.pages == r.currentPage) None else r.results.lastOption.map(_.id)
    def getPrevId = r => if (1 == r.currentPage) None else r.results.headOption.map(_.id)
  }

  implicit val itemResponse = new PaginatedApiResult[ItemResponse] {
    type Result = Content
    def getNextId = r => if (r.pages.exists(ps => r.currentPage.exists(cp => ps == cp))) None else r.results.flatMap(_.lastOption.map(_.id))
    def getPrevId = r => if (r.currentPage.exists(cp => 1 == cp)) None else r.results.flatMap(_.lastOption.map(_.id))
  }

  implicit val tagsResponse = new PaginatedApiResult[TagsResponse] {
    type Result = Tag
    def getNextId = r => if (r.pages == r.currentPage) None else r.results.lastOption.map(_.id)
    def getPrevId = r => if (1 == r.currentPage) None else r.results.headOption.map(_.id)
  }

  implicit val atomsResponse = new PaginatedApiResult[AtomsResponse] {
    type Result = Atom
    def getNextId = r => if (r.pages == r.currentPage) None else r.results.lastOption.map(_.id)
    def getPrevId = r => if (1 == r.currentPage) None else r.results.headOption.map(_.id)
  }

}