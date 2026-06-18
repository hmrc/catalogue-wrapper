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
import uk.gov.hmrc.cataloguewrapper.connectors.CatalogueMenuConnector
import uk.gov.hmrc.cataloguewrapper.models.{BannerMenu, MenuLink}
import uk.gov.hmrc.cataloguewrapper.views.html.{CatalogueScripts, CatalogueStylesheets, StandardCatalogueLayout}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CatalogueWrapperServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures:

  private val mockConnector = mock[CatalogueMenuConnector]
  private val mockConfig    = mock[CatalogueWrapperConfig]

  private val layout = new StandardCatalogueLayout(
    new CatalogueStylesheets(mockConfig),
    new CatalogueScripts(mockConfig)
  )

  private val service = new CatalogueWrapperService(mockConnector, mockConfig, layout)

  private val sampleMenu = BannerMenu(
    brand = MenuLink("brand", "MDTP", "/"),
    topLevelLinks = Seq.empty,
    dropdowns = Seq.empty
  )

  given HeaderCarrier = HeaderCarrier()
  given RequestHeader = FakeRequest()
  given Messages      = stubMessages()

  "standardCatalogueLayout" should {
    "fetch menu from connector and render the layout" in {
      when(mockConnector.getMenu()(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleMenu))
      when(mockConfig.quickSearchPath)
        .thenReturn("/catalogue-wrapper/quicksearch")
      when(mockConfig.assetsPrefix)
        .thenReturn("/catalogue-wrapper/assets")

      val result = service.standardCatalogueLayout(Html("<p>content</p>"), Some("Test Page")).futureValue
      result.body should include("MDTP - Test Page")
      result.body should include("/catalogue-wrapper/quicksearch")
      result.body should include("<p>content</p>")
    }

    "fail the future if connector fails" in {
      when(mockConnector.getMenu()(any[HeaderCarrier]))
        .thenReturn(Future.failed(RuntimeException("menu-bar-backend unavailable")))
      when(mockConfig.quickSearchPath)
        .thenReturn("/catalogue-wrapper/quicksearch")

      service
        .standardCatalogueLayout(Html("<p>content</p>"), Some("Test Page"))
        .failed
        .futureValue shouldBe a[RuntimeException]
    }
  }

  "catalogueMenuBar" should {
    "fetch menu from connector and render only the navbar" in {
      when(mockConnector.getMenu()(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleMenu))
      when(mockConfig.quickSearchPath)
        .thenReturn("/catalogue-wrapper/quicksearch")

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

      val result = service.catalogueMenuBarWithMenu(sampleMenu, activeItemId = Some("repos"))
      result.body should include("MDTP")
      result.body should not include "<html"
    }
  }

  "standardCatalogueLayoutWithMenu" should {
    "render layout synchronously with supplied menu" in {
      when(mockConfig.quickSearchPath)
        .thenReturn("/catalogue-wrapper/quicksearch")
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
