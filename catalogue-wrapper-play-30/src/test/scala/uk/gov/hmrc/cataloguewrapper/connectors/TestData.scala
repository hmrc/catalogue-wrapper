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

trait TestData {
  val rawJson: String =
    """
      |{
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
      |          "name": "Create a User",
      |          "id": "create-user",
      |          "href": "/create-user",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Create a Service User",
      |          "id": "create-service-user",
      |          "href": "/create-service-user",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Offboard Users",
      |          "id": "offboard-users",
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
      |          "name": "Deploy Service",
      |          "id": "deploy-service",
      |          "href": "/deploy-service",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Deployment Events",
      |          "id": "deployment-events",
      |          "href": "/deployments/production",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Version Timeline",
      |          "id": "deployment-timeline",
      |          "href": "/deployment-timeline",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "What's Running Where",
      |          "id": "whats-running-where",
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
      |          "name": "Shutter Overview - Frontend",
      |          "id": "shutter-overview-frontend",
      |          "href": "/shuttering-overview/frontend",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Shutter Overview - Api",
      |          "id": "shutter-overview-api",
      |          "href": "/shuttering-overview/api",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Shutter Overview - Rate",
      |          "id": "shutter-overview-rate",
      |          "href": "/shuttering-overview/rate",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Shutter Events",
      |          "id": "shutter-events",
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
      |          "name": "Platform Initiatives",
      |          "id": "platform-initiatives",
      |          "href": "/platform-initiatives",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Bobby Rules",
      |          "id": "bobby-rules",
      |          "href": "/bobbyrules",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Bobby Violations",
      |          "id": "bobby-violations",
      |          "href": "/bobby-violations",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Leak Detection - Rules",
      |          "id": "leak-detection-rules",
      |          "href": "/leak-detection",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Leak Detection - Repositories",
      |          "id": "leak-detection-repositories",
      |          "href": "/leak-detection/repositories?includeViolations=true",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Vulnerabilities",
      |          "id": "vulnerabilities",
      |          "href": "/vulnerabilities?curationStatus=ACTION_REQUIRED",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Vulnerabilities - Services",
      |          "id": "vulnerabilities-services",
      |          "href": "/vulnerabilities/services",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Vulnerabilities - Timeline",
      |          "id": "vulnerabilities-timeline",
      |          "href": "/vulnerabilities/timeline?curationStatus=ACTION_REQUIRED",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "_type": "DropdownSeparator"
      |        },
      |        {
      |          "name": "PR-Commenter Recommendations",
      |          "id": "pr-commenter-recommendations",
      |          "href": "/pr-commenter/recommendations",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Health Metrics - Timeline",
      |          "id": "health-metrics-timeline",
      |          "href": "/health-metrics/timeline",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Operational Metrics",
      |          "id": "operational-metrics",
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
      |          "name": "Dependency Explorer",
      |          "id": "dependency-explorer",
      |          "href": "/dependencyexplorer",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "JDK Explorer",
      |          "id": "jdk-explorer",
      |          "href": "/jdkexplorer",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "SBT Explorer",
      |          "id": "sbt-explorer",
      |          "href": "/sbtexplorer",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Search by URL",
      |          "id": "search-by-url",
      |          "href": "/search#",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Search Config",
      |          "id": "search-config",
      |          "href": "/config/search",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Search Commissioning State",
      |          "id": "search-commissioning-state",
      |          "href": "/commissioning-state/search",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Service Metrics",
      |          "id": "service-metrics",
      |          "href": "/service-metrics",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Test Results",
      |          "id": "test-results",
      |          "href": "/tests",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Config Warnings",
      |          "id": "config-warnings",
      |          "href": "/config/warnings/search",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Cost Explorer",
      |          "id": "cost-explorer",
      |          "href": "/cost-explorer",
      |          "external": false,
      |          "_type": "Page"
      |        },
      |        {
      |          "name": "Service Provision",
      |          "id": "service-provision",
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

}
