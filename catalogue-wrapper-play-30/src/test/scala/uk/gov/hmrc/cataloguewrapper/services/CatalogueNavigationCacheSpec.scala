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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.cataloguewrapper.config.CatalogueWrapperConfig
import uk.gov.hmrc.cataloguewrapper.connectors.CatalogueMenuConnector
import uk.gov.hmrc.cataloguewrapper.models.{BannerMenu, MenuLink, NavigationData, SearchTerm}
import uk.gov.hmrc.cataloguewrapper.search.SearchIndex
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CatalogueNavigationCacheSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures:

  given HeaderCarrier = HeaderCarrier()

  private val mockConfig = mock[CatalogueWrapperConfig]
  when(mockConfig.quickSearchRefreshThrottleSeconds).thenReturn(30L)

  private val sampleMenu = BannerMenu(
    brand = MenuLink("brand", "MDTP", "/"),
    topLevelLinks = Seq.empty,
    dropdowns = Seq.empty
  )

  private val sampleSearchTerms = Seq(SearchTerm("service", "foo-service", "/services/foo-service"))
  private val sampleNav         = NavigationData(menu = sampleMenu, searchIndex = sampleSearchTerms)

  private def makeCache(
      connector: CatalogueMenuConnector,
      searchIndex: SearchIndex = new SearchIndex,
      config: CatalogueWrapperConfig = mockConfig
  ): CatalogueNavigationCache =
    new CatalogueNavigationCache(connector, searchIndex, config)

  "refreshOrCached" should {
    "refresh and return the latest NavigationData from the connector" in {
      val connector = mock[CatalogueMenuConnector]
      when(connector.getNavigationData()(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleNav))

      val cache = makeCache(connector)
      cache.refreshOrCached().futureValue shouldBe sampleNav
    }

    "update the SearchIndex when refresh succeeds" in {
      val connector   = mock[CatalogueMenuConnector]
      val searchIndex = mock[SearchIndex]
      when(connector.getNavigationData()(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleNav))

      val cache = makeCache(connector, searchIndex)
      cache.refreshOrCached().futureValue

      verify(searchIndex).replaceAll(sampleSearchTerms)
    }

    "return cached data when backend fails after a prior success" in {
      val connector = mock[CatalogueMenuConnector]
      when(connector.getNavigationData()(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleNav))
        .thenReturn(Future.failed(RuntimeException("backend down")))

      val cache = makeCache(connector)
      cache.refreshOrCached().futureValue // prime the cache
      cache.refreshOrCached().futureValue shouldBe sampleNav
    }

    "return empty navigation data when backend fails and the cache is empty" in {
      val connector   = mock[CatalogueMenuConnector]
      val searchIndex = mock[SearchIndex]
      when(connector.getNavigationData()(any[HeaderCarrier]))
        .thenReturn(Future.failed(RuntimeException("backend down")))

      val cache = makeCache(connector, searchIndex)

      cache.refreshOrCached().futureValue shouldBe NavigationData.empty
      verify(searchIndex).replaceAll(Seq.empty)
    }
  }

  "shouldRefreshForSearch" should {
    "return true when cache is empty (cold pod)" in {
      val connector = mock[CatalogueMenuConnector]
      val cache     = makeCache(connector)

      cache.shouldRefreshForSearch() shouldBe true
    }

    "return false after real backend data with a non-empty search index is loaded" in {
      val connector = mock[CatalogueMenuConnector]
      when(connector.getNavigationData()(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleNav))

      val cache = makeCache(connector)
      cache.refreshOrCached().futureValue

      cache.shouldRefreshForSearch() shouldBe false
    }

    "throttle refresh after fallback empty data is cached" in {
      val connector = mock[CatalogueMenuConnector]
      when(connector.getNavigationData()(any[HeaderCarrier]))
        .thenReturn(Future.failed(RuntimeException("backend down")))

      val cache = makeCache(connector)
      cache.refreshOrCached().futureValue shouldBe NavigationData.empty

      cache.shouldRefreshForSearch() shouldBe false
      cache.shouldRefreshForSearch(Instant.now().plusSeconds(31)) shouldBe true
    }

    "throttle refresh after backend returns a real but empty search index" in {
      val connector      = mock[CatalogueMenuConnector]
      val emptySearchNav = sampleNav.copy(searchIndex = Seq.empty)
      when(connector.getNavigationData()(any[HeaderCarrier]))
        .thenReturn(Future.successful(emptySearchNav))

      val cache = makeCache(connector)
      cache.refreshOrCached().futureValue shouldBe emptySearchNav

      cache.shouldRefreshForSearch() shouldBe false
      cache.shouldRefreshForSearch(Instant.now().plusSeconds(31)) shouldBe true
    }

    "throttle again after a refresh attempt fails for fallback empty data" in {
      val connector = mock[CatalogueMenuConnector]
      when(connector.getNavigationData()(any[HeaderCarrier]))
        .thenReturn(Future.failed(RuntimeException("backend down")))

      val cache = makeCache(connector)
      cache.refreshOrCached().futureValue shouldBe NavigationData.empty

      // throttle has elapsed — a refresh attempt is allowed
      cache.shouldRefreshForSearch(Instant.now().plusSeconds(31)) shouldBe true

      // the retry also fails; lastRefreshAttemptAt is now bumped
      cache.refreshOrCached().futureValue shouldBe NavigationData.empty

      // should not immediately retry again — throttle resets from the failed attempt
      cache.shouldRefreshForSearch() shouldBe false
    }
  }

  "cachedMenu" should {
    "return None before any successful refresh" in {
      val connector = mock[CatalogueMenuConnector]
      val cache     = makeCache(connector)
      cache.cachedMenu shouldBe None
    }

    "return the cached menu after a successful refresh" in {
      val connector = mock[CatalogueMenuConnector]
      when(connector.getNavigationData()(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleNav))

      val cache = makeCache(connector)
      cache.refreshOrCached().futureValue
      cache.cachedMenu shouldBe Some(sampleMenu)
    }
  }

  "cachedSearchTerms" should {
    "return empty before any successful refresh" in {
      val connector = mock[CatalogueMenuConnector]
      val cache     = makeCache(connector)
      cache.cachedSearchTerms shouldBe Seq.empty
    }

    "return the cached search terms after a successful refresh" in {
      val connector = mock[CatalogueMenuConnector]
      when(connector.getNavigationData()(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleNav))

      val cache = makeCache(connector)
      cache.refreshOrCached().futureValue
      cache.cachedSearchTerms shouldBe sampleSearchTerms
    }
  }
