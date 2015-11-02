package com.gu.contentapi.client

object Limits {
  private val AcceptedUrlSize = 4096
  private val MaxEc2HostNameSize = "ec2-xxx-xxx-xxx-xxx.eu-west-1.compute.amazonaws.com".length
  private val MaximumTierParameterSize = "?user-tier=developer".length

  // All of the above seems rather fragile, hence this buffer
  private val SafetyBufferSize = 100

  val UrlSize = AcceptedUrlSize - (MaxEc2HostNameSize + MaximumTierParameterSize + SafetyBufferSize)
  val MaxSearchQueryIdSize = 50
}
