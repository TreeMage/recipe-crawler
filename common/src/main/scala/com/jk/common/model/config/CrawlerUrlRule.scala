package com.jk.common.model.config

import zio.config._
import ConfigDescriptor._

case class CrawlerUrlRule(pattern: String, shouldMatch: Boolean)

object CrawlerUrlRule {
    val descriptor = (string("pattern") |@| boolean("match"))(CrawlerUrlRule.apply, CrawlerUrlRule.unapply)
}