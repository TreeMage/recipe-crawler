package com.jk.common.model.config
import zio.config._
import ConfigDescriptor._

case class RedisConfig private(host: String, port: Int)

object RedisConfig {
    val descriptor = (string("host") |@| int("port"))(RedisConfig.apply, RedisConfig.unapply)
}