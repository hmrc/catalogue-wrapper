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

package uk.gov.hmrc.cataloguewrapper.services

import play.api.Logging
import uk.gov.hmrc.cataloguewrapper.connectors.CatalogueMenuConnector
import uk.gov.hmrc.cataloguewrapper.models.{BannerMenu, NavigationData, SearchTerm}
import uk.gov.hmrc.cataloguewrapper.search.SearchIndex
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CatalogueNavigationCache @Inject() (
    connector: CatalogueMenuConnector,
    searchIndex: SearchIndex
)(implicit ec: ExecutionContext)
    extends Logging:

  private final case class CacheEntry(
      data: NavigationData,
      updatedAt: Instant
  )

  private val cache =
    new AtomicReference[Option[CacheEntry]](None)

  def refreshOrCached()(implicit hc: HeaderCarrier): Future[NavigationData] =
    connector
      .getNavigationData()
      .map { fresh =>
        updateCache(fresh)
        fresh
      }
      .recoverWith { case error =>
        cache.get() match
          case Some(entry) =>
            logger.warn("Using cached catalogue navigation data because catalogue-navigation is unavailable", error)
            Future.successful(entry.data)

          case None =>
            logger.error("No cached catalogue navigation data is available", error)
            Future.failed(error)
      }

  def cachedMenu: Option[BannerMenu] =
    cache.get().map(_.data.menu)

  def cachedSearchTerms: Seq[SearchTerm] =
    cache.get().map(_.data.searchIndex).getOrElse(Seq.empty)

  private def updateCache(data: NavigationData): Unit =
    cache.set(Some(CacheEntry(data, Instant.now())))
    searchIndex.replaceAll(data.searchIndex)
