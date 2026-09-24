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

import java.time.{Duration, Instant}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.cataloguewrapper.config.CatalogueWrapperConfig
import uk.gov.hmrc.cataloguewrapper.connectors.CatalogueMenuConnector
import uk.gov.hmrc.cataloguewrapper.models.{NavigationData, SearchTerm, TopMenu}
import uk.gov.hmrc.cataloguewrapper.search.SearchIndex
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

class CatalogueNavigationCacheSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures:
  given HeaderCarrier = HeaderCarrier()

  private val menu = NavigationData.empty.menu.copy(topLevelLinks = Seq(TopMenu("Teams", "teams", Some("/teams"))))
  private val terms = Seq(SearchTerm("service", "foo-service", "/service/foo-service"))

  private def makeCache(
      connector: CatalogueMenuConnector,
      ttl: Duration = Duration.ofHours(1),
      throttle: Long = 30L,
      index: SearchIndex = new SearchIndex
  ) =
    val config = mock[CatalogueWrapperConfig]
    when(config.searchCacheTtl).thenReturn(ttl)
    when(config.quickSearchRefreshThrottleSeconds).thenReturn(throttle)
    new CatalogueNavigationCache(connector, index, config)

  "Navigation caching" should {
    "return menus without waiting for search and share concurrent search refreshes" in {
      val connector = mock[CatalogueMenuConnector]
      val pending = Promise[Seq[SearchTerm]]()
      when(connector.getMenu()(any[HeaderCarrier])).thenReturn(Future.successful(menu))
      when(connector.getSearchIndex()(any[HeaderCarrier])).thenReturn(pending.future)
      val index = new SearchIndex
      val cache = makeCache(connector, index = index)

      cache.refreshOrCached().futureValue.menu shouldBe menu
      cache.refreshOrCached().futureValue.menu shouldBe menu
      val joined = cache.refreshSearch()
      joined.isCompleted shouldBe false
      pending.success(terms)
      joined.futureValue shouldBe terms
      cache.refreshOrCached().futureValue shouldBe NavigationData(menu, terms)
      index.search(Seq("foo")) shouldBe terms
      verify(connector, times(3)).getMenu()(any[HeaderCarrier])
      verify(connector, times(1)).getSearchIndex()(any[HeaderCarrier])
    }

    "cache successful search responses, including empty results, for one hour" in {
      Seq(terms, Seq.empty[SearchTerm]).foreach { result =>
        val connector = mock[CatalogueMenuConnector]
        when(connector.getSearchIndex()(any[HeaderCarrier])).thenReturn(Future.successful(result))
        val cache = makeCache(connector)

        cache.refreshSearch().futureValue shouldBe result
        cache.refreshSearch().futureValue shouldBe result
        cache.shouldRefreshForSearch(Instant.now().plusSeconds(3590)) shouldBe false
        cache.shouldRefreshForSearch(Instant.now().plusSeconds(3601)) shouldBe true
        verify(connector, times(1)).getSearchIndex()(any[HeaderCarrier])
        verify(connector, never()).getMenu()(any[HeaderCarrier])
      }
    }

    "retain the search index on failure, then replace it after recovery" in {
      val connector = mock[CatalogueMenuConnector]
      when(connector.getSearchIndex()(any[HeaderCarrier])).thenReturn(
        Future.successful(terms), Future.failed(RuntimeException("offline")), Future.successful(Seq.empty))
      val index = new SearchIndex
      val cache = makeCache(connector, ttl = Duration.ZERO, throttle = 0L, index = index)

      cache.refreshSearch().futureValue shouldBe terms
      cache.refreshSearch().futureValue shouldBe terms
      index.search(Seq("foo")) shouldBe terms
      cache.refreshSearch().futureValue shouldBe empty
      index.search(Seq("foo")) shouldBe empty
      verify(connector, times(3)).getSearchIndex()(any[HeaderCarrier])
    }

    "throttle failed cold search refreshes for thirty seconds" in {
      val connector = mock[CatalogueMenuConnector]
      when(connector.getSearchIndex()(any[HeaderCarrier])).thenReturn(Future.failed(RuntimeException("offline")))
      val cache = makeCache(connector)

      cache.shouldRefreshForSearch() shouldBe true
      cache.refreshSearch().futureValue shouldBe empty
      cache.refreshSearch().futureValue shouldBe empty
      cache.shouldRefreshForSearch() shouldBe false
      cache.shouldRefreshForSearch(Instant.now().plusSeconds(31)) shouldBe true
      verify(connector, times(1)).getSearchIndex()(any[HeaderCarrier])
    }

    "retain the existing menu fallback without discarding search data" in {
      val connector = mock[CatalogueMenuConnector]
      when(connector.getMenu()(any[HeaderCarrier])).thenReturn(
        Future.successful(menu), Future.failed(RuntimeException("offline")))
      when(connector.getSearchIndex()(any[HeaderCarrier])).thenReturn(Future.successful(terms))
      val cache = makeCache(connector)

      cache.cachedMenu shouldBe None
      cache.refreshSearch().futureValue shouldBe terms
      cache.refreshOrCached().futureValue shouldBe NavigationData(menu, terms)
      cache.refreshOrCached().futureValue shouldBe NavigationData(menu, terms)
      cache.cachedMenu shouldBe Some(menu)
      cache.shouldRefreshForSearch() shouldBe false
      verify(connector, times(1)).getSearchIndex()(any[HeaderCarrier])
    }

    "keep a newly loaded menu when a pending search refresh completes" in {
      val connector = mock[CatalogueMenuConnector]
      val pending = Promise[Seq[SearchTerm]]()
      when(connector.getSearchIndex()(any[HeaderCarrier])).thenReturn(pending.future)
      when(connector.getMenu()(any[HeaderCarrier])).thenReturn(Future.successful(menu))
      val cache = makeCache(connector)

      cache.refreshOrCached().futureValue.menu shouldBe menu
      val joined = cache.refreshSearch()
      pending.success(terms)
      joined.futureValue shouldBe terms
      cache.cachedMenu shouldBe Some(menu)
      cache.cachedSearchTerms shouldBe terms
    }
  }
