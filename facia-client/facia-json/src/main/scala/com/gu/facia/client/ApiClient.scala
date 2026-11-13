package com.gu.facia.client

import com.gu.etagcaching.FreshnessPolicy.AlwaysWaitForRefreshedValue
import com.gu.etagcaching.aws.s3.{ObjectId, S3ByteArrayFetching}
import com.gu.etagcaching.{ConfigCache, ETagCache}
import com.gu.facia.client.models.{CollectionJson, ConfigJson, CustomSubnavConfig}
import play.api.libs.json.{Format, JsError, JsSuccess, Json}

import scala.concurrent.{ExecutionContext, Future}

trait ApiClient {
  def config: Future[ConfigJson]

  def collection(id: String): Future[Option[CollectionJson]]

  def subnavConfig(): Future[Option[CustomSubnavConfig]]
}

object ApiClient {
  private val Encoding = "utf-8"

  private def getOrWarnAboutMissingConfig(
      configOpt: Option[ConfigJson]
  ): ConfigJson =
    configOpt.getOrElse(throw BackendError("Config was missing!! NOT GOOD!"))

  private def parseBytes[B: Format](bytes: Array[Byte]): B =
    Json.fromJson[B](Json.parse(new String(bytes, Encoding))) match {
      case JsSuccess(value, _) => value
      case JsError(errors)     =>
        throw JsonDeserialisationError(
          s"Failed to parse JSON: $errors"
        )
    }

  /** @param s3Fetching
    *   see scaladoc on `S3ByteArrayFetching` i.e. use `S3ObjectFetching.byteArraysWith(s3AsyncClient)`
    */
  def withCaching(
      bucket: String,
      environment: Environment,
      s3Fetching: S3ByteArrayFetching,
      configureCollectionCache: ConfigCache = _.maximumSize(10000) // at most 1GB RAM worst case
  )(implicit ec: ExecutionContext): ApiClient = new ApiClient {
    private val fetching =
      s3Fetching.keyOn[String](path => ObjectId(bucket, path))

    def eTagCache[B: Format](configureCache: ConfigCache) = new ETagCache(
      fetching.thenParsing(parseBytes[B]),
      AlwaysWaitForRefreshedValue,
      configureCache
    )

    private val configCache = eTagCache[ConfigJson](_.maximumSize(1))
    private val collectionCache =
      eTagCache[CollectionJson](configureCollectionCache)
    private val subnavCache = eTagCache[CustomSubnavConfig](_.maximumSize(1))

    override def config: Future[ConfigJson] =
      configCache.get(environment.configS3Path).map(getOrWarnAboutMissingConfig)

    override def collection(id: String): Future[Option[CollectionJson]] =
      collectionCache.get(environment.collectionS3Path(id))

    override def subnavConfig(): Future[Option[CustomSubnavConfig]] =
      subnavCache.get(environment.customSubnavS3Path)
  }
}
