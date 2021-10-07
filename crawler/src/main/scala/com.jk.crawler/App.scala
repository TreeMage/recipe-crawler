package com.jk.crawler
import zio._
import zio.config._
import zio.config.syntax._
import zio.Console._
import com.jk.crawler.model.config.CrawlerApplicationConfig
import com.jk.common.model.config.RedisConfig
import com.redis.RedisClient
import com.jk.common.service.redis
import com.jk.crawler.service.crawler
import java.util.Properties
import zio.config.typesafe.TypesafeConfig

object App extends zio.App {

    override def run(args: List[String]): URIO[ZEnv,ExitCode] = app.provideSomeLayer[ZEnv](dependencies).exitCode

    val app = for {
        url <- redis.popFromUrlQueue()
        site <- crawler.crawl(url)
        _ <- printLine(site)
    } yield ()


    val configLayer = TypesafeConfig.fromHoconFile(new java.io.File("/home/johannes/recipe-thingy/recipe-crawler/crawler/conf/application.conf"),CrawlerApplicationConfig.descriptor)
    val redisConfigLayer = configLayer.narrow(_.redisConfig)
    val redisClientLayer = ZLayer.fromFunctionZIO[Has[RedisConfig], Throwable, RedisClient] { rc => 
        val config = rc.get
        IO.attempt(new RedisClient(config.host, config.port))
    }
    val queueConfigLayer = configLayer.narrow(_.queueConfig)
    val redisServiceLayer =  (queueConfigLayer ++ (redisConfigLayer >>> redisClientLayer)) >>> redis.RedisServiceLive.layer

    val crawlerConfigLayer = configLayer.narrow(_.crawlerConfig)
    val crawlerLayer = crawlerConfigLayer >>> crawler.CrawlerServiceLive.layer

    val dependencies = redisServiceLayer ++ crawlerLayer

}