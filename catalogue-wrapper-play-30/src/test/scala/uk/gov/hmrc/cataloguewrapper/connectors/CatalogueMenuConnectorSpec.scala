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

  val sampleMenu = BannerMenu(
    brand = MenuLink("mdtp", "MDTP", Some("/"), external = false),
    topLevelLinks = Seq(
      MenuLink("users", "Users", Some("/users"), external = false),
      MenuLink("teams", "Teams", Some("/teams"), external = false),
      MenuLink("repositories", "Repositories", Some("/repositories"), external = false),
      MenuLink("deployments", "Deployments", None, external = false),
      MenuLink("shuttering", "Shuttering", None, external = false),
      MenuLink("health", "Health", None, external = false),
      MenuLink("explore", "Explore", None, external = false),
      MenuLink("docs", "Docs", None, external = false)
    ),
    dropdowns = Seq(
      MenuDropdown(
        "users",
        "Users",
        Seq(
          MenuLink("create-user", "Create a User", Some("/create-user"), external = false),
          MenuLink("create-service-user", "Create a Service User", Some("/create-service-user"), external = false),
          MenuLink("offboard-users", "Offboard Users", Some("/offboard-users"), external = false)
        )
      ),
      MenuDropdown(
        "deployments",
        "Deployments",
        Seq(
          MenuLink("deploy-service", "Deploy Service", Some("/deploy-service"), external = false),
          MenuLink("deployment-events", "Deployment Events", Some("/deployments/production"), external = false),
          MenuLink("deployment-timeline", "Version Timeline", Some("/deployment-timeline"), external = false),
          MenuLink("whats-running-where", "What's Running Where", Some("/whats-running-where"), external = false)
        )
      ),
      MenuDropdown(
        "shuttering",
        "Shuttering",
        Seq(
          MenuLink(
            "shutter-overview-frontend",
            "Shutter Overview - Frontend",
            Some("/shuttering-overview/frontend"),
            external = false
          ),
          MenuLink(
            "shutter-overview-api",
            "Shutter Overview - Api",
            Some("/shuttering-overview/api"),
            external = false
          ),
          MenuLink(
            "shutter-overview-rate",
            "Shutter Overview - Rate",
            Some("/shuttering-overview/rate"),
            external = false
          ),
          MenuLink("shutter-events", "Shutter Events", Some("/shutter-events"), external = false)
        )
      ),
      MenuDropdown(
        "health",
        "Health",
        Seq(
          MenuLink("platform-initiatives", "Platform Initiatives", Some("/platform-initiatives"), external = false),
          MenuLink("bobby-rules", "Bobby Rules", Some("/bobbyrules"), external = false),
          MenuLink("bobby-violations", "Bobby Violations", Some("/bobby-violations"), external = false),
          MenuLink("leak-detection-rules", "Leak Detection - Rules", Some("/leak-detection"), external = false),
          MenuLink(
            "leak-detection-repositories",
            "Leak Detection - Repositories",
            Some("/leak-detection/repositories?includeViolations=true"),
            external = false
          ),
          MenuLink(
            "vulnerabilities",
            "Vulnerabilities",
            Some("/vulnerabilities?curationStatus=ACTION_REQUIRED"),
            external = false
          ),
          MenuLink(
            "vulnerabilities-services",
            "Vulnerabilities - Services",
            Some("/vulnerabilities/services"),
            external = false
          ),
          MenuLink(
            "vulnerabilities-timeline",
            "Vulnerabilities - Timeline",
            Some("/vulnerabilities/timeline?curationStatus=ACTION_REQUIRED"),
            external = false
          ),
          MenuLink(
            "pr-commenter-recommendations",
            "PR-Commenter Recommendations",
            Some("/pr-commenter/recommendations"),
            external = false
          ),
          MenuLink(
            "health-metrics-timeline",
            "Health Metrics - Timeline",
            Some("/health-metrics/timeline"),
            external = false
          ),
          MenuLink("operational-metrics", "Operational Metrics", Some("/health-metrics"), external = false)
        )
      ),
      MenuDropdown(
        "explore",
        "Explore",
        Seq(
          MenuLink("dependency-explorer", "Dependency Explorer", Some("/dependencyexplorer"), external = false),
          MenuLink("jdk-explorer", "JDK Explorer", Some("/jdkexplorer"), external = false),
          MenuLink("sbt-explorer", "SBT Explorer", Some("/sbtexplorer"), external = false),
          MenuLink("search-by-url", "Search by URL", Some("/search#"), external = false),
          MenuLink("search-config", "Search Config", Some("/config/search"), external = false),
          MenuLink(
            "search-commissioning-state",
            "Search Commissioning State",
            Some("/commissioning-state/search"),
            external = false
          ),
          MenuLink("service-metrics", "Service Metrics", Some("/service-metrics"), external = false),
          MenuLink("test-results", "Test Results", Some("/tests"), external = false),
          MenuLink("config-warnings", "Config Warnings", Some("/config/warnings/search"), external = false),
          MenuLink("cost-explorer", "Cost Explorer", Some("/cost-explorer"), external = false),
          MenuLink("service-provision", "Service Provision", Some("/service-provision"), external = false)
        )
      ),
      MenuDropdown(
        "docs",
        "Docs",
        Seq(
          MenuLink(
            "mdtp-handbook",
            "MDTP Handbook",
            Some("https://docs.tax.service.gov.uk/mdtp-handbook/"),
            external = true
          ),
          MenuLink(
            "blog-posts",
            "Blog Posts",
            Some(
              "https://confluence.tools.tax.service.gov.uk/dosearchsite.action?cql=(label=catalogue and type=blogpost) order by created desc"
            ),
            external = true
          )
        )
      )
    )
  )

  val sampleSearchTerms = Seq(SearchTerm("service", "foo-service", "/services/foo-service"))

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
