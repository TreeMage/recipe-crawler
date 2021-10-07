package com.jk.common.model.config

import zio.config._
import ConfigDescriptor._

case class CrawlerConfig(retries: Int, urlRules: List[CrawlerUrlRule])

object CrawlerConfig {

    val descriptor = (int("retries") |@| list("rules")(CrawlerUrlRule.descriptor))(CrawlerConfig.apply, CrawlerConfig.unapply)
}