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

import play.api.libs.json.{Format, Json}

case class ViewModel(
    brand: MenuLink,
    topLevelLinks: Seq[MenuLinkViewModel]
)

object ViewModel {
  given format: Format[ViewModel] = Json.format[ViewModel]

  def from(menu: BannerMenu, activeItemId: Option[String] = None): ViewModel =
    ViewModel(
      brand = menu.brand,
      topLevelLinks = menu.topLevelLinks.map { link =>
        val dropdown = menu.dropdowns.find(_.id == link.id)

        MenuLinkViewModel(
          name = link.name,
          id = link.id,
          href = link.href,
          external = link.external,
          dropdown = dropdown,
          isActive = activeItemId.contains(link.id),
          isDropdownActive =
            dropdown.exists(_.items.collect { case page: Page => page.id }.exists(activeItemId.contains))
        )
      }
    )

  val empty: ViewModel =
    ViewModel(
      brand = TopMenu(name = "MDTP", id = "brand", href = Some("/")),
      topLevelLinks = Seq.empty
    )
}

case class MenuLinkViewModel(
    name: String,
    id: String,
    href: Option[String],
    external: Boolean,
    dropdown: Option[MenuDropdown],
    isActive: Boolean = false,
    isDropdownActive: Boolean = false
)

object MenuLinkViewModel {
  given format: Format[MenuLinkViewModel] = Json.format[MenuLinkViewModel]
}

//case class TopMenuViewModel (
//    topLinks: Seq[TopMenu],
//    bannerMenu: BannerMenu
//) {
//
//}
