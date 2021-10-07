package com.jk.crawler.service

import com.jk.common.model.config.CrawlerConfig
import com.jk.common.model.config.CrawlerUrlRule
import com.jk.common.model.domain.CrawledSite
import zio._

sealed trait CrawlerError
final case object IOError extends CrawlerError
final case class ViolatesRule(rule: CrawlerUrlRule) extends CrawlerError
final case object RetriesExceeded extends CrawlerError


object crawler {
    trait CrawlerService {
        def crawl(url: String): IO[CrawlerError, CrawledSite]
    }

    case class CrawlerServiceLive(crawlerConfig: CrawlerConfig) extends CrawlerService {
        def crawl(url: String): IO[CrawlerError, CrawledSite] = validateRules(url) match {
            case Some(value) => IO.fail(ViolatesRule(value))
            case None => downloadFileFromUrl(url).map(CrawledSite(url,_)).retryN(crawlerConfig.retries).orElseFail(RetriesExceeded)
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
    object CrawlerServiceLive {
        val layer : URLayer[Has[CrawlerConfig], Has[CrawlerService]] = 
            (CrawlerServiceLive(_)).toLayer
    }

    def crawl(url: String): ZIO[Has[CrawlerService], CrawlerError, CrawledSite] = 
        ZIO.serviceWith[CrawlerService](_.crawl(url))
}