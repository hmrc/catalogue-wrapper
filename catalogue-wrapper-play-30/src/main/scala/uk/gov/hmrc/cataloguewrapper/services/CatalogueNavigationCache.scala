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
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

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

  private val cache = new AtomicReference[Option[CacheEntry]](None)
  private var searchRefresh: Option[Future[Seq[SearchTerm]]] = None
  private val emptyEntry = CacheEntry(NavigationData.empty, Instant.EPOCH, Instant.EPOCH, false)

  def refreshOrCached()(implicit hc: HeaderCarrier): Future[NavigationData] =
    refreshSearch()
    connector.getMenu().map { menu =>
      cache.updateAndGet { current =>
        val entry = current.getOrElse(emptyEntry)
        Some(entry.copy(data = entry.data.copy(menu = menu)))
      }.get.data
    }.recover { case NonFatal(error) =>
      logger.warn("Using cached catalogue menu because catalogue-config is unavailable", error)
      cache.get().map(_.data).getOrElse(NavigationData.empty)
    }

  def refreshSearch()(implicit hc: HeaderCarrier): Future[Seq[SearchTerm]] = synchronized {
    searchRefresh match
      case Some(pending) => pending
      case None if !shouldRefreshForSearch() => Future.successful(cachedSearchTerms)
      case None =>
        val promise = Promise[Seq[SearchTerm]]()
        searchRefresh = Some(promise.future)
        Try(connector.getSearchIndex()).fold(Future.failed, identity).onComplete { result =>
          synchronized {
            val now = Instant.now()
            result match
              case Success(terms) =>
                searchIndex.replaceAll(terms)
                cache.updateAndGet { current =>
                  val entry = current.getOrElse(emptyEntry)
                  Some(entry.copy(data = entry.data.copy(searchIndex = terms),
                    dataUpdatedAt = now, lastRefreshAttemptAt = now, loadedFromBackend = true))
                }
              case Failure(error) =>
                logger.warn("Using cached catalogue search index because catalogue-config is unavailable", error)
                cache.updateAndGet { current =>
                  Some(current.getOrElse(emptyEntry).copy(lastRefreshAttemptAt = now))
                }
            searchRefresh = None
            promise.success(cachedSearchTerms)
          }
        }
        promise.future
  }

  def shouldRefreshForSearch(): Boolean = shouldRefreshForSearch(Instant.now())

  def shouldRefreshForSearch(now: Instant): Boolean =
    cache.get() match
      case None => true
      case Some(entry) =>
        val fresh = entry.loadedFromBackend && now.isBefore(entry.dataUpdatedAt.plus(config.searchCacheTtl))
        val retryAllowed = !now.isBefore(entry.lastRefreshAttemptAt.plusSeconds(config.quickSearchRefreshThrottleSeconds))
        !fresh && retryAllowed

  def cachedMenu: Option[BannerMenu] = cache.get().map(_.data.menu)

  def cachedSearchTerms: Seq[SearchTerm] = cache.get().map(_.data.searchIndex).getOrElse(Seq.empty)
