package com.jk.common.model.config

import zio.config._
import ConfigDescriptor._

case class QueueConfig(discoveredUrlQueue: String, targetUrlQueue: String)

object QueueConfig {
    val descriptor = (string("discoveredUrls") |@| string("targetUrls"))(QueueConfig.apply, QueueConfig.unapply)
}