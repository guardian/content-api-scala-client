package lib

import scala.concurrent.{ExecutionContext => EC}

trait ExecutionContext {
  implicit val executionContext: EC = EC.global
}
