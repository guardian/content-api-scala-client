package com.gu.facia.client.lib

import com.gu.facia.client.models.{CollectionJson, ConfigJson, CustomSubnavConfig}
import com.gu.facia.client.{ApiClient, Environment}
import play.api.libs.json.{Format, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApiTestClient extends ApiClient with ResourcesHelper {
  private val environment = Environment.Dev

  private def retrieve[A: Format](key: String): Future[Option[A]] =
    Future.successful(slurp(key).map(Json.parse).flatMap(_.asOpt[A]))

  override def config: Future[ConfigJson] =
    retrieve[ConfigJson](environment.configS3Path).map(_.get)

  override def collection(id: String): Future[Option[CollectionJson]] =
    retrieve[CollectionJson](environment.collectionS3Path(id))

  override def subnavConfig(): Future[Option[CustomSubnavConfig]] =
    retrieve[CustomSubnavConfig](environment.customSubnavS3Path)
}
