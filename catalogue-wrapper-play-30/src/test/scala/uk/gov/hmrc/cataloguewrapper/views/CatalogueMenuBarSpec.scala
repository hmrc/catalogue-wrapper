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

package uk.gov.hmrc.cataloguewrapper.views

import org.jsoup.Jsoup
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.cataloguewrapper.models.{BannerMenu, TopMenu, ViewModel}
import uk.gov.hmrc.cataloguewrapper.views.html.CatalogueMenuBar

class CatalogueMenuBarSpec extends AnyWordSpec with Matchers:
  "Catalogue search" should {
    "keep automatic left spacing when the main links are hidden for expanded search" in {
      val menu = BannerMenu(TopMenu("MDTP", "brand", Some("/")),
        Seq("Teams", "Repositories", "Deployments").map(name => TopMenu(name, name.toLowerCase, Some("/" + name.toLowerCase))), Seq.empty)
      val html = CatalogueMenuBar(ViewModel.from(menu), quickSearchUrl = "/quicksearch", minSearchLen = 3,
        signOutUrl = Some("/sign-out"))(FakeRequest(), stubMessages()).toString
      val document = Jsoup.parse(html)
      document.select("#catalogue-search-bar").hasClass("ms-auto") shouldBe true
      document.select("#catalogue-search").size() shouldBe 1
      document.select("#main-menu-bar").size() shouldBe 1
    }
  }
