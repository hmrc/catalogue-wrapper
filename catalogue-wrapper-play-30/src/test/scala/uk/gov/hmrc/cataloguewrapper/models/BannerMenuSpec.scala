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
        brand = TopMenu("brand", "MDTP", Some("/"), external = false),
        topLevelLinks = Seq(TopMenu("repos", "Repositories", "/repositories")),
        dropdowns = Seq(
          MenuDropdown(
            "explore",
            "Explore",
            None,
            Seq(Page("Teams", "teams", "/teams"))
          )
        )
      )
      val json   = Json.toJson(menu)
      val parsed = json.as[BannerMenu]
      parsed shouldBe menu
    }

    "decode external links correctly" in {
      val json = Json.parse(
        """{
          |  "brand": {
          |    "id": "brand",
          |    "name": "MDTP",
          |    "href": "/",
          |    "external": false,
          |    "_type": "TopMenu"
          |  },
          |  "topLevelLinks": [
          |    {
          |      "id": "ext",
          |      "name": "External",
          |      "href": "https://example.com",
          |      "external": true,
          |      "_type": "TopMenu"
          |    }
          |  ],
          |  "dropdowns": []
          |}""".stripMargin
      )
      val menu = json.as[BannerMenu]
      menu.topLevelLinks.head.external shouldBe true
    }
  }

  "MenuLink JSON" should {
    "default external to false when absent" in {
      val json = Json.parse("""{"id":"foo","name":"Foo","href":"/foo","_type":"TopMenu"}""")
      json.as[MenuLink].external shouldBe false
    }

    "Example Json" should {
      val json = """{
                   |  "brand": {
                   |    "name": "MDTP",
                   |    "id": "mdtp",
                   |    "href": "/",
                   |    "external": false,
                   |    "_type": "TopMenu"
                   |  },
                   |  "topLevelLinks": [
                   |    {
                   |      "name": "Users",
                   |      "id": "users",
                   |      "href": "/users",
                   |      "external": false,
                   |      "_type": "TopMenu"
                   |    },
                   |    {
                   |      "name": "Teams",
                   |      "id": "teams",
                   |      "href": "/teams",
                   |      "external": false,
                   |      "_type": "TopMenu"
                   |    },
                   |    {
                   |      "name": "Repositories",
                   |      "id": "repositories",
                   |      "href": "/repositories",
                   |      "external": false,
                   |      "_type": "TopMenu"
                   |    },
                   |    {
                   |      "name": "Deployments",
                   |      "id": "deployments",
                   |      "external": false,
                   |      "_type": "TopMenu"
                   |    },
                   |    {
                   |      "name": "Shuttering",
                   |      "id": "shuttering",
                   |      "external": false,
                   |      "_type": "TopMenu"
                   |    },
                   |    {
                   |      "name": "Health",
                   |      "id": "health",
                   |      "external": false,
                   |      "_type": "TopMenu"
                   |    },
                   |    {
                   |      "name": "Explore",
                   |      "id": "explore",
                   |      "external": false,
                   |      "_type": "TopMenu"
                   |    },
                   |    {
                   |      "name": "Docs",
                   |      "id": "docs",
                   |      "external": false,
                   |      "_type": "TopMenu"
                   |    }
                   |  ],
                   |  "dropdowns": [
                   |    {
                   |      "id": "users",
                   |      "name": "Users",
                   |      "href": "/users",
                   |      "items": [
                   |        {
                   |          "name": "create-user",
                   |          "id": "Create a User",
                   |          "href": "/create-user",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "create-service-user",
                   |          "id": "Create a Service User",
                   |          "href": "/create-service-user",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "offboard-users",
                   |          "id": "Offboard Users",
                   |          "href": "/offboard-users",
                   |          "external": false,
                   |          "_type": "Page"
                   |        }
                   |      ],
                   |      "dropDownRole": []
                   |    },
                   |    {
                   |      "id": "deployments",
                   |      "name": "Deployments",
                   |      "items": [
                   |        {
                   |          "name": "deploy-service",
                   |          "id": "Deploy Service",
                   |          "href": "/deploy-service",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "deployment-events",
                   |          "id": "Deployment Events",
                   |          "href": "/deployments/production",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "deployment-timeline",
                   |          "id": "Version Timeline",
                   |          "href": "/deployment-timeline",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "whats-running-where",
                   |          "id": "What's Running Where",
                   |          "href": "/whats-running-where",
                   |          "external": false,
                   |          "_type": "Page"
                   |        }
                   |      ],
                   |      "dropDownRole": []
                   |    },
                   |    {
                   |      "id": "shuttering",
                   |      "name": "Shuttering",
                   |      "items": [
                   |        {
                   |          "name": "shutter-overview-frontend",
                   |          "id": "Shutter Overview - Frontend",
                   |          "href": "/shuttering-overview/frontend",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "shutter-overview-api",
                   |          "id": "Shutter Overview - Api",
                   |          "href": "/shuttering-overview/api",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "shutter-overview-rate",
                   |          "id": "Shutter Overview - Rate",
                   |          "href": "/shuttering-overview/rate",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "shutter-events",
                   |          "id": "Shutter Events",
                   |          "href": "/shutter-events",
                   |          "external": false,
                   |          "_type": "Page"
                   |        }
                   |      ],
                   |      "dropDownRole": []
                   |    },
                   |    {
                   |      "id": "health",
                   |      "name": "Health",
                   |      "items": [
                   |        {
                   |          "name": "platform-initiatives",
                   |          "id": "Platform Initiatives",
                   |          "href": "/platform-initiatives",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "bobby-rules",
                   |          "id": "Bobby Rules",
                   |          "href": "/bobbyrules",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "bobby-violations",
                   |          "id": "Bobby Violations",
                   |          "href": "/bobby-violations",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "leak-detection-rules",
                   |          "id": "Leak Detection - Rules",
                   |          "href": "/leak-detection",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "leak-detection-repositories",
                   |          "id": "Leak Detection - Repositories",
                   |          "href": "/leak-detection/repositories?includeViolations=true",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "vulnerabilities",
                   |          "id": "Vulnerabilities",
                   |          "href": "/vulnerabilities?curationStatus=ACTION_REQUIRED",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "vulnerabilities-services",
                   |          "id": "Vulnerabilities - Services",
                   |          "href": "/vulnerabilities/services",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "vulnerabilities-timeline",
                   |          "id": "Vulnerabilities - Timeline",
                   |          "href": "/vulnerabilities/timeline?curationStatus=ACTION_REQUIRED",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "pr-commenter-recommendations",
                   |          "id": "PR-Commenter Recommendations",
                   |          "href": "/pr-commenter/recommendations",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "health-metrics-timeline",
                   |          "id": "Health Metrics - Timeline",
                   |          "href": "/health-metrics/timeline",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "operational-metrics",
                   |          "id": "Operational Metrics",
                   |          "href": "/health-metrics",
                   |          "external": false,
                   |          "_type": "Page"
                   |        }
                   |      ],
                   |      "dropDownRole": []
                   |    },
                   |    {
                   |      "id": "explore",
                   |      "name": "Explore",
                   |      "items": [
                   |        {
                   |          "name": "dependency-explorer",
                   |          "id": "Dependency Explorer",
                   |          "href": "/dependencyexplorer",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "jdk-explorer",
                   |          "id": "JDK Explorer",
                   |          "href": "/jdkexplorer",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "sbt-explorer",
                   |          "id": "SBT Explorer",
                   |          "href": "/sbtexplorer",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "search-by-url",
                   |          "id": "Search by URL",
                   |          "href": "/search#",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "search-config",
                   |          "id": "Search Config",
                   |          "href": "/config/search",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "search-commissioning-state",
                   |          "id": "Search Commissioning State",
                   |          "href": "/commissioning-state/search",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "service-metrics",
                   |          "id": "Service Metrics",
                   |          "href": "/service-metrics",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "test-results",
                   |          "id": "Test Results",
                   |          "href": "/tests",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "config-warnings",
                   |          "id": "Config Warnings",
                   |          "href": "/config/warnings/search",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "cost-explorer",
                   |          "id": "Cost Explorer",
                   |          "href": "/cost-explorer",
                   |          "external": false,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "service-provision",
                   |          "id": "Service Provision",
                   |          "href": "/service-provision",
                   |          "external": false,
                   |          "_type": "Page"
                   |        }
                   |      ],
                   |      "dropDownRole": []
                   |    },
                   |    {
                   |      "id": "docs",
                   |      "name": "Docs",
                   |      "items": [
                   |        {
                   |          "name": "MDTP Handbook",
                   |          "id": "mdtp-handbook",
                   |          "href": "https://docs.tax.service.gov.uk/mdtp-handbook/",
                   |          "external": true,
                   |          "_type": "Page"
                   |        },
                   |        {
                   |          "name": "Blog Posts",
                   |          "id": "blog-posts",
                   |          "href": "https://confluence.tools.tax.service.gov.uk/dosearchsite.action?cql=(label=catalogue and type=blogpost) order by created desc",
                   |          "external": true,
                   |          "_type": "Page"
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
