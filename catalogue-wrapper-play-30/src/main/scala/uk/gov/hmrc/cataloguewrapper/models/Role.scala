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

import play.api.libs.json.*

sealed abstract class Role(val roleIdentifier: String)

object Role:
  case object CanCreate      extends Role("CAN_CREATE_USERS")
  case object CanManageUsers extends Role("CAN_MANAGE_USERS")

  val values: List[Role] =
    List(CanCreate, CanManageUsers)

  private val byIdentifier: Map[String, Role] =
    values.map(r => r.roleIdentifier -> r).toMap

  given Format[Role] with
    override def writes(o: Role): JsValue =
      JsString(o.roleIdentifier)

    override def reads(json: JsValue): JsResult[Role] =
      json.validate[String].flatMap { identifier =>
        byIdentifier.get(identifier) match
          case Some(role) => JsSuccess(role)
          case None       => JsError(s"Unknown Role identifier: $identifier")
      }
