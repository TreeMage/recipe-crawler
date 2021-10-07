package com.jk.crawler
import zio._
import zio.config.typesafe.TypesafeConfig
import com.redis.RedisClient
import com.jk.common.model.config.RedisConfig

object App extends zio.App {

    override def run(args: List[String]): URIO[ZEnv,ExitCode] = app.exitCode

    lazy val app = ZIO.succeed(0)

}