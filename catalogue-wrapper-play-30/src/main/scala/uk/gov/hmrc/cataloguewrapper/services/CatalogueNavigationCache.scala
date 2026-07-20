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
import uk.gov.hmrc.cataloguewrapper.config.CatalogueWrapperConfig
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
    searchIndex: SearchIndex,
    config: CatalogueWrapperConfig
)(implicit ec: ExecutionContext)
    extends Logging:

  private final case class CacheEntry(
      data: NavigationData,
      dataUpdatedAt: Instant,
      lastRefreshAttemptAt: Instant,
      loadedFromBackend: Boolean
  )

  private val cache =
    new AtomicReference[Option[CacheEntry]](None)

  def refreshOrCached()(implicit hc: HeaderCarrier): Future[NavigationData] =
    connector
      .getNavigationData()
      .map { fresh =>
        updateCache(fresh, loadedFromBackend = true)
        fresh
      }
      .recover { case error =>
        cache.get() match
          case Some(entry) =>
            logger.warn(
              "Using cached catalogue navigation data because catalogue-navigation is unavailable",
              error
            )
            recordRefreshAttempt()
            entry.data

          case None =>
            logger.warn(
              "No cached catalogue navigation data is available; using empty catalogue navigation data",
              error
            )
            updateCache(NavigationData.empty, loadedFromBackend = false)
            NavigationData.empty
      }

  /** Returns true when the controller should attempt a backend refresh before serving a quicksearch result.
    *
    * False (no refresh needed) when:
    *   - The cache was loaded from the backend and contains a non-empty search index.
    *
    * True (refresh allowed) when:
    *   - The cache is empty (cold pod).
    *   - The cache holds fallback/empty-search data and the throttle interval has elapsed since the last refresh
    *     attempt.
    */
  def shouldRefreshForSearch(): Boolean =
    shouldRefreshForSearch(Instant.now())

  def shouldRefreshForSearch(now: Instant): Boolean =
    cache.get() match
      case None =>
        true

      case Some(entry) if entry.loadedFromBackend && entry.data.searchIndex.nonEmpty =>
        false

      case Some(entry) =>
        val nextAllowedRefresh =
          entry.lastRefreshAttemptAt.plusSeconds(config.quickSearchRefreshThrottleSeconds)
        !now.isBefore(nextAllowedRefresh)

  def cachedMenu: Option[BannerMenu] =
    cache.get().map(_.data.menu)

  def cachedSearchTerms: Seq[SearchTerm] =
    cache.get().map(_.data.searchIndex).getOrElse(Seq.empty)

  private def updateCache(data: NavigationData, loadedFromBackend: Boolean): Unit =
    val now = Instant.now()
    cache.set(
      Some(
        CacheEntry(
          data = data,
          dataUpdatedAt = now,
          lastRefreshAttemptAt = now,
          loadedFromBackend = loadedFromBackend
        )
      )
    )
    searchIndex.replaceAll(data.searchIndex)

  private def recordRefreshAttempt(): Unit =
    val current = cache.get()
    cache.compareAndSet(
      current,
      current.map(_.copy(lastRefreshAttemptAt = Instant.now()))
    )
