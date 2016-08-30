package com.gu.contentapi.client.thrift

import java.io.ByteArrayInputStream

import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import org.apache.thrift.protocol.TCompactProtocol
import org.apache.thrift.transport.TIOStreamTransport

object ThriftDeserializer {

  def deserialize[T <: ThriftStruct](responseBody: Array[Byte], codec: ThriftStructCodec[T]): T = {
    val bbis = new ByteArrayInputStream(responseBody)
    val transport = new TIOStreamTransport(bbis)
    val protocol = new TCompactProtocol(transport)
    codec.decode(protocol)
  }
}

