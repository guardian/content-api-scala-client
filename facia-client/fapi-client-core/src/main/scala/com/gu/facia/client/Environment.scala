package com.gu.facia.client

case class Environment(stage: String) {
  private val s3PathPrefix: String = s"$stage/frontsapi"

  val configS3Path: String = s"$s3PathPrefix/config/config.json"
  def collectionS3Path(id: String): String =
    s"$s3PathPrefix/collection/$id/collection.json"
  val customSubnavS3Path: String =
    s"$s3PathPrefix/navigation/custom-subnav.json"
}

object Environment {
  val Prod = Environment("PROD")
  val Code = Environment("CODE")
  val Dev = Environment("DEV")
  val Test = Environment("TEST")
}
