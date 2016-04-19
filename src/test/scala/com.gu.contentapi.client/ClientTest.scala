package com.gu.contentapi.client

trait ClientTest {

  val api = new GuardianContentClient("test")
  val apiThrift = new GuardianContentClient("test", useThrift = true)

}
