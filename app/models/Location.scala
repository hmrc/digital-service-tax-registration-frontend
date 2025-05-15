/*
 * Copyright 2025 HM Revenue & Customs
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

package models

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import play.api.Environment
import play.api.libs.json._
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

import scala.util.matching.Regex

@Singleton
class Location @Inject() (env: Environment, appConfig: FrontendAppConfig) {

  private val alphaTwoCodeRegex: Regex = "^([A-Z]{2}|[A-Z]{2}-[A-Z0-9]{2})$".r
  private val countryCode_GB           = "GB"

  def locations: List[Country] =
    (env.resourceAsStream(appConfig.canonicalList) map Json.parse map {
      _.as[List[Country]].filter(l => l.code.matches(alphaTwoCodeRegex.toString()))
    }).getOrElse(throw new Exception("location-autocomplete-canonical-list.json file is missing"))

  def name(code: String): String = locations.find(x => x.code == code).map(_.name).getOrElse("unknown")

  def countryListWithoutGB: Seq[Country] = locations.filter(x => x.code != countryCode_GB)

  def countrySelectList(value: Map[String, String], countries: Seq[Country]): Seq[SelectItem] = {
    def containsCountry(country: Country): Boolean =
      value.get("country") match {
        case Some(countryCode) => countryCode == country.code
        case _                 => false
      }

    val countryJsonList = countries.map { country =>
      SelectItem(Some(country.code), country.name, containsCountry(country))
    }
    SelectItem(Some(""), "") +: countryJsonList
  }
}
