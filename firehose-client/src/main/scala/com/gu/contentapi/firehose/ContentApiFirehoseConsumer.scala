package com.gu.contentapi.firehose

import com.gu.contentapi.firehose.client.StreamListener
import com.gu.contentapi.firehose.kinesis.{KinesisStreamReader, KinesisStreamReaderConfig, SingleEventProcessor}
import com.gu.crier.model.event.v1.EventPayload.UnknownUnionField
import com.gu.crier.model.event.v1.EventType.EnumUnknownEventType
import com.gu.crier.model.event.v1.{Event, EventPayload, EventType}
import com.twitter.scrooge.ThriftStructCodec
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.kinesis.coordinator.CoordinatorConfig
import software.amazon.kinesis.coordinator.CoordinatorConfig.ClientVersionConfig
import software.amazon.kinesis.lifecycle.ShutdownReason
import software.amazon.kinesis.processor.{ShardRecordProcessor, ShardRecordProcessorFactory}

import scala.concurrent.duration._

class ContentApiFirehoseConsumer(
  val kinesisStreamReaderConfig: KinesisStreamReaderConfig,
  override val credentialsProvider: AwsCredentialsProvider,
  val streamListener: StreamListener,
  val filterProductionMonitoring: Boolean = false,
  val clientVersionCompatibility: CoordinatorConfig.ClientVersionConfig = ClientVersionConfig.CLIENT_VERSION_CONFIG_COMPATIBLE_WITH_2X // see https://github.com/guardian/content-api-firehose-client/pull/56
) extends KinesisStreamReader {

  lazy val eventProcessorFactory = new ShardRecordProcessorFactory {
    override def shardRecordProcessor(): ShardRecordProcessor = new ContentApiEventProcessor(filterProductionMonitoring, kinesisStreamReaderConfig.checkpointInterval, kinesisStreamReaderConfig.maxCheckpointBatchSize, streamListener)
  }
}

class ContentApiEventProcessor(filterProductionMonitoring: Boolean, override val checkpointInterval: Duration, override val maxCheckpointBatchSize: Int, streamListener: StreamListener)(implicit val codec: ThriftStructCodec[Event] = Event) extends SingleEventProcessor[Event] {

  override protected def processEvent(event: Event): Unit = {
    event.eventType match {

      case EventType.Update | EventType.RetrievableUpdate =>
        event.payload.foreach {
          case EventPayload.Content(content) => streamListener.contentUpdate(content)
          case EventPayload.RetrievableContent(content) => streamListener.contentRetrievableUpdate(content)
          case EventPayload.Atom(atom) => streamListener.atomUpdate(atom)
          case EventPayload.DeletedContent(content) => streamListener.contentDelete(content)
          case UnknownUnionField(e) => logger.warn(s"Received an unknown event payload $e. You should possibly consider updating")
        }

      case EventType.Delete =>
        if (filterProductionMonitoring && event.payloadId.startsWith("production-monitoring")) {
          // do nothing.
        } else {
          // Atom delete events are currently not supported by CAPI and therefore Crier
          streamListener.contentTakedown(event.payloadId)
        }

      case EnumUnknownEventType(e) => logger.warn(s"Received an unknown event type $e")
    }
  }

  override def shutdown(shutdownReason: ShutdownReason): Unit = {
    logger.info(s"EventProcessor is shutting down: shutdown state is $shutdownReason")
  }
}
