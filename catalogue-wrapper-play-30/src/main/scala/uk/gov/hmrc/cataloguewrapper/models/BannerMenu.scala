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

import play.api.libs.json.{Json, OFormat}

final case class MenuLink(
    id: String,
    text: String,
    href: String,
    external: Boolean = false
)

object MenuLink:
  given OFormat[MenuLink] = Json.using[Json.WithDefaultValues].format[MenuLink]

final case class MenuDropdown(
    id: String,
    text: String,
    items: Seq[MenuLink]
)

object MenuDropdown:
  given OFormat[MenuDropdown] = Json.format[MenuDropdown]

final case class BannerMenu(
    brand: MenuLink,
    topLevelLinks: Seq[MenuLink],
    dropdowns: Seq[MenuDropdown]
)

object BannerMenu:
  given OFormat[BannerMenu] = Json.format[BannerMenu]

final case class SearchTerm(
    linkType: String,
    name: String,
    href: String,
    weight: Float = 0.5f,
    hints: Set[String] = Set.empty,
    openInNewWindow: Boolean = false
):
  lazy val terms: Set[String] =
    Set(name, linkType).union(hints).map(SearchTerm.normalise)

object SearchTerm:
  given OFormat[SearchTerm] = Json.using[Json.WithDefaultValues].format[SearchTerm]

  def normalise(value: String): String =
    value.toLowerCase.replaceAll("[ \\-_]", "")

final case class NavigationData(
    menu: BannerMenu,
    searchIndex: Seq[SearchTerm]
)

object NavigationData:
  given OFormat[NavigationData] =
    Json.using[Json.WithDefaultValues].format[NavigationData]
