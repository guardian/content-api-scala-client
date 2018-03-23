package com.gu.contentapi.client

import com.gu.contentapi.client.model.v1._
import com.gu.contentatom.thrift.Atom

private[client] trait MetaResult[Response] {
  type Result
  def getResults: Response => Seq[Result]
  def getId: Result => String
  def getCurrentPage: Response => Int
  def getTotalPages: Response => Int
  def isLastPage: Response => Boolean = a => getCurrentPage(a) == getTotalPages(a)
}

object MetaResult {

  implicit val searchResponse = new MetaResult[SearchResponse] {
    type Result = Content
    def getCurrentPage = _.currentPage
    def getTotalPages = _.pages
    def getResults = _.results
    def getId = _.id
  }

  implicit val itemResponse = new MetaResult[ItemResponse] {
    type Result = Content
    def getCurrentPage = _.currentPage.getOrElse(1)
    def getTotalPages = _.pages.getOrElse(1)
    def getResults = _.results.getOrElse(Nil)
    def getId = _.id
  }

  implicit val tagsResponse = new MetaResult[TagsResponse] {
    type Result = Tag
    def getCurrentPage = _.currentPage
    def getTotalPages = _.pages
    def getResults = _.results
    def getId = _.id
  }

  implicit val atomsResponse = new MetaResult[AtomsResponse] {
    type Result = Atom
    def getCurrentPage = _.currentPage
    def getTotalPages = _.pages
    def getResults = _.results
    def getId = _.id
  }

}