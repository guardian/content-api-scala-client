package com.gu.contentapi.firehose.kinesis

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.gu.contentapi.firehose.kinesis.KinesisStreamReader.{ checkApiAccess, kinesisClientFor }
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient
import software.amazon.kinesis.common.KinesisClientUtil.createKinesisAsyncClient
import software.amazon.kinesis.common.{ ConfigsBuilder, InitialPositionInStream, KinesisRequestsBuilder }
import software.amazon.kinesis.coordinator.CoordinatorConfig.ClientVersionConfig
import software.amazon.kinesis.coordinator.Scheduler
import software.amazon.kinesis.processor.ShardRecordProcessorFactory

import java.util.UUID
import java.util.concurrent.Executors

object KinesisStreamReader {
  def kinesisClientFor(credentialsProvider: AwsCredentialsProvider, region: Region): KinesisAsyncClient =
    createKinesisAsyncClient(
      KinesisAsyncClient.builder().credentialsProvider(credentialsProvider).region(region))

  /**
   * This method is a quick check to ensure that the running JVM process can execute a successful AWS SDK API call
   * to Kinesis. It blocks and fails fast. See https://github.com/guardian/content-api-firehose-client/pull/51
   */
  def checkApiAccess(client: KinesisAsyncClient, streamName: String): Unit = {
    val response = client.describeStreamSummary(
      KinesisRequestsBuilder.describeStreamSummaryRequestBuilder.streamName(streamName).build).get()
    require(response.streamDescriptionSummary().streamName() == streamName)
  }
}

trait KinesisStreamReader {
  val credentialsProvider: AwsCredentialsProvider
  val kinesisStreamReaderConfig: KinesisStreamReaderConfig
  val clientVersionCompatibility: ClientVersionConfig
  protected val eventProcessorFactory: ShardRecordProcessorFactory

  /* only applies when there are no checkpoints */
  val initialPosition = InitialPositionInStream.TRIM_HORIZON

  /* Unique ID for the worker thread */
  private val workerId = UUID.randomUUID().toString

  private val kinesisClient = kinesisClientFor(credentialsProvider, Region.of(kinesisStreamReaderConfig.awsRegion))

  private val dynamoClient = DynamoDbAsyncClient.builder().credentialsProvider(credentialsProvider).region(Region.of(kinesisStreamReaderConfig.awsRegion)).build()
  private val cwClient = CloudWatchAsyncClient.builder().credentialsProvider(credentialsProvider).region(Region.of(kinesisStreamReaderConfig.awsRegion)).build()
  private val configsBuilder = new ConfigsBuilder(
    kinesisStreamReaderConfig.streamName,
    kinesisStreamReaderConfig.applicationName,
    kinesisClient,
    dynamoClient,
    cwClient,
    workerId,
    eventProcessorFactory)

  lazy val scheduler: Scheduler = new Scheduler(
    configsBuilder.checkpointConfig(),
    configsBuilder.coordinatorConfig().clientVersionConfig(clientVersionCompatibility),
    configsBuilder.leaseManagementConfig(),
    configsBuilder.lifecycleConfig(),
    configsBuilder.metricsConfig(),
    configsBuilder.processorConfig(),
    configsBuilder.retrievalConfig())

  private lazy val threadPool = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat(s"${getClass.getSimpleName}-$workerId-thread-%d").build())

  /* Start the worker in a new thread. It will run forever */
  private lazy val workerThread =
    new Thread(scheduler, s"${getClass.getSimpleName}-$workerId")

  def start(): Thread = {
    workerThread.start()
    checkApiAccess(kinesisClient, kinesisStreamReaderConfig.streamName)
    workerThread
  }

  def shutdown(): Unit = {
    scheduler.shutdown()
    threadPool.shutdown()
  }

}
