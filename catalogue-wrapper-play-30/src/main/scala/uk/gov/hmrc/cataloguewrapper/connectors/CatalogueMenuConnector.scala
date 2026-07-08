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

import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.cataloguewrapper.config.CatalogueWrapperConfig
import uk.gov.hmrc.cataloguewrapper.models.{BannerMenu, NavigationData, SearchTerm}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait TestConstants {
  val rawJson =
    """
      |{
      |  "brand": {
      |    "name": "MDTP",
      |    "id": "mdtp",
      |    "description": "MDTP",
      |    "href": "/",
      |    "external": false
      |  },
      |  "topLevelLinks": [
      |    {
      |      "name": "Users",
      |      "id": "users",
      |      "description": "View and manage users",
      |      "href": "/users",
      |      "external": false
      |    },
      |    {
      |      "name": "Teams",
      |      "id": "teams",
      |      "description": "View and manage teams",
      |      "href": "/teams",
      |      "external": false
      |    },
      |    {
      |      "name": "Repositories",
      |      "id": "repositories",
      |      "description": "View and manage repositories",
      |      "href": "/repositories",
      |      "external": false
      |    },
      |    {
      |      "name": "Deployments",
      |      "id": "deployments",
      |      "description": "View and manage deployments",
      |      "external": false
      |    },
      |    {
      |      "name": "Shuttering",
      |      "id": "shuttering",
      |      "description": "View and manage shuttering",
      |      "external": false
      |    },
      |    {
      |      "name": "Health",
      |      "id": "health",
      |      "description": "View and manage health",
      |      "external": false
      |    },
      |    {
      |      "name": "Explore",
      |      "id": "explore",
      |      "description": "Explore services",
      |      "external": false
      |    },
      |    {
      |      "name": "Docs",
      |      "id": "docs",
      |      "description": "View documentation",
      |      "external": false
      |    }
      |  ],
      |  "dropdowns": [
      |    {
      |      "id": "users",
      |      "text": "Users",
      |      "href": "/users",
      |      "items": [
      |        {
      |          "id": "create-user",
      |          "name": "Create a User",
      |          "href": "/create-user",
      |          "external": false
      |        },
      |        {
      |          "id": "create-service-user",
      |          "name": "Create a Service User",
      |          "href": "/create-service-user",
      |          "external": false
      |        },
      |        {
      |          "id": "offboard-users",
      |          "name": "Offboard Users",
      |          "href": "/offboard-users",
      |          "external": false
      |        }
      |      ],
      |      "dropDownRole": []
      |    },
      |    {
      |      "id": "deployments",
      |      "text": "Deployments",
      |      "items": [
      |        {
      |          "id": "deploy-service",
      |          "name": "Deploy Service",
      |          "href": "/deploy-service",
      |          "external": false
      |        },
      |        {
      |          "id": "deployment-events",
      |          "name": "Deployment Events",
      |          "href": "/deployments/production",
      |          "external": false
      |        },
      |        {
      |          "id": "deployment-timeline",
      |          "name": "Version Timeline",
      |          "href": "/deployment-timeline",
      |          "external": false
      |        },
      |        {
      |          "id": "whats-running-where",
      |          "name": "What's Running Where",
      |          "href": "/whats-running-where",
      |          "external": false
      |        }
      |      ],
      |      "dropDownRole": []
      |    },
      |    {
      |      "id": "shuttering",
      |      "text": "Shuttering",
      |      "items": [
      |        {
      |          "id": "shutter-overview-frontend",
      |          "name": "Shutter Overview - Frontend",
      |          "href": "/shuttering-overview/frontend",
      |          "external": false
      |        },
      |        {
      |          "id": "shutter-overview-api",
      |          "name": "Shutter Overview - Api",
      |          "href": "/shuttering-overview/api",
      |          "external": false
      |        },
      |        {
      |          "id": "shutter-overview-rate",
      |          "name": "Shutter Overview - Rate",
      |          "href": "/shuttering-overview/rate",
      |          "external": false
      |        },
      |        {
      |          "id": "shutter-events",
      |          "name": "Shutter Events",
      |          "href": "/shutter-events",
      |          "external": false
      |        }
      |      ],
      |      "dropDownRole": []
      |    },
      |    {
      |      "id": "health",
      |      "text": "Health",
      |      "items": [
      |        {
      |          "id": "platform-initiatives",
      |          "name": "Platform Initiatives",
      |          "href": "/platform-initiatives",
      |          "external": false
      |        },
      |        {
      |          "id": "bobby-rules",
      |          "name": "Bobby Rules",
      |          "href": "/bobbyrules",
      |          "external": false
      |        },
      |        {
      |          "id": "bobby-violations",
      |          "name": "Bobby Violations",
      |          "href": "/bobby-violations",
      |          "external": false
      |        },
      |        {
      |          "id": "leak-detection-rules",
      |          "name": "Leak Detection - Rules",
      |          "href": "/leak-detection",
      |          "external": false
      |        },
      |        {
      |          "id": "leak-detection-repositories",
      |          "name": "Leak Detection - Repositories",
      |          "href": "/leak-detection/repositories?includeViolations=true",
      |          "external": false
      |        },
      |        {
      |          "id": "vulnerabilities",
      |          "name": "Vulnerabilities",
      |          "href": "/vulnerabilities?curationStatus=ACTION_REQUIRED",
      |          "external": false
      |        },
      |        {
      |          "id": "vulnerabilities-services",
      |          "name": "Vulnerabilities - Services",
      |          "href": "/vulnerabilities/services",
      |          "external": false
      |        },
      |        {
      |          "id": "vulnerabilities-timeline",
      |          "name": "Vulnerabilities - Timeline",
      |          "href": "/vulnerabilities/timeline?curationStatus=ACTION_REQUIRED",
      |          "external": false
      |        },
      |        {
      |          "id": "pr-commenter-recommendations",
      |          "name": "PR-Commenter Recommendations",
      |          "href": "/pr-commenter/recommendations",
      |          "external": false
      |        },
      |        {
      |          "id": "health-metrics-timeline",
      |          "name": "Health Metrics - Timeline",
      |          "href": "/health-metrics/timeline",
      |          "external": false
      |        },
      |        {
      |          "id": "operational-metrics",
      |          "name": "Operational Metrics",
      |          "href": "/health-metrics",
      |          "external": false
      |        }
      |      ],
      |      "dropDownRole": []
      |    },
      |    {
      |      "id": "explore",
      |      "text": "Explore",
      |      "items": [
      |        {
      |          "id": "dependency-explorer",
      |          "name": "Dependency Explorer",
      |          "href": "/dependencyexplorer",
      |          "external": false
      |        },
      |        {
      |          "id": "jdk-explorer",
      |          "name": "JDK Explorer",
      |          "href": "/jdkexplorer",
      |          "external": false
      |        },
      |        {
      |          "id": "sbt-explorer",
      |          "name": "SBT Explorer",
      |          "href": "/sbtexplorer",
      |          "external": false
      |        },
      |        {
      |          "id": "search-by-url",
      |          "name": "Search by URL",
      |          "href": "/search#",
      |          "external": false
      |        },
      |        {
      |          "id": "search-config",
      |          "name": "Search Config",
      |          "href": "/config/search",
      |          "external": false
      |        },
      |        {
      |          "id": "search-commissioning-state",
      |          "name": "Search Commissioning State",
      |          "href": "/commissioning-state/search",
      |          "external": false
      |        },
      |        {
      |          "id": "service-metrics",
      |          "name": "Service Metrics",
      |          "href": "/service-metrics",
      |          "external": false
      |        },
      |        {
      |          "id": "test-results",
      |          "name": "Test Results",
      |          "href": "/tests",
      |          "external": false
      |        },
      |        {
      |          "id": "config-warnings",
      |          "name": "Config Warnings",
      |          "href": "/config/warnings/search",
      |          "external": false
      |        },
      |        {
      |          "id": "cost-explorer",
      |          "name": "Cost Explorer",
      |          "href": "/cost-explorer",
      |          "external": false
      |        },
      |        {
      |          "id": "service-provision",
      |          "name": "Service Provision",
      |          "href": "/service-provision",
      |          "external": false
      |        }
      |      ],
      |      "dropDownRole": []
      |    },
      |    {
      |      "id": "docs",
      |      "text": "Docs",
      |      "items": [
      |        {
      |          "id": "mdtp-handbook",
      |          "name": "MDTP Handbook",
      |          "href": "https://docs.tax.service.gov.uk/mdtp-handbook/",
      |          "external": true
      |        },
      |        {
      |          "id": "blog-posts",
      |          "name": "Blog Posts",
      |          "href": "https://confluence.tools.tax.service.gov.uk/dosearchsite.action?cql=(label=catalogue and type=blogpost) order by created desc",
      |          "external": true
      |        }
      |      ],
      |      "dropDownRole": []
      |    }
      |  ]
      |}""".stripMargin
}

@Singleton
class CatalogueMenuConnector @Inject() (
    httpClient: HttpClientV2,
    config: CatalogueWrapperConfig
)(implicit ec: ExecutionContext)
    extends Logging
    with TestConstants:

  val searchIndexEndpoint = "/catalogue-config/menu-bar/search-index"

  val menuBarEndpoint = "/catalogue-config/menu-bar/menu"

  def getNavigationData()(implicit hc: HeaderCarrier): Future[NavigationData] =
    val menuF        = getMenu()
    val searchIndexF = getSearchIndex()

    menuF
      .zip(searchIndexF)
      .map { case (menu, searchIndex) =>
        NavigationData(menu = menu, searchIndex = searchIndex)
      }

  private def getMenu()(implicit hc: HeaderCarrier): Future[BannerMenu] = {
    // TODO: Retrieving static version of menu for testing.
    val resultOrError = Json.parse(rawJson).validate[BannerMenu]
    resultOrError match {
      case JsSuccess(value, path) => Future.successful(value)
      case JsError(errors)        =>
        logger.error(s"Failed to parse menu JSON: $errors")
        Future.failed(new RuntimeException("Failed to parse menu JSON"))
    }
  }

  private def getMenuFromService()(implicit hc: HeaderCarrier): Future[BannerMenu] = {

    for {
      raw    <- httpClient
                  .get(url"${config.menuBarBaseUrl}/catalogue-config/menu-bar/menu")
                  .execute[String]
      _       = logger.warn("Fetching menu bar contents as JSON: " + raw)
      result <- httpClient
                  .get(url"${config.menuBarBaseUrl}/catalogue-config/menu-bar/menu")
                  .execute[BannerMenu]

    } yield result

  }

  private def getSearchIndex()(implicit hc: HeaderCarrier): Future[Seq[SearchTerm]] = {
//    println("Attempting to construct url:")
//    val value = url"${config.menuBarBaseUrl}$searchIndexEndpoint"
    httpClient
      .get(url"${config.menuBarBaseUrl}/catalogue-config/menu-bar/search-index")
      .execute[Seq[SearchTerm]]
  }
