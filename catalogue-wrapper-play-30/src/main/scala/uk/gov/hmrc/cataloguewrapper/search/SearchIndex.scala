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

import uk.gov.hmrc.cataloguewrapper.models.SearchTerm

import java.util.concurrent.atomic.AtomicReference
import javax.inject.Singleton

@Singleton
class SearchIndex:

  private val cachedIndex =
    new AtomicReference[Map[String, Seq[SearchTerm]]](Map.empty)

  def replaceAll(terms: Seq[SearchTerm]): Unit =
    cachedIndex.set(SearchIndex.optimiseIndex(terms))

  def search(queryTerms: Seq[String]): Seq[SearchTerm] =
    SearchIndex.search(queryTerms, cachedIndex.get())

  def isPopulated: Boolean =
    cachedIndex.get().nonEmpty

object SearchIndex:

  def search(
      queryTerms: Seq[String],
      index: Map[String, Seq[SearchTerm]]
  ): Seq[SearchTerm] =
    val normalised = queryTerms.map(SearchTerm.normalise)

    normalised.headOption match
      case None =>
        Seq.empty

      case Some(first) =>
        normalised
          .foldLeft(index.getOrElse(first.take(3), Seq.empty)) { (acc, cur) =>
            acc.filter(_.terms.exists(_.contains(cur)))
          }
          .map { st =>
            if normalised.exists(_ == SearchTerm.normalise(st.name)) then st.copy(weight = 1.0f)
            else st
          }
          .sortBy(st => (-st.weight, st.name.toLowerCase))
          .distinct

  def optimiseIndex(terms: Seq[SearchTerm]): Map[String, Seq[SearchTerm]] =
    terms
      .flatMap { st =>
        val searchableValues =
          Seq(st.linkType, st.name) ++ st.hints.toSeq

        searchableValues.flatMap { value =>
          SearchTerm
            .normalise(value)
            .sliding(3, 1)
            .map(chunk => chunk -> st)
        }
      }
      .groupBy(_._1)
      .view
      .mapValues(_.map(_._2))
      .toMap
