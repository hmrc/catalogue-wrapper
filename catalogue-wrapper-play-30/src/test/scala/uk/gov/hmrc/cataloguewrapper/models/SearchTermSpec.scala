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

class SearchTermSpec extends AnyWordSpec with Matchers:

  "SearchTerm JSON" should {
    "round-trip through reads/writes" in {
      val term = SearchTerm(
        linkType = "service",
        name = "my-service",
        href = "/services/my-service",
        weight = 1.0f,
        hints = Set("tag1", "tag2"),
        openInNewWindow = false
      )
      Json.toJson(term).as[SearchTerm] shouldBe term
    }

    "use default values when fields absent" in {
      val json = Json.parse("""{"linkType":"team","name":"My Team","href":"/teams/my-team"}""")
      val term = json.as[SearchTerm]
      term.weight shouldBe 0.5f
      term.hints shouldBe Set.empty
      term.openInNewWindow shouldBe false
    }
  }
