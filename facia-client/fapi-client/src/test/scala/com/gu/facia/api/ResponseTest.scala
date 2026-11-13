package com.gu.facia.api

import org.scalatest.concurrent.ScalaFutures
import org.scalatest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

class ResponseTest extends AnyFreeSpec with Matchers with ScalaFutures {

  "Response.filter" - {
    "should return the same result if the filter function returns true" in {
      Response.Right[Int](4).filter(_ == 4).asFuture.futureValue should equal(
        Right(4)
      )
    }

    "should return a Left(FilteredOut) if the filter function returns false" in {
      Response.Right[Int](4).filter(_ == 3).asFuture.futureValue should equal(
        Left(FilteredOut)
      )
    }

    "should play nicely with for comprehensions" in {
      for (r <- Response.Right[Int](4) if r == 4) yield r should equal(Right(4))
      for (r <- Response.Right[Int](4) if r == 3)
        yield r should equal(Left(FilteredOut))
    }
  }
}
