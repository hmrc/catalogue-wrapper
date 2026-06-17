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

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.cataloguewrapper.connectors.CatalogueMenuConnector
import uk.gov.hmrc.cataloguewrapper.models.SearchTerm
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class QuickSearchControllerSpec extends AnyWordSpec with Matchers with MockitoSugar:

  private val mockConnector = mock[CatalogueMenuConnector]

  private val controller = new QuickSearchController(
    stubMessagesControllerComponents(),
    mockConnector
  )

  "search" should {
    "return OK with JSON results" in {
      val results = Seq(SearchTerm("service", "foo", "/services/foo"))
      when(mockConnector.search(eqTo("foo"), eqTo(20))(any[HeaderCarrier]))
        .thenReturn(Future.successful(results))

      val result = controller.search("foo", 20)(FakeRequest())
      status(result) shouldBe OK
      contentType(result) shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(results)
    }

    "return empty array when connector returns empty" in {
      when(mockConnector.search(any[String], any[Int])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Seq.empty))

      val result = controller.search("zzz", 20)(FakeRequest())
      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.arr()
    }
  }
