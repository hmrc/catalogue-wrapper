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
import uk.gov.hmrc.cataloguewrapper.models.{BannerMenu, MenuDropdown, MenuLink, SearchTerm}
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

  "getMenu" should {
    "call /menu-bar/menu and decode BannerMenu" in {
      stubFor(
        get(urlEqualTo("/menu-bar/menu"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(Json.toJson(sampleMenu).toString())
          )
      )

      connector.getMenu().futureValue shouldBe sampleMenu
      verify(getRequestedFor(urlEqualTo("/menu-bar/menu")))
    }
  }

  "search" should {
    "call /menu-bar/quicksearch with query and limit parameters" in {
      val results = Seq(SearchTerm("service", "foo-service", "/services/foo-service"))
      stubFor(
        get(urlPathEqualTo("/menu-bar/quicksearch"))
          .withQueryParam("query", equalTo("foo"))
          .withQueryParam("limit", equalTo("10"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(Json.toJson(results).toString())
          )
      )

      connector.search("foo", 10).futureValue shouldBe results
    }

    "return empty sequence when backend returns empty array" in {
      stubFor(
        get(urlPathEqualTo("/menu-bar/quicksearch"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody("[]")
          )
      )

      connector.search("xyz").futureValue shouldBe Seq.empty
    }
  }
