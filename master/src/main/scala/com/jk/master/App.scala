package com.jk.master
import zio._
import zio.Console._
import zio.config._
import zio.config.syntax._
import com.jk.master.model.config.ApplicationConfig
import zio.config.typesafe.TypesafeConfig
import com.jk.common.model.config.RedisConfig
import com.redis.RedisClient
import com.jk.common.service.redis._

object App extends zio.App {

  def run(args: List[String]): URIO[ZEnv,ExitCode] = app.provideLayer(configLayer ++ redisServiceLayer ++ Console.live).exitCode


  lazy val app = for {
    _ <- pushToUrlQueue("Hi")
    _ <- printLine("Pushed value 'Hi'.")
    v <- popFromUrlQueue()
    _ <- printLine(s"Popped value '$v'.")
  } yield ()

  val defaultConfig = Map(
    "name" -> "testName",
    "redis.host" -> "host",
    "redis.port" -> "1234",
    "redis.seedQueue" -> "seedQueue",
    "redis.urlQueue" -> "urlQueue"
  )

  lazy val configLayer = TypesafeConfig.fromHoconFile(new java.io.File("/home/johannes/recipe-thingy/recipe-crawler/master/conf/application.conf"),ApplicationConfig.descriptor)

  lazy val redisClientLayer: ZLayer[Has[RedisConfig], Throwable, Has[RedisClient]] = 
    ZLayer.fromFunctionZIO { hc => 
      val config = hc.get
      ZIO.attempt(new RedisClient(config.host, config.port))
    }
  
  lazy val redisConfigLayer = configLayer.narrow(_.redisConfig)
  lazy val queueConfigLayer = configLayer.narrow(_.queueConfig)
  val redisServiceLayer =  (queueConfigLayer ++ (redisConfigLayer >>> redisClientLayer)) >>> RedisServiceLive.layer
}

