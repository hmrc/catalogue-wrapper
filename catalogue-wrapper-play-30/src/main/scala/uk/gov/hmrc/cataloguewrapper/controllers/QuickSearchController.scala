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

package uk.gov.hmrc.cataloguewrapper.controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cataloguewrapper.config.CatalogueWrapperConfig
import uk.gov.hmrc.cataloguewrapper.models.SearchTerm
import uk.gov.hmrc.cataloguewrapper.search.SearchIndex
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class QuickSearchController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    searchIndex: SearchIndex,
    config: CatalogueWrapperConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController:

  def search(query: String, limit: Option[Int]): Action[AnyContent] =
    Action {
      val queryTerms =
        query.trim
          .split("\\s+")
          .toIndexedSeq
          .map(SearchTerm.normalise)
          .filter(_.length >= config.quickSearchMinTermLength)
          .take(config.quickSearchMaxTerms)

      val results =
        if queryTerms.isEmpty then Seq.empty
        else searchIndex.search(queryTerms).take(limit.getOrElse(config.quickSearchLimit))

      Ok(Json.toJson(results))
    }
