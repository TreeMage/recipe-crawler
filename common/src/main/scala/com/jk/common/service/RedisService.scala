package com.jk.common.service


import zio._
import com.jk.common.model.config.RedisConfig
import com.redis.RedisClient
import com.jk.common.model.config.QueueConfig

object redis {
    trait RedisSerivce {
        def pushToUrlQueue(url: String): Task[Unit]
        def popFromUrlQueue(): Task[String]
    }

    case class RedisServiceLive(config: QueueConfig, client: RedisClient) extends RedisSerivce {
        def pushToUrlQueue(url: String): Task[Unit] = 
            ZIO.attempt(client.lpush(config.discoveredUrlQueue, url))
                .mapBoth(
                    err => new RuntimeException("Failed to push URL to queue.", err),
                    _ => ()
                )
        
        def popFromUrlQueue(): Task[String] = 
            ZIO.attempt(client.brpop(0,config.discoveredUrlQueue))
                .filterOrDieMessage {
                    case Some(_) => true
                    case _ => false
                }("Failed to pop URL from queue.")
                .map(_.get._2)
                
    }

    object RedisServiceLive {
        val layer: URLayer[Has[QueueConfig] with Has[RedisClient], Has[RedisSerivce]] =
            (RedisServiceLive(_,_)).toLayer
    }

    def pushToUrlQueue(url: String): ZIO[Has[RedisSerivce], Throwable, Unit] =
        ZIO.serviceWith[RedisSerivce](_.pushToUrlQueue(url))

    def popFromUrlQueue(): ZIO[Has[RedisSerivce], Throwable, String] =
        ZIO.serviceWith[RedisSerivce](_.popFromUrlQueue)
}