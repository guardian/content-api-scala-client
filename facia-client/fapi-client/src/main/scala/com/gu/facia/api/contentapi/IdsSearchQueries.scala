package com.gu.facia.api.contentapi

import scala.annotation.tailrec

object IdsSearchQueries {
  type Id = String
  type Url = String

  val MaxBatchSize = 50

  def makeBatches(ids: Seq[Id]): Option[Seq[Seq[Id]]] = {
    def batchAndRemaining(ids: Seq[Id]): Option[(Seq[Id], Seq[Id])] =
      ids.inits.find { init =>
        init.length <= MaxBatchSize
      } map { batch =>
        (batch, ids.drop(batch.length))
      }

    @tailrec
    def iter(ids: Seq[Id], accumulator: Seq[Seq[Id]]): Option[Seq[Seq[Id]]] = {
      if (ids.isEmpty) {
        Some(accumulator)
      } else
        batchAndRemaining(ids) match {
          case None                     => None
          case Some((batch, remaining)) =>
            iter(remaining, accumulator :+ batch)
        }
    }

    iter(ids, Seq.empty)
  }
}
