package com.jk.master.model.config

import com.jk.common.model.config.RedisConfig
import zio.config._
import ConfigDescriptor._
import com.jk.common.model.config.QueueConfig

case class ApplicationConfig(redisConfig: RedisConfig, queueConfig: QueueConfig)

object ApplicationConfig {

    val descriptor = ((nested("redis")(RedisConfig.descriptor)) |@| (nested("queue")(QueueConfig.descriptor)))(ApplicationConfig.apply, ApplicationConfig.unapply)
}
