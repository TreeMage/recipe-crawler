package com.jk.common.model.config

import zio.config._
import ConfigDescriptor._

case class QueueConfig(urlQueue: String, seedQueue: String)

object QueueConfig {
    val descriptor = (string("url") |@| string("seed"))(QueueConfig.apply, QueueConfig.unapply)
}