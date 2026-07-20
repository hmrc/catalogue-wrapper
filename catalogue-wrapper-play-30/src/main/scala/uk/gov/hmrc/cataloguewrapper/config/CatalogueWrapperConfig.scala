/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.cataloguewrapper.config

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class CatalogueWrapperConfig @Inject() (
    servicesConfig: ServicesConfig,
    configuration: Configuration
):
  val menuBarBaseUrl: String =
    servicesConfig.baseUrl("menu-bar")

  val quickSearchLimit: Int =
    configuration.getOptional[Int]("catalogue-wrapper.quick-search.default-limit").getOrElse(20)

  val quickSearchMinTermLength: Int =
    configuration.getOptional[Int]("catalogue-wrapper.quick-search.min-term-length").getOrElse(3)

  val quickSearchMaxTerms: Int =
    configuration.getOptional[Int]("catalogue-wrapper.quick-search.max-terms").getOrElse(5)

  val quickSearchRefreshThrottleSeconds: Long =
    configuration
      .getOptional[Long]("catalogue-wrapper.quick-search.refresh-throttle-seconds")
      .getOrElse(30L)

  val assetsPrefix: String =
    configuration.getOptional[String]("catalogue-wrapper.assets-prefix").getOrElse("/catalogue-wrapper/assets")

  val quickSearchPath: String =
    configuration.getOptional[String]("catalogue-wrapper.quick-search-path").getOrElse("/catalogue-wrapper/quicksearch")
