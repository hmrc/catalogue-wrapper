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

package uk.gov.hmrc.cataloguewrapper.search

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.cataloguewrapper.models.SearchTerm

class SearchIndexSpec extends AnyWordSpec with Matchers:

  private def term(
      name: String,
      linkType: String = "service",
      hints: Set[String] = Set.empty,
      weight: Float = 0.5f
  ): SearchTerm =
    SearchTerm(linkType = linkType, name = name, href = s"/$name", weight = weight, hints = hints)

  "SearchTerm.normalise" should {
    "lower-case the value" in {
      SearchTerm.normalise("FooBar") shouldBe "foobar"
    }

    "strip spaces" in {
      SearchTerm.normalise("foo bar") shouldBe "foobar"
    }

    "strip hyphens" in {
      SearchTerm.normalise("foo-bar") shouldBe "foobar"
    }

    "strip underscores" in {
      SearchTerm.normalise("foo_bar") shouldBe "foobar"
    }
  }

  "SearchIndex.optimiseIndex" should {
    "index name, linkType, and each hint separately as sliding 3-char chunks" in {
      val terms = Seq(term("abc", linkType = "svc", hints = Set("foo", "bar")))
      val index = SearchIndex.optimiseIndex(terms)
      // name "abc", linkType "svc", hint "foo", hint "bar" all indexed
      index.keys should contain allOf ("abc", "svc", "foo", "bar")
    }

    "not produce cross-hint chunks" in {
      // hints "foo" and "bar" should NOT produce chunk "oob" (from "foobar" mkString)
      val terms = Seq(term("xyz", hints = Set("foo", "bar")))
      val index = SearchIndex.optimiseIndex(terms)
      index.keys should not contain "oob"
    }

    "map each chunk to the originating SearchTerm" in {
      val t     = term("foo-bar")
      val index = SearchIndex.optimiseIndex(Seq(t))
      index("foo") should contain(t)
    }
  }

  "SearchIndex.search" should {
    "return empty when queryTerms is empty" in {
      val index = SearchIndex.optimiseIndex(Seq(term("foo-service")))
      SearchIndex.search(Seq.empty, index) shouldBe Seq.empty
    }

    "return matching terms for a single query token" in {
      val t     = term("foo-service")
      val index = SearchIndex.optimiseIndex(Seq(t))
      SearchIndex.search(Seq("foo"), index) should contain(t)
    }

    "apply AND semantics — both tokens must match" in {
      val t1    = term("foo-bar")
      val t2    = term("foo-baz")
      val index = SearchIndex.optimiseIndex(Seq(t1, t2))
      val res   = SearchIndex.search(Seq("foo", "bar"), index)
      res should contain(t1)
      res should not contain t2
    }

    "boost to weight 1.0 when a query token exactly matches the normalised name" in {
      val t     = term("my-service")
      val index = SearchIndex.optimiseIndex(Seq(t))
      val res   = SearchIndex.search(Seq("myservice"), index)
      res.head.weight shouldBe 1.0f
    }

    "sort by weight descending then name ascending" in {
      val hi    = term("aaa-service", weight = 0.9f)
      val lo    = term("bbb-service", weight = 0.1f)
      val mid   = term("ccc-service", weight = 0.5f)
      val index = SearchIndex.optimiseIndex(Seq(lo, mid, hi))
      val res   = SearchIndex.search(Seq("ser"), index)
      res.map(_.name) shouldBe Seq("aaa-service", "ccc-service", "bbb-service")
    }

    "deduplicate results" in {
      val t     = term("dupe-service")
      val index = SearchIndex.optimiseIndex(Seq(t, t))
      SearchIndex.search(Seq("dup"), index).count(_ == t) shouldBe 1
    }
  }

  "SearchIndex (stateful)" should {
    "replace the index and return updated search results" in {
      val idx = new SearchIndex
      idx.replaceAll(Seq(term("old-service")))
      idx.search(Seq("old")) should not be empty

      idx.replaceAll(Seq(term("new-service")))
      idx.search(Seq("new")) should not be empty
      idx.search(Seq("old")) shouldBe Seq.empty
    }

    "return empty when index has not been populated" in {
      val idx = new SearchIndex
      idx.search(Seq("foo")) shouldBe Seq.empty
    }

    "report isPopulated false before replaceAll" in {
      val idx = new SearchIndex
      idx.isPopulated shouldBe false
    }

    "report isPopulated true after replaceAll with non-empty terms" in {
      val idx = new SearchIndex
      idx.replaceAll(Seq(term("foo-service")))
      idx.isPopulated shouldBe true
    }

    "report isPopulated true after replaceAll with empty terms (loaded but empty)" in {
      val idx = new SearchIndex
      idx.replaceAll(Seq.empty)
      idx.isPopulated shouldBe true
      idx.search(Seq("foo")) shouldBe Seq.empty
    }
  }
