package com.jk.crawler.service

import zio.Task
import com.jk.common.model.config.CrawlerConfig
import zio.UIO
import zio.IO
import com.jk.common.model.config.CrawlerUrlRule
import zio.ZManaged
import zio.Schedule
import zio.ZIO
import zio.Has
import zio.Clock

sealed trait CrawlerError
final case object IOError extends CrawlerError
final case class ViolatesRule(rule: CrawlerUrlRule) extends CrawlerError
final case object RetriesExceeded extends CrawlerError


object crawler {
    trait CrawlerService {
        def crawl(url: String): ZIO[Has[Clock],CrawlerError, String]
    }

    case class CrawlerServiceLive(crawlerConfig: CrawlerConfig) extends CrawlerService {
        def crawl(url: String): ZIO[Has[Clock],CrawlerError, String] = validateRules(url) match {
            case Some(value) => IO.fail(ViolatesRule(value))
            case None => downloadFileFromUrl(url).retryOrElse(Schedule.recurs(crawlerConfig.retries - 1), (e, _: Any) => ZIO.fail(RetriesExceeded))
        }

        private def validateRules(url: String): Option[CrawlerUrlRule] =
            crawlerConfig.urlRules.filter { rule =>
                val doesMatch = url.matches(rule.pattern)
                !(doesMatch && rule.shouldMatch || !doesMatch && !rule.shouldMatch)
            }.headOption
        
        private def downloadFileFromUrl(url: String): IO[CrawlerError, String] = 
            IO.acquireReleaseWith(IO.attempt(scala.io.Source.fromURL(url)).orElseFail(IOError))(r => IO.succeed(r.close())) {
                src => IO.blocking(IO.succeed(src.mkString))
            }
    
    }
}