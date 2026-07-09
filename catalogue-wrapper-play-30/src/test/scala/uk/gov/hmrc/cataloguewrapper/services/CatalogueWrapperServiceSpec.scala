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
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import uk.gov.hmrc.cataloguewrapper.config.CatalogueWrapperConfig
import uk.gov.hmrc.cataloguewrapper.models.{BannerMenu, NavigationData, TopMenu}
import uk.gov.hmrc.cataloguewrapper.views.html.{CatalogueScripts, CatalogueStylesheets, StandardCatalogueLayout}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CatalogueWrapperServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures:

  private val mockNavCache = mock[CatalogueNavigationCache]
  private val mockConfig   = mock[CatalogueWrapperConfig]

  private val layout = new StandardCatalogueLayout(
    new CatalogueStylesheets(mockConfig),
    new CatalogueScripts(mockConfig)
  )

  private val service = new CatalogueWrapperService(mockNavCache, mockConfig, layout)

  private val sampleMenu = BannerMenu(
    brand = TopMenu(name = "MDTP", id = "brand", href = Some("/")),
    topLevelLinks = Seq.empty,
    dropdowns = Seq.empty
  )

  private val sampleNav = NavigationData(menu = sampleMenu, searchIndex = Seq.empty)

  given HeaderCarrier = HeaderCarrier()
  given RequestHeader = FakeRequest()
  given Messages      = stubMessages()

  "standardCatalogueLayout" should {
    "fetch navigation from cache and render the layout" in {
      when(mockNavCache.refreshOrCached()(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleNav))
      when(mockConfig.quickSearchPath)
        .thenReturn("/catalogue-wrapper/quicksearch")
      when(mockConfig.quickSearchMinTermLength)
        .thenReturn(3)
      when(mockConfig.assetsPrefix)
        .thenReturn("/catalogue-wrapper/assets")

      val result = service.standardCatalogueLayout(Html("<p>content</p>"), Some("Test Page")).futureValue
      result.body should include("MDTP - Test Page")
      result.body should include("/catalogue-wrapper/quicksearch")
      result.body should include("<p>content</p>")
    }

    "render with empty navigation when backend is unavailable and cache is empty" in {
      when(mockNavCache.refreshOrCached()(any[HeaderCarrier]))
        .thenReturn(Future.successful(NavigationData.empty))
      when(mockConfig.quickSearchPath)
        .thenReturn("/catalogue-wrapper/quicksearch")
      when(mockConfig.quickSearchMinTermLength)
        .thenReturn(3)
      when(mockConfig.assetsPrefix)
        .thenReturn("/catalogue-wrapper/assets")

      val result =
        service.standardCatalogueLayout(Html("<p>content</p>"), Some("Test Page")).futureValue
      result.body should include("MDTP - Test Page")
      result.body should include("/catalogue-wrapper/quicksearch")
      result.body should include("<p>content</p>")
    }

    "render using navigation returned by the cache" in {
      when(mockNavCache.refreshOrCached()(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleNav))
      when(mockConfig.quickSearchPath)
        .thenReturn("/catalogue-wrapper/quicksearch")
      when(mockConfig.quickSearchMinTermLength)
        .thenReturn(3)
      when(mockConfig.assetsPrefix)
        .thenReturn("/catalogue-wrapper/assets")

      val result = service.standardCatalogueLayout(Html("<p>cached</p>"), Some("Stale Page")).futureValue
      result.body should include("MDTP - Stale Page")
      result.body should include("<p>cached</p>")
    }
  }

  "catalogueMenuBar" should {
    "fetch navigation from cache and render only the navbar" in {
      when(mockNavCache.refreshOrCached()(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleNav))
      when(mockConfig.quickSearchPath)
        .thenReturn("/catalogue-wrapper/quicksearch")
      when(mockConfig.quickSearchMinTermLength)
        .thenReturn(3)

      val result = service.catalogueMenuBar(activeItemId = Some("repos")).futureValue
      result.body should include("MDTP")
      result.body should include("/catalogue-wrapper/quicksearch")
      result.body should not include "<html"
    }
  }

  "catalogueMenuBarWithMenu" should {
    "render only the navbar synchronously with supplied menu" in {
      when(mockConfig.quickSearchPath)
        .thenReturn("/catalogue-wrapper/quicksearch")
      when(mockConfig.quickSearchMinTermLength)
        .thenReturn(3)

      val result = service.catalogueMenuBarWithMenu(sampleMenu, activeItemId = Some("repos"))
      result.body should include("MDTP")
      result.body should not include "<html"
    }

    "use # when a menu link href is missing" in {
      when(mockConfig.quickSearchPath)
        .thenReturn("/catalogue-wrapper/quicksearch")
      when(mockConfig.quickSearchMinTermLength)
        .thenReturn(3)

      val menuWithMissingHref = sampleMenu.copy(
        topLevelLinks = Seq(TopMenu(name = "Repositories", id = "repos", href = None))
      )

      val result = service.catalogueMenuBarWithMenu(menuWithMissingHref, activeItemId = Some("repos"))
      result.body should include("href=\"#\"")
    }
  }

  "standardCatalogueLayoutWithMenu" should {
    "render layout synchronously with supplied menu" in {
      when(mockConfig.quickSearchPath)
        .thenReturn("/catalogue-wrapper/quicksearch")
      when(mockConfig.quickSearchMinTermLength)
        .thenReturn(3)
      when(mockConfig.assetsPrefix)
        .thenReturn("/catalogue-wrapper/assets")

      val result = service.standardCatalogueLayoutWithMenu(
        menu = sampleMenu,
        content = Html("<p>content</p>"),
        pageTitle = Some("Sync Test")
      )
      result.body should include("MDTP - Sync Test")
      result.body should include("<p>content</p>")
    }
  }
