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
import uk.gov.hmrc.cataloguewrapper.connectors.CatalogueMenuConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

// TODO: When catalogue-navigation becomes user/admin-aware, decide whether this proxy
// should enforce consuming-service auth or simply forward session/header context.
// Option A: remain unauthenticated, forward headers, let catalogue-navigation decide.
//    - It will be this but the frontend will be authenticated and the backend will also validate that authentication
//    - passed through HeaderCarrier as session cookies
// Option B: consuming service owns the route and wraps it in its own IdentifierAction.
// Option C: wrapper exposes an injected action-builder hook.
@Singleton
class QuickSearchController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    connector: CatalogueMenuConnector,
    config: CatalogueWrapperConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController:

  def search(query: String, limit: Option[Int]): Action[AnyContent] =
    Action.async { implicit request =>
      given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      connector
        .search(query, limit.getOrElse(config.quickSearchLimit))
        .map(results => Ok(Json.toJson(results)))
    }
