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

package uk.gov.hmrc.cataloguewrapper.connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.cataloguewrapper.models.{BannerMenu, MenuDropdown, MenuLink, NavigationData, SearchTerm}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.play.audit.http.HttpAuditing

class CatalogueMenuConnectorSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with WireMockSupport
    with MockitoSugar:

  lazy val app: Application =
    GuiceApplicationBuilder()
      .configure(
        "microservice.services.menu-bar.host"     -> wireMockHost,
        "microservice.services.menu-bar.port"     -> wireMockPort,
        "microservice.services.menu-bar.protocol" -> "http"
      )
      .bindings(
        bind[HttpAuditing].toInstance(mock[HttpAuditing]),
        bind[HttpClientV2].toProvider[uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider]
      )
      .build()

  private lazy val connector = app.injector.instanceOf[CatalogueMenuConnector]
  given HeaderCarrier        = HeaderCarrier()

  val sampleMenu = BannerMenu(
    brand = MenuLink("brand", "MDTP", "/"),
    topLevelLinks = Seq(MenuLink("repos", "Repositories", "/repositories")),
    dropdowns = Seq(MenuDropdown("explore", "Explore", Seq(MenuLink("teams", "Teams", "/teams"))))
  )

  val sampleNavData = NavigationData(
    menu = sampleMenu,
    searchIndex = Seq(SearchTerm("service", "foo-service", "/services/foo-service"))
  )

  "getNavigationData" should {
    "call /menu-bar/navigation-data and decode NavigationData containing menu + searchIndex" in {
      stubFor(
        get(urlEqualTo("/menu-bar/navigation-data"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(Json.toJson(sampleNavData).toString())
          )
      )

      connector.getNavigationData().futureValue shouldBe sampleNavData
      verify(getRequestedFor(urlEqualTo("/menu-bar/navigation-data")))
    }

    "decode NavigationData with an empty searchIndex" in {
      val navDataNoSearch = sampleNavData.copy(searchIndex = Seq.empty)
      stubFor(
        get(urlEqualTo("/menu-bar/navigation-data"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(Json.toJson(navDataNoSearch).toString())
          )
      )

      connector.getNavigationData().futureValue shouldBe navDataNoSearch
    }
  }
