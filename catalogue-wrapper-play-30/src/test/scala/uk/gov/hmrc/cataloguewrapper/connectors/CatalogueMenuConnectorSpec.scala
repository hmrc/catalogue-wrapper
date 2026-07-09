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
import uk.gov.hmrc.cataloguewrapper.models.{BannerMenu, MenuDropdown, NavigationData, Page, SearchTerm, TopMenu}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.play.audit.http.HttpAuditing

class CatalogueMenuConnectorSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with WireMockSupport
    with MockitoSugar
    with TestData:

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

  val sampleMenu: BannerMenu = BannerMenu(
    brand = TopMenu(
      name = "MDTP",
      id = "mdtp",
      href = Some("/")
    ),
    topLevelLinks = Seq(
      TopMenu(name = "Users", id = "users", href = Some("/users")),
      TopMenu(name = "Teams", id = "teams", href = Some("/teams")),
      TopMenu(name = "Repositories", id = "repositories", href = Some("/repositories")),
      TopMenu(name = "Deployments", id = "deployments", href = None),
      TopMenu(name = "Shuttering", id = "shuttering", href = None),
      TopMenu(name = "Health", id = "health", href = None),
      TopMenu(name = "Explore", id = "explore", href = None),
      TopMenu(name = "Docs", id = "docs", href = None)
    ),
    dropdowns = Seq(
      MenuDropdown(
        "users",
        "Users",
        Some("/users"),
        Seq(
          Page("Create a User", "create-user", "/create-user"),
          Page("Create a Service User", "create-service-user", "/create-service-user"),
          Page("Offboard Users", "offboard-users", "/offboard-users")
          )
      ),
      MenuDropdown(
        "deployments",
        "Deployments",
        None,
        Seq(
          Page("Deploy Service", "deploy-service", "/deploy-service"),
          Page("Deployment Events", "deployment-events", "/deployments/production"),
          Page("Version Timeline", "deployment-timeline", "/deployment-timeline"),
          Page("What's Running Where", "whats-running-where", "/whats-running-where")
          )
      ),
      MenuDropdown(
        "shuttering",
        "Shuttering",
        None,
        Seq(
          Page("Shutter Overview - Frontend", "shutter-overview-frontend", "/shuttering-overview/frontend"),
          Page("Shutter Overview - Api", "shutter-overview-api", "/shuttering-overview/api"),
          Page("Shutter Overview - Rate", "shutter-overview-rate", "/shuttering-overview/rate"),
          Page("Shutter Events", "shutter-events", "/shutter-events")
          )
      ),
      MenuDropdown(
        "health",
        "Health",
        None,
        Seq(
          Page("Platform Initiatives", "platform-initiatives", "/platform-initiatives"),
          Page("Bobby Rules", "bobby-rules", "/bobbyrules"),
          Page("Bobby Violations", "bobby-violations", "/bobby-violations"),
          Page("Leak Detection - Rules", "leak-detection-rules", "/leak-detection"),
          Page("Leak Detection - Repositories", "leak-detection-repositories", "/leak-detection/repositories?includeViolations=true"),
          Page("Vulnerabilities", "vulnerabilities", "/vulnerabilities?curationStatus=ACTION_REQUIRED"),
          Page("Vulnerabilities - Services", "vulnerabilities-services", "/vulnerabilities/services"),
          Page("Vulnerabilities - Timeline", "vulnerabilities-timeline", "/vulnerabilities/timeline?curationStatus=ACTION_REQUIRED"),
          Page("PR-Commenter Recommendations", "pr-commenter-recommendations", "/pr-commenter/recommendations"),
          Page("Health Metrics - Timeline", "health-metrics-timeline", "/health-metrics/timeline"),
          Page("Operational Metrics", "operational-metrics", "/health-metrics")
          )
      ),
      MenuDropdown(
        "explore",
        "Explore",
        None,
        Seq(
          Page("Dependency Explorer", "dependency-explorer", "/dependencyexplorer"),
          Page("JDK Explorer", "jdk-explorer", "/jdkexplorer"),
          Page("SBT Explorer", "sbt-explorer", "/sbtexplorer"),
          Page("Search by URL", "search-by-url", "/search#"),
          Page("Search Config", "search-config", "/config/search"),
          Page("Search Commissioning State", "search-commissioning-state", "/commissioning-state/search"),
          Page("Service Metrics", "service-metrics", "/service-metrics"),
          Page("Test Results", "test-results", "/tests"),
          Page("Config Warnings", "config-warnings", "/config/warnings/search"),
          Page("Cost Explorer", "cost-explorer", "/cost-explorer"),
          Page("Service Provision", "service-provision", "/service-provision")
          )
      ),
      MenuDropdown(
        "docs",
        "Docs",
        None,
        Seq(
          Page(
            name = "MDTP Handbook",
            id = "mdtp-handbook",
            href = Some("https://docs.tax.service.gov.uk/mdtp-handbook/"),
            external = true
          ),
          Page(
            name = "Blog Posts",
            id = "blog-posts",
            href = Some(
              "https://confluence.tools.tax.service.gov.uk/dosearchsite.action?cql=(label=catalogue and type=blogpost) order by created desc"
            ),
            external = true
          )
        )
      )
    )
  )

  val sampleSearchTerms: Seq[SearchTerm] = Seq(SearchTerm("service", "foo-service", "/services/foo-service"))

  "getNavigationData" should {
    "call /menu-bar/menu and /menu-bar/search-index and combine into NavigationData" in {
      stubFor(
        get(urlEqualTo("/catalogue-config/menu-bar/menu"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(rawJson)
          )
      )
      stubFor(
        get(urlEqualTo("/catalogue-config/menu-bar/search-index"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(Json.toJson(sampleSearchTerms).toString())
          )
      )

      connector.getNavigationData().futureValue shouldBe NavigationData(sampleMenu, sampleSearchTerms)
    }

    "decode NavigationData with an empty search index" in {
      stubFor(
        get(urlEqualTo("/catalogue-config/menu-bar/menu"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(Json.toJson(sampleMenu).toString())
          )
      )
      stubFor(
        get(urlEqualTo("/catalogue-config/menu-bar/search-index"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody("[]")
          )
      )

      connector.getNavigationData().futureValue shouldBe NavigationData(sampleMenu, Seq.empty)
    }
  }
