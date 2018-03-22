package com.gu.contentapi.client

import com.gu.contentapi.client.model.v1._
import com.gu.contentatom.thrift.Atom

private[client] trait MetaResult[A] {
  type R
  def getCurrentPage: A => Int
  def getTotalPages: A => Int
  def isLastPage: A => Boolean = a => getCurrentPage(a) == getTotalPages(a)
  def getResults: A => Seq[R]
  def getId: R => String
}

object MetaResult {

  implicit val searchResponse = new MetaResult[SearchResponse] {
    type R = Content
    def getCurrentPage = _.currentPage
    def getTotalPages = _.pages
    def getResults = _.results
    def getId = _.id
  }

  implicit val itemResponse = new MetaResult[ItemResponse] {
    type R = Content
    def getCurrentPage = _.currentPage.getOrElse(1)
    def getTotalPages = _.pages.getOrElse(1)
    def getResults = _.results.getOrElse(Nil)
    def getId = _.id
  }

  implicit val tagsResponse = new MetaResult[TagsResponse] {
    type R = Tag
    def getCurrentPage = _.currentPage
    def getTotalPages = _.pages
    def getResults = _.results
    def getId = _.id
  }

  implicit val atomsResponse = new MetaResult[AtomsResponse] {
    type R = Atom
    def getCurrentPage = _.currentPage
    def getTotalPages = _.pages
    def getResults = _.results
    def getId = _.id
  }

}