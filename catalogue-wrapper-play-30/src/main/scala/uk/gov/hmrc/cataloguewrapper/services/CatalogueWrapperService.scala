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

package uk.gov.hmrc.cataloguewrapper.services

import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.cataloguewrapper.config.CatalogueWrapperConfig
import uk.gov.hmrc.cataloguewrapper.connectors.CatalogueMenuConnector
import uk.gov.hmrc.cataloguewrapper.models.BannerMenu
import uk.gov.hmrc.cataloguewrapper.views.html.StandardCatalogueLayout
import uk.gov.hmrc.cataloguewrapper.views.html.{CatalogueMenuBar => CatalogueMenuBarView}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CatalogueWrapperService @Inject() (
    connector: CatalogueMenuConnector,
    config: CatalogueWrapperConfig,
    standardLayout: StandardCatalogueLayout
)(implicit ec: ExecutionContext):

  /** Fetch the menu from catalogue-navigation and render the full page layout. */
  def standardCatalogueLayout(
      content: HtmlFormat.Appendable,
      pageTitle: Option[String] = None,
      activeItemId: Option[String] = None,
      showSignOut: Boolean = true,
      signOutUrl: Option[String] = None,
      scripts: Seq[HtmlFormat.Appendable] = Seq.empty,
      stylesheets: Seq[HtmlFormat.Appendable] = Seq.empty,
      fullWidth: Boolean = true
  )(implicit
      hc: HeaderCarrier,
      request: RequestHeader,
      messages: Messages
  ): Future[HtmlFormat.Appendable] =
    connector.getMenu().map { menu =>
      standardLayout(
        menu = menu,
        content = content,
        pageTitle = pageTitle,
        activeItemId = activeItemId,
        quickSearchUrl = config.quickSearchPath,
        showSignOut = showSignOut,
        signOutUrl = signOutUrl,
        scripts = scripts,
        stylesheets = stylesheets,
        fullWidth = fullWidth
      )
    }

  /** Render the full page layout with an already-fetched menu (useful in tests or when menu is cached). */
  def standardCatalogueLayoutWithMenu(
      menu: BannerMenu,
      content: HtmlFormat.Appendable,
      pageTitle: Option[String] = None,
      activeItemId: Option[String] = None,
      showSignOut: Boolean = true,
      signOutUrl: Option[String] = None,
      scripts: Seq[HtmlFormat.Appendable] = Seq.empty,
      stylesheets: Seq[HtmlFormat.Appendable] = Seq.empty,
      fullWidth: Boolean = true
  )(implicit
      request: RequestHeader,
      messages: Messages
  ): HtmlFormat.Appendable =
    standardLayout(
      menu = menu,
      content = content,
      pageTitle = pageTitle,
      activeItemId = activeItemId,
      quickSearchUrl = config.quickSearchPath,
      showSignOut = showSignOut,
      signOutUrl = signOutUrl,
      scripts = scripts,
      stylesheets = stylesheets,
      fullWidth = fullWidth
    )

  /** Fetch the menu and render only the navbar/search bar. Use this when you want to embed the menu bar into an
    * existing layout rather than replacing the whole page shell.
    */
  def catalogueMenuBar(
      activeItemId: Option[String] = None,
      showSignOut: Boolean = true,
      signOutUrl: Option[String] = None
  )(implicit
      hc: HeaderCarrier,
      request: RequestHeader,
      messages: Messages
  ): Future[HtmlFormat.Appendable] =
    connector.getMenu().map { menu =>
      CatalogueMenuBarView(
        menu = menu,
        activeItemId = activeItemId,
        quickSearchUrl = config.quickSearchPath,
        showSignOut = showSignOut,
        signOutUrl = signOutUrl
      )
    }

  /** Render only the navbar/search bar with an already-fetched menu. */
  def catalogueMenuBarWithMenu(
      menu: BannerMenu,
      activeItemId: Option[String] = None,
      showSignOut: Boolean = true,
      signOutUrl: Option[String] = None
  )(implicit
      request: RequestHeader,
      messages: Messages
  ): HtmlFormat.Appendable =
    CatalogueMenuBarView(
      menu = menu,
      activeItemId = activeItemId,
      quickSearchUrl = config.quickSearchPath,
      showSignOut = showSignOut,
      signOutUrl = signOutUrl
    )
