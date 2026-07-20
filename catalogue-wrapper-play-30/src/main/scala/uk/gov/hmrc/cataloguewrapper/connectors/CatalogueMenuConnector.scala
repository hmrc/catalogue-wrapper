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

import uk.gov.hmrc.cataloguewrapper.config.CatalogueWrapperConfig
import uk.gov.hmrc.cataloguewrapper.models.{BannerMenu, NavigationData, SearchTerm}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CatalogueMenuConnector @Inject() (
    httpClient: HttpClientV2,
    config: CatalogueWrapperConfig
)(implicit ec: ExecutionContext):

  def getNavigationData()(implicit hc: HeaderCarrier): Future[NavigationData] =
    val menuF        = getMenu()
    val searchIndexF = getSearchIndex()

    menuF
      .zip(searchIndexF)
      .map { case (menu, searchIndex) =>
        NavigationData(menu = menu, searchIndex = searchIndex)
      }

  private def getMenu()(implicit hc: HeaderCarrier): Future[BannerMenu] =
    httpClient
      .get(url"${config.menuBarBaseUrl}/menu-bar/menu")
      .execute[BannerMenu]

  private def getSearchIndex()(implicit hc: HeaderCarrier): Future[Seq[SearchTerm]] =
    httpClient
      .get(url"${config.menuBarBaseUrl}/menu-bar/search-index")
      .execute[Seq[SearchTerm]]
