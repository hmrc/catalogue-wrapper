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
        brand = MenuLink("brand", "MDTP", "/", external = false),
        topLevelLinks = Seq(MenuLink("repos", "Repositories", "/repositories")),
        dropdowns = Seq(
          MenuDropdown(
            "explore",
            "Explore",
            Seq(MenuLink("teams", "Teams", "/teams"))
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
          "brand": {"id":"brand","text":"MDTP","href":"/","external":false},
          "topLevelLinks": [{"id":"ext","text":"External","href":"https://example.com","external":true}],
          "dropdowns": []
        }
      """)
      val menu = json.as[BannerMenu]
      menu.topLevelLinks.head.external shouldBe true
    }
  }

  "MenuLink JSON" should {
    "default external to false when absent" in {
      val json = Json.parse("""{"id":"foo","text":"Foo","href":"/foo"}""")
      json.as[MenuLink].external shouldBe false
    }
  }
