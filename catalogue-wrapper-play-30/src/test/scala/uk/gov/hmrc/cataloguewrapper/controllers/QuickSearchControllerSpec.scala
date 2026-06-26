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

package uk.gov.hmrc.cataloguewrapper.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{clearInvocations, verify, verifyNoInteractions, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.cataloguewrapper.config.CatalogueWrapperConfig
import uk.gov.hmrc.cataloguewrapper.models.{BannerMenu, MenuLink, NavigationData, SearchTerm}
import uk.gov.hmrc.cataloguewrapper.search.SearchIndex
import uk.gov.hmrc.cataloguewrapper.services.CatalogueNavigationCache
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class QuickSearchControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures:

  private val mockSearchIndex = mock[SearchIndex]
  private val mockNavCache    = mock[CatalogueNavigationCache]
  private val mockConfig      = mock[CatalogueWrapperConfig]

  when(mockConfig.quickSearchLimit).thenReturn(20)
  when(mockConfig.quickSearchMinTermLength).thenReturn(3)
  when(mockConfig.quickSearchMaxTerms).thenReturn(5)

  private val controller = new QuickSearchController(
    stubMessagesControllerComponents(),
    mockSearchIndex,
    mockNavCache,
    mockConfig
  )

  private val sampleNav = NavigationData(
    menu = BannerMenu(brand = MenuLink("brand", "MDTP", "/"), topLevelLinks = Seq.empty, dropdowns = Seq.empty),
    searchIndex = Seq(SearchTerm("service", "foo-service", "/services/foo-service"))
  )

  "search" should {
    "search the local SearchIndex when it is already populated" in {
      val results = Seq(SearchTerm("service", "foo-service", "/services/foo-service"))
      when(mockSearchIndex.isPopulated).thenReturn(true)
      when(mockSearchIndex.search(Seq("foo"))).thenReturn(results)

      val result = controller.search("foo", None)(FakeRequest())
      status(result) shouldBe OK
      contentType(result) shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(results)
      verifyNoInteractions(mockNavCache)
    }

    "warm the cache via CatalogueNavigationCache when SearchIndex is cold" in {
      val results = Seq(SearchTerm("service", "foo-service", "/services/foo-service"))
      when(mockSearchIndex.isPopulated).thenReturn(false)
      when(mockNavCache.refreshOrCached()(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleNav))
      when(mockSearchIndex.search(Seq("foo"))).thenReturn(results)

      val result = controller.search("foo", None)(FakeRequest())
      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(results)
      verify(mockNavCache).refreshOrCached()(any[HeaderCarrier])
    }

    "respect the limit parameter" in {
      val allResults = (1 to 10).map(i => SearchTerm("service", s"svc-$i", s"/services/svc-$i"))
      when(mockSearchIndex.isPopulated).thenReturn(true)
      when(mockSearchIndex.search(Seq("svc"))).thenReturn(allResults)

      val result = controller.search("svc", Some(3))(FakeRequest())
      status(result) shouldBe OK
      contentAsJson(result).as[Seq[SearchTerm]] should have length 3
    }

    "use config.quickSearchLimit when no limit is provided" in {
      val results =
        (1 to 30).map(i => SearchTerm("service", s"bar-service-$i", s"/services/bar-service-$i"))
      when(mockSearchIndex.isPopulated).thenReturn(true)
      when(mockSearchIndex.search(Seq("bar"))).thenReturn(results)

      val result = controller.search("bar", None)(FakeRequest())
      status(result) shouldBe OK
      contentAsJson(result).as[Seq[SearchTerm]] should have length 20
    }

    "return empty array for query terms shorter than min-term-length after normalisation" in {
      clearInvocations(mockSearchIndex)
      // "a-b" has length 3 raw but normalises to "ab" (length 2) — must be filtered
      val result = controller.search("a-b", None)(FakeRequest())
      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.arr()
      verifyNoInteractions(mockSearchIndex)
    }

    "return empty array when SearchIndex returns empty" in {
      when(mockSearchIndex.isPopulated).thenReturn(true)
      when(mockSearchIndex.search(Seq("zzz"))).thenReturn(Seq.empty)

      val result = controller.search("zzz", None)(FakeRequest())
      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.arr()
    }

    "apply AND semantics by passing all query tokens to SearchIndex" in {
      val results = Seq(SearchTerm("service", "foo-bar", "/services/foo-bar"))
      when(mockSearchIndex.isPopulated).thenReturn(true)
      when(mockSearchIndex.search(Seq("foo", "bar"))).thenReturn(results)

      val result = controller.search("foo bar", None)(FakeRequest())
      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(results)
      verify(mockSearchIndex).search(Seq("foo", "bar"))
    }
  }
