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

package uk.gov.hmrc.cataloguewrapper.models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class BannerMenuSpec extends AnyWordSpec with Matchers:

  "BannerMenu JSON" should {
    "round-trip through reads/writes" in {
      val menu   = BannerMenu(
        brand = MenuLink("brand", "MDTP", Some("/"), external = false),
        topLevelLinks = Seq(MenuLink("repos", "Repositories", Some("/repositories"))),
        dropdowns = Seq(
          MenuDropdown(
            "explore",
            "Explore",
            Seq(MenuLink("teams", "Teams", Some("/teams")))
          )
        )
      )
      val json   = Json.toJson(menu)
      val parsed = json.as[BannerMenu]
      parsed shouldBe menu
    }

    "decode external links correctly" in {
      val json = Json.parse("""
        {
          "brand": {"id":"brand","name":"MDTP","href":"/","external":false},
          "topLevelLinks": [{"id":"ext","name":"External","href":"https://example.com","external":true}],
          "dropdowns": []
        }
      """)
      val menu = json.as[BannerMenu]
      menu.topLevelLinks.head.external shouldBe true
    }
  }

  "MenuLink JSON" should {
    "default external to false when absent" in {
      val json = Json.parse("""{"id":"foo","name":"Foo","href":"/foo"}""")
      json.as[MenuLink].external shouldBe false
    }

    "Example Json" should {
      val json = """{
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
                   |      "name": "Users",
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
                   |      "name": "Deployments",
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
                   |      "name": "Shuttering",
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
                   |      "name": "Health",
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
                   |      "name": "Explore",
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
                   |      "name": "Docs",
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

      "successfully serialize to BannerMenu" in {
        val resultOrError = Json.parse(json).as[BannerMenu]
        resultOrError match {
          case menu: BannerMenu =>
            println("Parsed BannerMenu successfully:")
            println(menu)
          case _                =>
            println("Failed to parse BannerMenu.")
        }
      }

    }
  }
