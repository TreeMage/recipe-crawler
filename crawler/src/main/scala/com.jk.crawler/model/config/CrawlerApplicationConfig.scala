package com.jk.crawler.model.config

import zio.config._
import ConfigDescriptor._
import com.jk.common.model.config.RedisConfig
import com.jk.common.model.config.CrawlerConfig
import com.jk.common.model.config.QueueConfig

case class CrawlerApplicationConfig(redisConfig: RedisConfig, queueConfig: QueueConfig, crawlerConfig: CrawlerConfig)

object CrawlerApplicationConfig {
    val descriptor = (nested("redis")(RedisConfig.descriptor) |@| nested("queue")(QueueConfig.descriptor) |@| nested("crawler")
    (CrawlerConfig.descriptor))(CrawlerApplicationConfig.apply, CrawlerApplicationConfig.unapply)
}