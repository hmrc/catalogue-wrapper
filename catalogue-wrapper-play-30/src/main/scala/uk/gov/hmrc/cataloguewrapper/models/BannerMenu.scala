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
import play.api.libs.json.{Format, Json, JsonConfiguration, JsonNaming}

sealed trait MenuLink {
  def name: String

  def id: String

  def href: Option[String]

  def external: Boolean
}

object MenuLink:
  given JsonConfiguration        = JsonConfiguration(
    typeNaming = JsonNaming(_.split("\\.").last)
    )
  given format: Format[MenuLink] = Json.format[MenuLink]

sealed trait DropdownItem:
  def isSeparator: Boolean = false
  def asPage: Option[Page] = None

object DropdownItem:
  given JsonConfiguration            = JsonConfiguration(
    typeNaming = JsonNaming(_.split("\\.").last)
    )
  given format: Format[DropdownItem] = Json.format[DropdownItem]

case object DropdownSeparator extends DropdownItem:
  given format: Format[DropdownSeparator.type] = Json.format[DropdownSeparator.type]
  override def isSeparator: Boolean = true

final case class BannerMenu(
  brand: MenuLink,
  topLevelLinks: Seq[MenuLink],
  dropdowns: Seq[MenuDropdown]
)

object BannerMenu:
  given format: Format[BannerMenu] =
    Json.format[BannerMenu]

  val empty: BannerMenu =
    BannerMenu(
      brand = TopMenu("brand", "MDTP", Some("/")),
      topLevelLinks = Seq.empty,
      dropdowns = Seq.empty
      )

final case class MenuDropdown(
  id           :String,
  name         :String,
  href         :Option[String],
  items        :Seq[DropdownItem],
  dropDownRole : Seq[Role] = Nil
)

object MenuDropdown {
  given format: Format[MenuDropdown] =
    Json.format[MenuDropdown]
}

final case class TopMenu(
  name        :String,
  id          :String,
  href        :Option[String],
  external    :Boolean = false
) extends MenuLink

object TopMenu:
  given format: Format[TopMenu] = Json.format[TopMenu]
  def apply(name: String, id: String, href: String): TopMenu =
    TopMenu(name, id, Some(href))

  def apply(name: String, id: String): TopMenu =
    TopMenu(name, id, None)

final case class Page(
  name        :String,
  id          :String,
  href        :Option[String],
  external    :Boolean = false
) extends MenuLink
  with DropdownItem:
  override def asPage: Option[Page] = Some(this)

object Page:
  given format: Format[Page] = Json.format[Page]

  def apply(name: String, id: String, href: String): Page =
    Page(name, id, Some(href))

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
  given Format[SearchTerm] = Json.using[Json.WithDefaultValues].format[SearchTerm]

  def normalise(value: String): String =
    value.toLowerCase.replaceAll("[ \\-_]", "")

final case class NavigationData(
    menu: BannerMenu,
    searchIndex: Seq[SearchTerm]
)

object NavigationData:
  given Format[NavigationData] =
    Json.using[Json.WithDefaultValues].format[NavigationData]

  val empty: NavigationData =
    NavigationData(
      menu = BannerMenu.empty,
      searchIndex = Seq.empty
    )
